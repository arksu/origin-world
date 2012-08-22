unit parser_utils;

interface

uses
  SysUtils, Types;

type
  TIntCoords = record
    X, Y, W, H : integer;
  end;

  TIntSize = record
    Width, Height : integer;
  end;

  function IntCoords(X,Y,W,H: integer) : TIntCoords; inline;
  function IntSize(W, H : integer) : TIntSize; inline;
  function Tok(const Sep: string; var S: string): string;
  function StrToColor(const S: string): Cardinal;
  function StrToIntCoords(S: string): TIntCoords;
  function StrToPoint(S: string): TPoint;

const
  ColorWhite = $FFFFFFFF;
  ColorRed = $FFFF0000;
  ColorGreen = $FF00FF00;
  ColorBlue = $FF0000FF;
  ColorBlack = 0;

implementation

function IntCoords(X,Y,W,H: integer) : TIntCoords; inline;
begin
  result.X := X;
  result.Y := Y;
  result.W := W;
  result.H := H;
end;

function IntSize(W, H : integer) : TIntSize; inline;
begin
  result.Width := W;
  result.Height := H;
end;

function StrToColor(const S: string): Cardinal;
const
  Colors: array[0..4] of record
    Name: string;
    Color: Cardinal;
  end = (
    (Name: 'white'; Color: ColorWhite),
    (Name: 'red'; Color: ColorRed),
    (Name: 'green'; Color: ColorGreen),
    (Name: 'blue'; Color: ColorBlue),
    (Name: 'black'; Color: ColorBlack));
var
  Dummy, i: Integer;
begin
  Result:=ColorWhite;
  if S='' then Exit;
  if S[1]='$' then
  begin
    Val(S, Result, Dummy);
    Exit;
  end;
  for i:=0 to High(Colors) do
    if Colors[i].Name=S then
    begin
      Result:=Colors[i].Color;
      Exit;
    end;
end;

function StrToIntCoords(S: string): TIntCoords;
var
  X, Y, W, H: Integer;
begin
  Result:=IntCoords(0, 0, 0, 0);
  if S='' then Exit;
  X:=StrToInt(Trim(Tok(',', S)));
  Y:=StrToInt(Trim(Tok(',', S)));
  W:=StrToInt(Trim(Tok(',', S)));
  H:=StrToInt(Trim(Tok(',', S)));
  Result:=IntCoords(X, Y, W, H);
end;

function StrToPoint(S: string): TPoint;
var
  X, Y: Integer;
begin
  Result:=Point(0, 0);
  if S='' then Exit;
  X:=StrToInt(Trim(Tok(',', S)));
  Y:=StrToInt(Trim(Tok(',', S)));
  Result:=Point(X, Y);
end;


function Tok(const Sep: string; var S: string): string;

  function IsOneOf(C: Char; const S: string): Boolean;
  var
    i: integer;
  begin
    Result:=false;
    for i:=1 to Length(S) do
    begin
      if C=Copy(S, i, 1) then
      begin
        Result:=true;
        Exit;
      end;
    end;
  end;

var
  C: Char;
begin
  Result:='';
  if S='' then Exit;
  C:=S[1];
  while IsOneOf(C, Sep) do
  begin
    Delete(S, 1, 1);
    if S='' then Exit;
    C:=S[1];
  end;
  while (not IsOneOf(C, Sep)) and (S<>'') do
  begin
    Result:=Result+C;
    Delete(S, 1, 1);
    if S='' then Exit;
    C:=S[1];
  end;
end;

end.
