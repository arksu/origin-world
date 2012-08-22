unit main;

interface

uses
  generics.collections, ShellAPI,
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls, ExtCtrls, ComCtrls, utils_xml, parser_utils, inifiles,
  pngimage, IdBaseComponent, IdComponent, IdTCPConnection, IdTCPClient, IdHTTP;

const
  TILE_W = 46;
  TILE_H = 23;
  OUT_DIR = 'out\';
  HOST = '127.0.0.1';
  PORT = '222';
  DIR = '/res/';
  VERSIONS = 'versions.xml';
  FTP_DEF_PASS = '';
  FTP_DEF_USER = '';
  OPTIONS_FILENAME = 'res.ini';

type
  TVersion = record
    name : string;
    ver : Integer;
  end;

  TResForm = class(TForm)
    logmemo: TMemo;
    Panel1: TPanel;
    Button1: TButton;
    OpenDialog1: TOpenDialog;
    http: TIdHTTP;
    procedure Button1Click(Sender: TObject);
    procedure FormShow(Sender: TObject);
    procedure FormCreate(Sender: TObject);
  private
    version : array of TVersion;

    server_host, server_port : string;
    ftp_user, ftp_pass : string;
  public
    procedure ParseFile(fname : string);
    procedure UploadFile;
    procedure LoadVersions;
    function GetVer(aname : string) : Integer;
  end;

  PFileIO = ^TFileIO;
  TFileIO = class
  public
          buffer : array[0..65535] of byte;
          pos : Cardinal;

          constructor Create;
          function Clear : PFileIO;
          function Send2Stream(s : TStream) : PFileIO;
          function Write<T>(value : T): PFileIO;
          function WriteJavaUInt(i : Cardinal) : PFileIO;
          function WriteString(value : WideString): PFileIO;
          function WriteShortString(value : AnsiString): PFileIO;
          function Read<T>(var source : pointer) : T;
          function ReadString(var source : pointer) : string;
  end;

  TRes = class
  protected
    procedure WriteFile(fname : string; outs : TStream);
  public
    function GetResName : string; virtual; abstract;
    procedure Write2Stream(outs : TStream); virtual; abstract;
    procedure Parse(node : TXML); virtual; abstract;

    procedure OnCreate; virtual;
    procedure OnDestroy; virtual;
    destructor Destroy; override;
    constructor Create(node : TXML);
  end;
  CRes = class of TRes;

  TResType = record
    name : string;
    c : CRes;
  end;

  TResDraw = class(TRes)
  public
    name : string;
    layers : TList<TRes>;
    size : TPoint;
    offset : TPoint;

    procedure OnCreate; override;
    procedure OnDestroy; override;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TResTexture = class(TRes)
  public
    name : string;
    fname : string;
    width : Integer;
    height : Integer;

    procedure GetSize;
    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TResSprite = class(TRes)
  public
    z : Integer;
    addz : Integer;
    tex_name : string;
    offset : TPoint;
    tex_rect : TIntCoords;
    tag : Integer;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TResFrame = class(TRes)
  public
    tex_name : string;
    tex_rect : TIntCoords;
    procedure OnCreate; override;
    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
    constructor Create(atex_name : string; atx, aty, atw, ath : Integer); overload;
  end;

  TResAnim = class(TRes)
  public
    z : Integer;
    addz : Integer;
    offset : TPoint;
    time : Integer;
    frames : TList<TResFrame>;
    tag : Integer;

    procedure CreateFrames(node : TXML);

    procedure OnCreate; override;
    procedure OnDestroy; override;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TResWeightList = class(TRes)
  public
    items: TList<TRes>;

    procedure OnCreate; override;
    procedure OnDestroy; override;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TResWeightItem = class(TRes)
  public
    weight : Integer;
    items: TList<TRes>;

    procedure OnCreate; override;
    procedure OnDestroy; override;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TGround = class
    w : Integer;
    tx, ty : Integer;
    constructor Create;
  end;

  TTileFragment = class
    tx, ty : Integer;
    w : Integer;
    idx : Integer;
    constructor Create;
  end;

  TFlavour = class
    draw_name : string;
    w : Integer;
  end;

  TResTile = class(TRes)
  public
    tex_name : string;
    type_id : Integer;
    ground : TList<TGround>;
    wall : TList<TGround>;
    border : TList<TTileFragment>;
    corner : TList<TTileFragment>;
    flavour : TList<TFlavour>;

    procedure OnCreate; override;
    procedure OnDestroy; override;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TResCursor = class(TRes)
  public
    name : string;
    tex_name : string;
    offset : TPoint;

    procedure OnCreate; override;
    procedure OnDestroy; override;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TResBinary = class(TRes)
  public
    name : string;
    file_name : string;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TResSound = class(TRes)
  public
    name : string;
    file_name : string;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;

  TResTextureArray = class(TRes)
  public
    width : Integer;
    height : Integer;
    file_name : string;
    count : integer;
    alpha_file_name : string;
    alpha_count : integer;

    function GetResName : string; override;
    procedure Parse(node : TXML); override;
    procedure Write2Stream(outs : TStream); override;
  end;


  TContainer = class
  private
    Resources : TList<TRes>;

  public
    // версия
    ver : Word;

    procedure AddRes(ares : TRes);
    function  GetTexture(name : string) : TResTexture;

    // записать все в файл
    procedure write_file(filename : string);

    procedure Clear;
    constructor Create;
    destructor Destroy; override;
  end;

var
  FileIO : TFileIO;
  ResForm: TResForm;
  res_types : array of TResType;
  outstream : TFileStream;
  log_level : Integer;
  container : TContainer;
  is_error : Boolean = False;
  need_upload : Boolean = False;
  curr_res_name : string;
  AppVersionString : string;

procedure WriteRes(r : TRes; s : TStream);
function currpath: string;
procedure AddResType(aname : string; ac : CRes);
procedure Log(msg : string);

implementation

{$R *.dfm}

procedure Log(msg : string);
var
  i : Integer;
  s : string;
begin
  s := '';
  for i := 0 to log_level - 1 do
    s := s + '  ';
  ResForm.logmemo.Lines.BeginUpdate;
  ResForm.logmemo.Lines.Add(s+msg);
  ResForm.logmemo.Lines.EndUpdate;
end;

procedure AddResType(aname : string; ac : CRes);
begin
  SetLength(res_types, length(res_types)+1 );
  res_types[High(res_types)].name := aname;
  res_types[High(res_types)].c := ac;
end;

function GetResType(aname : string) : CRes;
var
  t : TResType;
begin
  Result := TRes;
  for t in res_types do
    if t.name = aname then
    begin
      Result := t.c;
      Break;
    end;
end;

function currpath: string;
begin
  Result := extractfiledir(paramstr(0));
  if not IsPathDelimiter(Result, Length(Result)) then
    Result := Result + PathDelim;
end;

procedure TResForm.FormCreate(Sender: TObject);
var
  ini : TIniFile;
begin
  Caption := 'a1 Resource compiler  ver '+AppVersionString;
  version := nil;
  ini := TIniFile.Create(IncludeTrailingBackslash(ExtractFilePath(ParamStr(0))) + OPTIONS_FILENAME);
  server_host := ini.ReadString('main', 'host', HOST);
  server_port := ini.ReadString('main', 'port', PORT);
  ftp_user := ini.ReadString('main', 'ftp_user', FTP_DEF_USER);
  ftp_pass := ini.ReadString('main', 'ftp_pass', FTP_DEF_PASS);
  ini.Free;
end;

procedure TResForm.FormShow(Sender: TObject);
begin
  if ParamStr(1) <> '' then
  begin
    LoadVersions;

    if ParamStr(2) = 'upload' then
      need_upload := true;

    ParseFile(currpath+ParamStr(1));

    if (ParamStr(2) = 'upload') and (not is_error) then
    begin
      UploadFile;
    end;

    if not is_error then
      Application.Terminate;
  end;
end;

function TResForm.GetVer(aname: string): Integer;
var
  i : Integer;
begin
  for i := 0 to Length(version)-1 do
    if version[i].name = aname then
    begin
      if need_upload then
        version[i].ver := version[i].ver+1;
      Exit(version[i].ver);
    end;

  SetLength(version, Length(version)+1);
  version[High(version)].name := aname;
  version[High(version)].ver := 1;
  Exit(1);
end;

procedure TResForm.LoadVersions;
var
  ms : TMemoryStream;
  xml : TXML;
  Text     : String;
  UTF8Text : UTF8String;
  Size   : Integer;
  i : Integer;
begin
  http.Request.URL := 'http://'+server_host+DIR+'versions.xml';
  http.Request.Method := 'http';
  ms := TMemoryStream.Create;
  http.Get( 'http://'+server_host+DIR+'versions.xml', ms );

  Size := ms.Size;
  SetLength(UTF8Text, Size);
  ms.Seek(0,0);
  ms.Read(UTF8Text[1], Size);
  Text := UTF8ToString(UTF8Text);
  xml := TXML.Create(Text, 1);
  SetLength(UTF8Text, 0);
  ms.Free;

  if xml.Tag = 'list' then
  begin
    for i := 0 to xml.Count - 1 do
    begin
      if xml.NodeI[i].Tag = 'file' then
      begin
        SetLength(version, Length(version)+1);
        version[High(version)].name := xml.NodeI[i].Params['name'].Value;
        version[High(version)].ver := StrToIntDef(xml.NodeI[i].Params['ver'].Value, 1);
      end;
    end;

  end;
end;

procedure TResForm.ParseFile(fname: string);
var
  xml, node : TXML;
  i : Integer;
  C : CRes;
  out_name : string;
  ver : Word;
begin
  logmemo.Lines.Clear;
  log_level := 0;
  Log('BEGIN parse file: '+fname);
  inc(log_level);
  xml := nil;
  try
    xml := TXML.Create(fname);

    if xml.Tag = 'a1res' then begin
      Log('begin add types');

      inc(log_level);
      container := TContainer.Create;
      out_name := xml.Params['name'].Value;
      ver := GetVer(out_name);

      for i := 0 to xml.Count - 1 do
      begin
        node := xml.NodeI[i];
        C := GetResType(node.Tag);

        if C <> TRes then
          container.AddRes( C.Create(node) );
      end;

      Dec(log_level);
      Log('end add types');

      container.ver := ver;
      container.write_file(currpath + OUT_DIR + out_name+'.res');
      curr_res_name := out_name + '.res';
      container.Free;
    end;

  finally
    if xml <> nil then
      xml.Free;
  end;
  dec(log_level);
  Log('end parse file: '+fname);
end;


procedure TResForm.UploadFile;
var
  st : TStringList;
  i : Integer;
begin
  // генерим хмл
  DeleteFile(currpath+OUT_DIR+VERSIONS);
  st := TStringList.Create;
  st.Add('<?xml version="1.0"?>');
  st.Add('<list>');
  for i := 0 to Length(version) - 1 do
    st.Add('<file name="'+version[i].name+'" ver="'+IntToStr(version[i].ver)+'" />');
  st.Add('</list>');
  st.SaveToFile(currpath+OUT_DIR+VERSIONS);

  // генерим скрипт
  st.Clear;
  st.Add('open ftp://'+ftp_user+':'+ftp_pass+'@'+server_host+':'+server_port);
  st.Add('option confirm off');
  // загружаем хмлку
  st.Add('put "'+currpath+OUT_DIR+VERSIONS+'"');
  // загружаем файл ресурса
  st.Add('put "'+currpath+OUT_DIR+curr_res_name+'"');
  st.Add('exit');
  DeleteFile(currpath+'winscp_script');
  st.SaveToFile( currpath+'winscp_script' );

  // запускаем скрипт
  ShellExecute( Handle, 'open', 'winscp.exe','/console /script=winscp_script', nil, SW_NORMAL );
end;

{$REGION 'TFileIO'}
{ TFileIO }

function TFileIO.Clear: PFileIO;
begin
        pos := 0;

        result := @self;
end;

constructor TFileIO.Create;
begin
        Clear;
end;

function TFileIO.Read<T>(var source: pointer): T;
begin
        result := T(source^);
        inc(Cardinal(source), SizeOf(T));
end;

function TFileIO.ReadString(var source: pointer): string;
var
        str : WideString;
        chr : WideChar;
begin
        repeat
                chr := WideChar(source^);
                Inc(Cardinal(source), SizeOf(WideChar));
                if (chr = WideChar(0)) then begin
                        break;
                end;
                str := str + chr;
        until (false);

        result := str;
end;

function TFileIO.Send2Stream(s: TStream): PFileIO;
begin
  s.Write( buffer[0], pos );

  Result := @Self;
end;

function TFileIO.Write<T>(value: T): PFileIO;
var
        ptr : Pointer;
begin
        ptr := @buffer[pos];
        T(ptr^) := value;
        inc(pos, SizeOf(T));

        result := @self;
end;

function TFileIO.WriteJavaUInt(i: Cardinal): PFileIO;
  var
    C: array[0..3] of Byte absolute i;
begin
        buffer[pos] := C[3]; Inc(pos);
        buffer[pos] := C[2]; Inc(pos);
        buffer[pos] := C[1]; Inc(pos);
        buffer[pos] := C[0]; Inc(pos);
        result := @self;
end;

function TFileIO.WriteShortString(value: AnsiString): PFileIO;
var
        i : Integer;
begin
        Write<word>(Length(value));
        if Length(value) = 0 then exit(@self);

        for i := 1 to Length(value) do
          write<Byte>(Ord(value[i]));

        result := @self;
end;

function TFileIO.WriteString(value: WideString): PFileIO;
var
        size : Cardinal;
begin
        if (value = '') then begin
                Write<Byte>($00);
                Write<Byte>($00);
                exit(@self);
        end;

        size := Length(value) * 2 + 2;
        Move(PWideString(value)^, buffer[pos], size);
        inc(pos, size);

        result := @self;
end;
{$ENDREGION}

{ TContainer }

procedure TContainer.AddRes(ares: TRes);
begin
  Resources.Add(ares);
end;

procedure TContainer.Clear;
var
  i : TRes;
begin
  for i in Resources do
    i.Free;
  Resources.Clear;
end;

constructor TContainer.Create;
begin
  Resources := tlist<TRes>.Create;
end;

destructor TContainer.Destroy;
begin
  Clear;
  Resources.Free;
  inherited;
end;

function TContainer.GetTexture(name: string): TResTexture;
var
  r : TRes;
begin
  for r in Resources do
    if r is TResTexture then
      if (r as TResTexture).name = name then
        Exit(r as TResTexture);
  Result := nil;
end;

procedure TContainer.write_file(filename: string);
var
//  i : Integer;
  r : TRes;
begin
    Log('begin write file: '+filename);
    if FileExists(filename) then
      DeleteFile(filename);
    inc(log_level);
    
    outstream := TFileStream.Create(filename, fmCreate);
    try
      FileIO.Clear.
        Write<Byte>(37).
        Write<Byte>(75).
        Write<word>(ver).  // ver
        Write<word>(Resources.Count).  // count res
        Send2Stream(outstream);

      for r in Resources do
      begin
        WriteRes(r,outstream);
      end;
    finally
      outstream.Free;
    end;
    Dec(log_level);
    Log('end write file: '+filename);
end;

{ TResImage }

function TResTexture.GetResName: string;
begin
  Result := 'texture';
end;

procedure TResTexture.GetSize;
var
  ext : string;
  png : TPngImage;
begin
  ext := ExtractFileExt(fname);
  if LowerCase(ext) = '.png' then
  begin
    png := TPngImage.Create;
    png.LoadFromFile(currpath + fname);
    width := png.Width;
    height := png.Height;
    png.Free;
  end else begin
    Log('ERROR: unknown file extension');
    is_error := true;
  end;
end;

function checkn2(a : Integer) : Boolean;
var
  n2 : array[0..10] of Integer;
  i : Integer;
begin
  n2[0] := 2;
  n2[1] := 4;
  n2[2] := 8;
  n2[3] := 16;
  n2[4] := 32;
  n2[5] := 64;
  n2[6] := 128;
  n2[7] := 256;
  n2[8] := 512;
  n2[9] := 1024;
  n2[10] := 2048;

  Result := false;
  for i := 0 to 10 do
    if n2[i] = a then begin
      Result := true;
      Break;
    end;
end;

procedure TResTexture.Parse(node: TXML);
begin
  name := node.Params['name'].Value;
  fname := node.Params['srcfile'].Value;
  GetSize;

  if (not checkn2(width)) or (not checkn2(height)) then
    MessageBox(Application.Handle, pchar('размер текстуры не в степени 2 name='+name+' file='+fname),'ПОШЕЛ НАХУЙ',MB_ICONERROR);
end;


procedure TResTexture.Write2Stream(outs: TStream);
begin
    FileIO.Clear.
      WriteShortString(name).
      Send2Stream(outs);
    WriteFile(fname, outs);
end;

procedure TResForm.Button1Click(Sender: TObject);
begin
  if OpenDialog1.Execute then
    ParseFile(OpenDialog1.FileName);
end;

{ TRes }

constructor TRes.Create(node: TXML);
begin
  inc(log_level);
  OnCreate;
  Parse(node);
  Dec(log_level);
end;

destructor TRes.Destroy;
begin
  OnDestroy;
  inherited;
end;

procedure TRes.OnCreate;
begin
end;

procedure TRes.OnDestroy;
begin
end;

procedure TRes.WriteFile(fname: string; outs: TStream);
var
  fs : TFileStream;
begin
  fs := TFileStream.Create(currpath + fname, fmOpenRead);
  FileIO.Clear.WriteJavaUInt(fs.Size).Send2Stream(outs);
  outs.CopyFrom(fs, 0);
  fs.Free;
end;

{ TResSprite }

function TResSprite.GetResName: string;
begin
  Result := 'sprite';
end;

procedure TResSprite.Parse(node: TXML);
begin
  z := StrToIntDef(node.Params['z'].Value, 10);
  addz := StrToIntDef(node.Params['addz'].Value, 0);
  if addz < 0 then addz := 32768 - addz;

  offset := StrToPoint(node.Params['offset'].Value);
  tex_name := node.Params['texture'].Value;
  tex_rect := StrToIntCoords(node.Params['tex_rect'].Value);
  tag := StrToIntDef(node.Params['tag'].Value, 0);
end;

procedure TResSprite.Write2Stream(outs: TStream);
begin
    FileIO.Clear.
        WriteShortString(tex_name).
        Write<Word>(z).
        Write<Word>(tag).
        Write<Word>(addz).
        Write<Word>(offset.X).
        Write<Word>(offset.Y).
        Write<Word>(tex_rect.X).
        Write<Word>(tex_rect.Y).
        Write<Word>(tex_rect.W).
        Write<Word>(tex_rect.H).
        Send2Stream(outs);
end;

{ TResDraw }

function TResDraw.GetResName: string;
begin
  Result := 'draw';
end;

procedure TResDraw.OnCreate;
begin
  inherited;
  layers := TList<TRes>.Create;
end;

procedure TResDraw.OnDestroy;
var
  r : TRes;
begin
  inherited;
  for r in layers do
    r.Free;

  layers.Free;
end;

procedure TResDraw.Parse(node: TXML);
var
  i : Integer;
  C : CRes;
  r : TRes;
begin
  name := node.Params['name'].Value;
  size := StrToPoint(node.Params['size'].Value);
  offset := StrToPoint(node.Params['offset'].Value);

  for i := 0 to node.Count - 1 do
  begin
    C := GetResType(node.NodeI[i].Tag);

    if C <> TRes then
    begin
      r := C.Create(node.NodeI[i]);
      Log('draw add layer: '+r.GetResName);
      layers.Add(r);
    end else begin
      Log('!>>> unkonwn draw type: '+node.NodeI[i].Tag);
    end;
  end;
end;

procedure TResDraw.Write2Stream(outs: TStream);
var
  r : TRes;
begin
  FileIO.Clear.
        WriteShortString(name).
        Write<Word>(size.X).
        Write<Word>(size.Y).
        Write<Word>(offset.X).
        Write<Word>(offset.Y).
        Write<Word>(layers.Count).
        Send2Stream(outs);

  for r in layers do
  begin
    Log('draw: write layer "'+r.GetResName+'"');
    writeres(r, outs);
  end;
end;

{ TResAnim }

procedure TResAnim.CreateFrames(node: TXML);
var
  i, count : Integer;
  size : TPoint;
  tex_name : string;
  tex_rect : TIntCoords;
  cx, cy : Integer;
//  tex : TResTexture;
  f : TResFrame;
begin
  Inc(log_level);
  count := StrToIntDef(node.Params['count'].Value, 0);
  size := StrToPoint(node.Params['size'].Value);
  tex_rect := StrToIntCoords(node.Params['tex_rect'].Value);
  tex_name := node.Params['texture'].Value;
//  tex := container.GetTexture(tex_name);
//  if tex = nil then
//  begin
//    Log('ERROR: no texture! name='+tex_name);
//    is_error := True;
//    Dec(log_level);
//    Exit;
//  end;

  cx := tex_rect.X;
  cy := tex_rect.Y;
  for i := 0 to count - 1 do
  begin
    if cx + size.x > tex_rect.X+tex_rect.W then
      if cx = 0 then begin
        Log('ERROR: width too small');
        is_error := True;
      end
      else
      begin
        cx := 0;
        cy := cy + size.Y;
      end;

    if cy + size.y > tex_rect.Y+tex_rect.H then
      Break;

    Log('frame created, num='+IntToStr(i)+' tx='+IntToStr(cx)+
    ' ty='+IntToStr(cy)+' tw='+IntToStr(size.x)+' th='+IntToStr(size.y));
    f := TResFrame.Create(tex_name, cx, cy, size.x, size.y);
    frames.Add(f);
    cx := cx + size.x;
  end;
  Dec(log_level);
end;

function TResAnim.GetResName: string;
begin
  Result := 'anim';
end;

procedure TResAnim.OnCreate;
begin
  inherited;
  frames := TList<TResFrame>.Create;
end;

procedure TResAnim.OnDestroy;
var
  r : TRes;
begin
  inherited;
  for r in frames do
    r.Free;

  frames.Free;
end;

procedure TResAnim.Parse(node: TXML);
var
  i : Integer;
  r : TResFrame;
begin
  z := StrToIntDef(node.Params['z'].Value, 10);
  addz := StrToIntDef(node.Params['addz'].Value, 0);
  if addz < 0 then addz := 32768 - addz;

  offset := StrToPoint(node.Params['offset'].Value);
  time := StrToIntDef(node.Params['time'].Value, 0);
  tag := StrToIntDef(node.Params['tag'].Value, 0);

  for i := 0 to node.Count - 1 do
  begin
    r := nil;
    if node.NodeI[i].Tag = 'frame' then
      r := TResFrame.Create(node.NodeI[i]);

    if node.NodeI[i].Tag = 'frames' then
      CreateFrames(node.NodeI[i]);

    if r <> nil then
    begin
      Log('anim add: '+r.GetResName);
      frames.Add(r);
    end;
  end;

end;

procedure TResAnim.Write2Stream(outs: TStream);
var
  r : TResFrame;
begin
  FileIO.Clear.
        Write<Word>(z).
        Write<Word>(tag).
        Write<Word>(addz).
        Write<Word>(offset.X).
        Write<Word>(offset.Y).
        Write<Word>(time).
        Write<Word>(frames.Count).
        Send2Stream(outs);

  for r in frames do
  begin
    Log('anim: write "'+r.GetResName+'"');
    writeres(r, outs);
  end;
end;

procedure WriteRes(r : TRes; s : TStream);
var
  tmp : TMemoryStream;
begin
        Inc(log_level);
        tmp := TMemoryStream.Create;
        Log(r.GetResName+' write to stream');
        Inc(log_level);
        r.Write2Stream(tmp);
        Dec(log_level);
        tmp.Seek(0,0);
        FileIO.Clear.
            WriteShortString(r.GetResName).
            WriteJavaUInt(tmp.Size).
            Send2Stream(s);
        s.CopyFrom(tmp,0);
        tmp.Free;
        Dec(log_level);
end;

{ TResFrame }

constructor TResFrame.Create(atex_name: string; atx, aty, atw, ath: Integer);
begin
  tex_name := atex_name;
  tex_rect := IntCoords(atx, aty, atw, ath);
end;

function TResFrame.GetResName: string;
begin
  Result := 'frame';
end;

procedure TResFrame.OnCreate;
begin
  inherited;
  tex_rect := IntCoords(0,0,0,0);
end;

procedure TResFrame.Parse(node: TXML);
var
  t : TResTexture;
begin
  if node = nil then
  begin
    Log('frame created');
    Exit;
  end;

  tex_name := node.Params['texture'].Value;
  tex_rect := StrToIntCoords(node.Params['tex_rect'].Value);

  if (tex_rect.W = 0) or (tex_rect.H = 0) then
  begin
    t := container.GetTexture(tex_name);
    if t <> nil then
    begin
      tex_rect := IntCoords(0,0, t.width, t.height);
      Log('no tex_rect. get from texture. tw='+IntToStr(tex_rect.W)+
      ' th='+IntToStr(tex_rect.H));
    end;
  end;
end;

procedure TResFrame.Write2Stream(outs: TStream);
begin
  FileIO.Clear.
        WriteShortString(tex_name).
        Write<Word>(tex_rect.X).
        Write<Word>(tex_rect.Y).
        Write<Word>(tex_rect.W).
        Write<Word>(tex_rect.H).
        Send2Stream(outs);
end;

{ TGround }

constructor TGround.Create;
begin
  w := 10;
end;

{ TTileFragment }

constructor TTileFragment.Create;
begin
  w := 10;
end;

{ TResTile }

function TResTile.GetResName: string;
begin
  Result := 'tile';
end;

procedure TResTile.OnCreate;
begin
  inherited;
  ground := TList<TGround>.Create;
  wall := TList<TGround>.Create;
  border := TList<TTileFragment>.Create;
  corner := TList<TTileFragment>.Create;
  flavour := TList<TFlavour>.Create;
end;

procedure TResTile.OnDestroy;
var
  g : TGround;
  t : TTileFragment;
  f : TFlavour;
begin
  for g in ground do
    g.Free;
  ground.Free;
  for g in wall do
    g.Free;
  wall.Free;
  for t in border do
    t.Free;
  border.Free;
  for t in corner do
    t.Free;
  corner.Free;
  for f in flavour do
    f.Free;
  flavour.Free;

  inherited;
end;

procedure TResTile.Parse(node: TXML);
var
  i : Integer;
  n : TXML;
  g : TGround;
  t : TTileFragment;
  tex : TResTexture;
  idx, tx, ty : Integer;
  f : TFlavour;
  pos : TPoint;
begin
  type_id := StrToIntDef(node.Params['type'].Value, 0);
  tex_name := node.Params['texture'].Value;
  tex := container.GetTexture(tex_name);
  if tex = nil then
  begin
    Log('ERROR: no texture! name='+tex_name);
    is_error := True;
    Dec(log_level);
    Exit;
  end;

  for i := 0 to node.Count - 1 do
  begin
    n := node.NodeI[i];
    if n.Tag = 'ground' then
    begin
      g := TGround.Create;
      g.w := StrToIntDef(n.Params['w'].Value,10);
      if n.Params.Param['pos'].Value <> '' then
      begin
        pos := StrToPoint(n.Params['pos'].Value);
        idx := -1;
        tx := pos.X;
        ty := pos.Y;
      end else begin
        idx := StrToIntDef(n.Params['tex_idx'].Value,0);
        ty := (idx div (tex.width div TILE_W)) * TILE_H;
        tx := (idx mod (tex.width div TILE_W)) * TILE_W;
      end;
      g.tx := tx;
      g.ty := ty;
      log('ground tex_idx='+IntToStr(idx)+' tx='+IntToStr(tx)+' ty='+IntToStr(ty));
      ground.Add(g);
    end;
    if n.Tag = 'wall' then
    begin
      g := TGround.Create;
      g.w := StrToIntDef(n.Params['w'].Value,10);
      if n.Params.Param['pos'].Value <> '' then
      begin
        pos := StrToPoint(n.Params['pos'].Value);
        idx := -1;
        tx := pos.X;
        ty := pos.Y;
      end else begin
        idx := StrToIntDef(n.Params['tex_idx'].Value,0);
        ty := (idx div (tex.width div TILE_W)) * TILE_H;
        tx := (idx mod (tex.width div TILE_W)) * TILE_W;
      end;
      g.tx := tx;
      g.ty := ty;
      log('wall tex_idx='+IntToStr(idx)+' tx='+IntToStr(tx)+' ty='+IntToStr(ty));
      wall.Add(g);
    end;
    if n.Tag = 'border' then
    begin
      t := TTileFragment.Create;
      t.w := StrToIntDef(n.Params['w'].Value,10);
      t.idx := StrToIntDef(n.Params['idx'].Value,0);
      if n.Params.Param['pos'].Value <> '' then
      begin
        pos := StrToPoint(n.Params['pos'].Value);
        idx := -1;
        tx := pos.X;
        ty := pos.Y;
      end else begin
        idx := StrToIntDef(n.Params['tex_idx'].Value,0);
        ty := (idx div (tex.width div TILE_W)) * TILE_H;
        tx := (idx mod (tex.width div TILE_W)) * TILE_W;
      end;
      log('border tex_idx='+IntToStr(idx)+' tx='+IntToStr(tx)+' ty='+IntToStr(ty));
      t.tx := tx;
      t.ty := ty;
      border.Add(t);
    end;
    if n.Tag = 'corner' then
    begin
      t := TTileFragment.Create;
      t.w := StrToIntDef(n.Params['w'].Value,10);
      t.idx := StrToIntDef(n.Params['idx'].Value,0);
      if n.Params.Param['pos'].Value <> '' then
      begin
        pos := StrToPoint(n.Params['pos'].Value);
        idx := -1;
        tx := pos.X;
        ty := pos.Y;
      end else begin
        idx := StrToIntDef(n.Params['tex_idx'].Value,0);
        ty := (idx div (tex.width div TILE_W)) * TILE_H;
        tx := (idx mod (tex.width div TILE_W)) * TILE_W;
      end;
      log('corner tex_idx='+IntToStr(idx)+' tx='+IntToStr(tx)+' ty='+IntToStr(ty));
      t.tx := tx;
      t.ty := ty;
      corner.Add(t);
    end;
    if n.Tag = 'flavour' then
    begin
      f := TFlavour.Create;
      f.w := StrToIntDef(n.Params['w'].Value,10);
      f.draw_name := n.Params['draw'].Value;
      log('flavour draw='+f.draw_name+' w='+IntToStr(f.w));
      flavour.Add(f);
    end;

  end;

end;

procedure TResTile.Write2Stream(outs: TStream);
var
  g : TGround;
  t : TTileFragment;
  f : TFlavour;
begin
    FileIO.Clear.
        Write<Word>(type_id).
        WriteShortString(tex_name).
        Write<Word>(ground.Count);

  for g in ground do
    FileIO.
        Write<Word>(g.w).
        Write<Word>(g.tx).
        Write<Word>(g.ty);

  FileIO.Write<Word>(wall.Count);
  for g in wall do
    FileIO.
        Write<Word>(g.w).
        Write<Word>(g.tx).
        Write<Word>(g.ty);

  FileIO.Write<Word>(border.Count);
  for t in border do
    FileIO.
        Write<Word>(t.idx).
        Write<Word>(t.w).
        Write<Word>(t.tx).
        Write<Word>(t.ty);

  FileIO.Write<Word>(corner.Count);
  for t in corner do
    FileIO.
        Write<Word>(t.idx).
        Write<Word>(t.w).
        Write<Word>(t.tx).
        Write<Word>(t.ty);

  FileIO.Write<Word>(flavour.Count);
  for f in flavour do
    FileIO.
        Write<Word>(f.w).
        WriteShortString(f.draw_name);

  FileIO.Send2Stream(outs);
end;

{ TResCursor }

function TResCursor.GetResName: string;
begin
  Result := 'cursor';
end;

procedure TResCursor.OnCreate;
begin
  inherited;

end;

procedure TResCursor.OnDestroy;
begin
  inherited;

end;

procedure TResCursor.Parse(node: TXML);
begin
  name := node.Params['name'].Value;
  offset := StrToPoint(node.Params['offset'].Value);
  tex_name := node.Params['srcfile'].Value;
end;

procedure TResCursor.Write2Stream(outs: TStream);
begin
    FileIO.Clear.
        WriteShortString(name).
        Write<Word>(offset.X).
        Write<Word>(offset.Y).
        Send2Stream(outs);
    WriteFile(tex_name, outs);
end;

{ TResBinary }

function TResBinary.GetResName: string;
begin
  Result := 'bin';
end;

procedure TResBinary.Parse(node: TXML);
begin
  name := node.Params['name'].Value;
  file_name := node.Params['srcfile'].Value;
end;

procedure TResBinary.Write2Stream(outs: TStream);
begin
    FileIO.Clear.
      WriteShortString(name).
      Send2Stream(outs);
    WriteFile(file_name, outs);
end;

{ TResOGG }

function TResSound.GetResName: string;
begin
  Result := 'sound';
end;

procedure TResSound.Parse(node: TXML);
begin
  name := node.Params['name'].Value;
  file_name := node.Params['srcfile'].Value;
end;

procedure TResSound.Write2Stream(outs: TStream);
begin
    FileIO.Clear.
      WriteShortString(name).
      Send2Stream(outs);
    WriteFile(file_name, outs);
end;

{ TResWeightList }

function TResWeightList.GetResName: string;
begin
  Result := 'weightlist';
end;

procedure TResWeightList.OnCreate;
begin
  inherited;
  items := TList<TRes>.Create;
end;

procedure TResWeightList.OnDestroy;
var
  i : TRes;
begin
  for i in items do
    i.Free;
  items.Free;

  inherited;
end;

procedure TResWeightList.Parse(node: TXML);
var
  i : Integer;
  r : TRes;
begin
  for i := 0 to node.Count - 1 do
  begin
    r := nil;
    if node.NodeI[i].Tag = 'item' then
      r := TResWeightItem.Create(node.NodeI[i]);

    if r <> nil then
    begin
      Log('weight list add: '+r.GetResName);
      items.Add(r);
    end else begin
      Log('!>>> weight list unknown: '+node.NodeI[i].Tag);
    end;
  end;

end;

procedure TResWeightList.Write2Stream(outs: TStream);
var
  r : TRes;
begin
  FileIO.Clear.
        Write<Word>(items.Count).
        Send2Stream(outs);

  for r in items do
  begin
    Log('weight item: write "'+r.GetResName+'"');
    writeres(r, outs);
  end;
end;

{ TResWeightItem }

function TResWeightItem.GetResName: string;
begin
  Result := 'item';
end;

procedure TResWeightItem.OnCreate;
begin
  inherited;
  items := TList<TRes>.Create;
end;

procedure TResWeightItem.OnDestroy;
var
  i : TRes;
begin
  for i in items do
    i.Free;
  items.Free;

  inherited;
end;

procedure TResWeightItem.Parse(node: TXML);
var
  i : Integer;
  cur_node : TXML;
  C : CRes;
begin
  weight := StrToIntDef(node.Params['weight'].Value, 10);

  for i := 0 to node.Count - 1 do
  begin
    cur_node := node.NodeI[i];
    C := GetResType(cur_node.Tag);

    if C <> TRes then
    begin
      Log('weight item add: '+cur_node.Tag);
      items.Add( C.Create(cur_node) );
    end;
  end;
end;

procedure TResWeightItem.Write2Stream(outs: TStream);
var
  r : TRes;
begin
  FileIO.Clear.
        Write<Word>(weight).
        Write<Word>(items.Count).
        Send2Stream(outs);

  for r in items do
  begin
    Log('weight item: write "'+r.GetResName+'"');
    writeres(r, outs);
  end;
end;

{ TResTextureArray }

function TResTextureArray.GetResName: string;
begin
  Result := 'texture_array';
end;

procedure TResTextureArray.Parse(node: TXML);
begin
  width := StrToIntDef(node.Params['w'].Value, 0);
  height := StrToIntDef(node.Params['h'].Value, 0);
  count := StrToIntDef(node.Params['count'].Value, 0);
  alpha_count := StrToIntDef(node.Params['alpha_count'].Value, 0);
  file_name := node.Params['file'].Value;
  alpha_file_name := node.Params['alphafile'].Value;
end;

procedure TResTextureArray.Write2Stream(outs: TStream);
begin
    FileIO.Clear.
        Write<Word>(width).
        Write<Word>(height).
        Write<Word>(count).
        Write<Word>(alpha_count).
        Send2Stream(outs);

    WriteFile(file_name, outs);
    WriteFile(alpha_file_name, outs);
end;

initialization
  FileIO := TFileIO.Create;
  res_types := nil;
  AddResType('texture',   TResTexture);
  AddResType('sprite',    TResSprite);
  AddResType('draw',      TResDraw);
  AddResType('weightlist',TResWeightList);
  AddResType('sprite',    TResSprite);
  AddResType('anim',      TResAnim);
  AddResType('tile',      TResTile);
  AddResType('cursor',    TResCursor);
  AddResType('bin',       TResBinary);
  AddResType('sound',     TResSound);
  AddResType('texture_array', TResTextureArray);

finalization
  FileIO.Free;
end.
