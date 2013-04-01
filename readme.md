LW - A small didactic (toy) imperative language.
======

LW is a small language used in some universities to teach the basics of imperative languages, and the concepts of static analysis, basic parsing, handling scope, what a state is, the difference between an expression and a command, and how to walk an AST tree to do static checks and to run the program.

I did this compact LW (and it's extension, LWPlus) interpreter at uni in 2008. Funnily enough, I couldn't find recent references to LW, so actually I had to reverse engineer the semantics of the language and some examples reading my very old undocumented code.

The language supports one type only (ints), it supports normal C scoping rules, while loops with continue. Programs read data from a file, and print out one or more values. There is no support function definition / invocation.

There are two neat things about this: how compact the whole thing is (4 pages of code), and that the grammar for the language is actually not baked in the interpreter. Rather, it's read at runtime from a grammar file which is almost in BNF form, so one could tweak that a little, I thought that was quite cool, at the time :-). The semantics of the static checks and execution are baked in the interpreter though.

To build the interpreter:
```
javac LWInterpreter.java
```
Usage:
```
java LW grammarFileName progSourceName maxMem dataFile
```
To run the examples program:
```
java LW LW.grammar examples/LW/minimal.lw 20 examples/data/nodata.data
java LW LW.grammar examples/LW/basic.lw 20 examples/data/zero.data
java LW LW.grammar examples/LW/factorial.lw 20 examples/data/one.data
java LW LW.grammar examples/LW/factorial.lw 20 examples/data/two.data
java LW LW.grammar examples/LW/factorial.lw 20 examples/data/three.data
java LW LW.grammar examples/LW/factorial.lw 20 examples/data/four.data

java LW LWPlus.grammar examples/LWPlus/minimal.lwplus 20 examples/data/nodata.data
java LW LWPlus.grammar examples/LWPlus/basic.lwplus 20 examples/data/nodata.data
java LW LWPlus.grammar examples/LWPlus/plus.lwplus 20 examples/data/one-one.data
java LW LWPlus.grammar examples/LWPlus/factorial.lwplus 20 examples/data/one.data
java LW LWPlus.grammar examples/LWPlus/factorial.lwplus 20 examples/data/two.data
java LW LWPlus.grammar examples/LWPlus/factorial.lwplus 20 examples/data/three.data
java LW LWPlus.grammar examples/LWPlus/factorial.lwplus 20 examples/data/four.data
```

Example program (in LWPlus) to calculate factorial:
```
do
  with PARTIAL begin
    with COUNTER begin
      read COUNTER ;
      PARTIAL := 1 ;
      while COUNTER <> 1 do
        PARTIAL := prod PARTIAL COUNTER ;
        COUNTER := sum COUNTER -1
      endw ;
      write PARTIAL
    end
  end
endp
```

