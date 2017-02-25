program factorial;

function fac(n: Integer): Integer;
begin
    if n = 0 then return 1;
    return (n * fac(n - 1));
end;

begin
    write('Factorial of 0: ');
    writeln(fac(0));
    writeln(' ');

    write('Factorial of 3: ');
    writeln(fac(3));
    writeln(' ');

    write('Factorial of 10: ');
    writeln(fac(10));
    writeln(' ');
end.
