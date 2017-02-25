#!/usr/bin/env bash

echo "Simple arithmetic operations..."
./gradlew run -PappArgs="['calculator.pas']" > /dev/null
java -noverify Calculator
echo "----------------------------------------"

echo "Variables management..."
./gradlew run -PappArgs="['variables.pas']" > /dev/null
java -noverify Variables
echo "----------------------------------------"

echo "Factorial..."
./gradlew run -PappArgs="['factorial.pas']" > /dev/null
java -noverify Factorial
echo "----------------------------------------"

echo "Binary search..."
./gradlew run -PappArgs="['binarySearch.pas']" > /dev/null
java -noverify BinarySearch
echo "----------------------------------------"

echo "Merge sort..."
./gradlew run -PappArgs="['mergeSort.pas']" > /dev/null
java -noverify MergeSort