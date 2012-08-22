unit utils_xml;

// part of eXgine (XProger)
// http://code.google.com/p/exgine/

interface

uses
  Classes,
  StrUtils;

type
  TXMLParamValue = AnsiString; // Variant

  TXMLParam = record
    Name  : AnsiString;
    Value : AnsiString;
  end;

  TXMLParams = class
    constructor Create(const Text: AnsiString);
  protected
    FParams : array of TXMLParam;
    function GetParam(const Name: AnsiString): TXMLParam;
    function GetParamI(Idx: Integer): TXMLParam;
  public
    function Count: Integer;
    property Param[const Name: AnsiString]: TXMLParam read GetParam; default;
    property ParamI[Idx: Integer]: TXMLParam read GetParamI;
  end;

  TXML = class
    constructor Create(const FileName: string); overload;
    constructor Create(const aText: AnsiString; BeginPos: Integer); overload;
    destructor Destroy; override;
  protected
    FNode    : array of TXML;
    FTag     : AnsiString;
    FContent : AnsiString;
    FDataLen : Integer;
    FParams  : TXMLParams;
    function GetNode(const TagName: AnsiString): TXML;
    function GetNodeI(Idx: Integer): TXML;
  public
    function Count: Integer;
    property Tag: AnsiString read FTag;
    property Content: AnsiString read FContent;
    property DataLen: Integer read FDataLen;
    property Params: TXMLParams read FParams;
    property Node[const TagName: AnsiString]: TXML read GetNode; default;
    property NodeI[Idx: Integer]: TXML read GetNodeI;
  end;

implementation

uses
  SysUtils;

const
  NullParam : TXMLParam = (Name: ''; Value: '');

function TrimQuote(const Text: AnsiString): AnsiString;
begin
  Result := Text;
  if Result[1] = '"' then
    Delete(Result, 1, 1);
  if Result[Length(Result)] = '"' then
    Delete(Result, Length(Result), 1);
end;

function TrimCopy(const Text: AnsiString; Index, Count: Integer): AnsiString;
const
  TrimChars : set of AnsiChar = [#9, #10, #13, #32];
var
  i : Integer;
begin
  for i := Index to Index + Count - 1 do
    if Text[i] in TrimChars then
    begin
      Inc(Index);
      Dec(Count);
    end else
      break;
  for i := Index + Count - 1 downto Index do
    if Text[i] in TrimChars then
      Dec(Count)
    else
      break;
  Result := Copy(Text, Index, Count);
end;

{ TXMLParams }
constructor TXMLParams.Create(const Text: AnsiString);
var
  i          : Integer;
  Flag       : (F_BEGIN, F_NAME, F_VALUE);
  ParamIdx   : Integer;
  IndexBegin : Integer;
  ReadValue  : Boolean;
  TextFlag   : Boolean;
begin
  inherited Create;

  Flag       := F_BEGIN;
  ParamIdx   := -1;
  IndexBegin := 1;
  ReadValue  := False;
  TextFlag   := False;
  for i := 1 to Length(Text) do
    case Flag of
      F_BEGIN :
        if Text[i] <> ' ' then
        begin
          ParamIdx := Length(FParams);
          SetLength(FParams, ParamIdx + 1);
          FParams[ParamIdx].Name  := '';
          FParams[ParamIdx].Value := '';
          Flag := F_NAME;
          IndexBegin := i;
        end;
      F_NAME :
        if Text[i] = '=' then
        begin
          FParams[ParamIdx].Name := TrimCopy(Text, IndexBegin, i - IndexBegin);
          Flag := F_VALUE;
          IndexBegin := i + 1;
        end;
      F_VALUE :
        begin
          if Text[i] = '"' then
            TextFlag := not TextFlag;
          if (Text[i] <> ' ') and (not TextFlag) then
            ReadValue := True
          else
            if ReadValue then
            begin
              FParams[ParamIdx].Value := TrimQuote(TrimCopy(Text, IndexBegin, i - IndexBegin));
              Flag := F_BEGIN;
              ReadValue := False;
              ParamIdx := -1;
            end else
              continue;
        end;
    end;
  if ParamIdx <> -1 then
    FParams[ParamIdx].Value := TrimQuote(TrimCopy(Text, IndexBegin, Length(Text) - IndexBegin + 1));
end;

function TXMLParams.Count: Integer;
begin
  Result := Length(FParams);
end;

function TXMLParams.GetParam(const Name: AnsiString): TXMLParam;
var
  i     : Integer;
begin
  for i := 0 to Count - 1 do
    if FParams[i].Name = Name then
    begin
      Result.Name  := PAnsiChar(FParams[i].Name);
      Result.Value := PAnsiChar(FParams[i].Value);
      Exit;
    end;
  Result := NullParam;
end;

function TXMLParams.GetParamI(Idx: Integer): TXMLParam;
begin
  Result.Name  := PAnsiChar(FParams[Idx].Name);
  Result.Value := PAnsiChar(FParams[Idx].Value);
end;

{ TXML }
constructor TXML.Create(const FileName: string);
var
  Stream : TFileStream;
  Text   : AnsiString;
  Size   : Integer;
begin
  inherited Create;

  Stream := TFileStream.Create(FileName, fmOpenRead);
  if Stream <> nil then
  begin
    Size := Stream.Size;
    SetLength(Text, Size);
    Stream.Read(Text[1], Size);
    Create(Text, 1);
    Stream.Free;
  end;
end;

constructor TXML.Create(const aText: AnsiString; BeginPos: Integer);
var
  i, j : Integer;
  Flag : (F_BEGIN, F_TAG, F_PARAMS, F_CONTENT, F_END);
  BeginIndex : Integer;
  TextFlag   : Boolean;
  Text       : AnsiString;
begin
  inherited Create;

  Text:=aText;

  if BeginPos=1 then
  begin
    i:=Pos('<!--', Text);
    while i>0 do
    begin
      j:=PosEx('-->', Text, i);
      if j=0
        then j:=Length(Text);
      Delete(Text, i, j-i+3);
      i:=Pos('<!--', Text);
    end;
  end;

  TextFlag := False;
  Flag     := F_BEGIN;
  i := BeginPos - 1;

  BeginIndex := BeginPos;
  FContent := '';
  while i <= Length(Text) do
  begin
    Inc(i);
    case Flag of
    // waiting for new tag '<...'
      F_BEGIN :
        if Text[i] = '<' then
        begin
          Flag := F_TAG;
          BeginIndex := i + 1;
        end;
    // waiting for tag name '... ' or '.../' or '...>'
      F_TAG :
        begin
          case Text[i] of
            '>' : Flag := F_CONTENT;
            '/' : Flag := F_END;
            ' ' : Flag := F_PARAMS;
            '?', '!' :
              begin
                Flag := F_BEGIN;
                continue;
              end
          else
            continue;
          end;
          FTag := TrimCopy(Text, BeginIndex, i - BeginIndex);
          BeginIndex := i + 1;
        end;
    // parse tag parameters
      F_PARAMS :
        begin
          if Text[i] = '"' then
            TextFlag := not TextFlag;
          if not TextFlag then
          begin
            case Text[i] of
              '>' : Flag := F_CONTENT;
              '/' : Flag := F_END;
            else
              continue;
            end;
            FParams := TXMLParams.Create(TrimCopy(Text, BeginIndex, i - BeginIndex));
            BeginIndex := i + 1;
          end;
        end;
    // parse tag content
      F_CONTENT :
        begin
          case Text[i] of
            '"' : TextFlag := not TextFlag;
            '<' :
              if not TextFlag then
              begin
                FContent := FContent + TrimCopy(Text, BeginIndex, i - BeginIndex);
              // is new tag or my tag closing?
                for j := i to Length(Text) do
                  if Text[j] = '>' then
                  begin
                    if TrimCopy(Text, i + 1, j - i - 1) <> '/' + FTag then
                    begin
                      SetLength(FNode, Length(FNode) + 1);
                      FNode[Length(FNode) - 1] := TXML.Create(Text, i - 1);
                      i := i + FNode[Length(FNode) - 1].DataLen;
                      BeginIndex := i + 1;
                    end else
                      Flag := F_END;
                    break;
                  end;
              end
          end;
        end;
    // waiting for close tag
      F_END :
        if Text[i] = '>' then
        begin
          FDataLen := i - BeginPos;
          break;
        end;
    end;
  end;
end;

destructor TXML.Destroy;
var
  i : Integer;
begin
  for i := 0 to Count - 1 do
    NodeI[i].Free;
  FParams.Free;
  inherited;
end;

function TXML.Count: Integer;
begin
  Result := Length(FNode);
end;

function TXML.GetNode(const TagName: AnsiString): TXML;
var
  i : Integer;
begin
  for i := 0 to Count - 1 do
    if FNode[i].Tag = TagName then
    begin
      Result := FNode[i];
      Exit;
    end;
  Result := nil;
end;

function TXML.GetNodeI(Idx: Integer): TXML;
begin
  Result := FNode[Idx];
end;

end.
