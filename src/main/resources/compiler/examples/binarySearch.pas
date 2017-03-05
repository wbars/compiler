program binarySearch;
type intArr = array of Integer;

var
    arr: intArr;
    mid,ilow,ihigh,search: Integer;
    i,n,fi, j: Integer;
    found: Boolean;

procedure printArray(a: intArr);
begin
    for i := 0 to (len(a) - 1) do begin
        write(a[i]);
        write(' ');
    end;
    writeln(' ');
    return;
end;

begin
    arr := {3, 5, 5};
    write('Array: ');
    printArray(arr);

    search := 8194;
    write('Search: ');
    writeln(search);

    ilow  := 0;
    ihigh := len(arr) - 1;
    found := false;
    repeat
        mid := (ilow + ihigh) / 2;
        if search = arr[mid] then begin
            found := true;
            fi := mid;
            break;
        end;
        if search < arr[mid] then
            ihigh := mid - 1
        else
            ilow := mid + 1;
    until ihigh < ilow;
    if found then begin
        write('Found element: ');
        writeln(fi);
    end
    else writeln('Element NOT found');
    return;
end.