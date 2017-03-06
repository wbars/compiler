program mergeSort;
type intArr = array of Integer;
var a: intArr;


procedure sortMerge(a,l,r: intArr);
var i,j,k: Integer;

begin
    i := 0;
    j := 0;
    k := 0;

    while ( (i < len(l)) && (j < len(r) ) ) do
     begin
        if (l[i] < r[j]) then begin
            array_push(l[i], k, a);
            i := i + 1;
            k := k + 1;
        end
        else begin
            array_push(r[j], k, a);
            j := j + 1;
            k:= k + 1;
         end;
        end;


     while (i < len(l)) do
     begin
        array_push(l[i], k, a);
        i := i + 1;
        k := k + 1;
     end;

     while (j < len(r)) do
          begin
             array_push(r[j], k, a);
             j := j + 1;
             k := k + 1;
          end;
return;
end;

procedure division(a: intArr);
var low, high, mid, p: Integer; l,r: intArr;

begin
    low := 0;
    high := len(a);
    p := 0;
    mid := (high + low) / 2;
    if (len(a) < 2) then return;
    l := new_array(mid, Integer);
    r := new_array((high - mid), Integer);

    for p := 0 to (len(l) - 1) do array_push(a[p], p, l);
    for q := 0 to (len(r) - 1) do begin
        array_push(a[p], q, r);
        p := p + 1;
    end;
    division(l);
    division(r);
    sortMerge(a, l, r);
return;
end;

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
a := {1,2,3};
write('Array before sorting: ');
printArray(a);
division(a);
write('Array after sorting: ');
printArray(a);
return;
end.