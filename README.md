Simple Pascal-like language to JVM bytecode compiler.

See usage in run.sh file

Main differences from classic Pascal:

- No subrange bounds in array types (write just `array of Integer`)
- Array literals `{1,2,3}`
- `new_array(size: Integer; type: Type)` for allocating empty array with size
- `array_push(index: Integer; value: T, arrayReference: Array)` instead of assignment by index

**Disclaimer**: since this project was made only for self-learning and from curiosity there is very basic Pascal features support 
and some of these can be *suddenly* disabled (like comments).
