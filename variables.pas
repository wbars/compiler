program variables;

var
    a,b,c: Integer;

procedure printVars(a,b,c: Integer);
begin
    write('a: ');
    writeln(a);

    write('b: ');
    writeln(b);

    write('c: ');
    writeln(c);
end;

begin
    a := 11;
    b := 22;
    c := 33;
    printVars(a, b, c);
    writeln(' ');

    writeln('a := b');
    a := b;
    printVars(a, b, c);
    writeln(' ');

    writeln('b := c');
    b := c;
    printVars(a, b, c);
end.