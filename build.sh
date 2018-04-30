#!/bin/bash
javac -Xlint:-deprecation -classpath ./compiler -d ./compiler ./compiler/compiler/*.java ./compiler/parser/*.java ./compiler/x64codegen/*.java ./compiler/x86codegen/*.java ./compiler/optimizer/*.java ./compiler/lowlevel/*.java ./compiler/dataflow/*.java
