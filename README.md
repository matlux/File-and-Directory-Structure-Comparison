# File-and-Directory-Structure-Comparison

A commandline written in Clojure that compares two files or two nested directory structures. MD5 hashing is used to determine file equality.

## Install

    cd [directory where you have downloaded the project]

    lein uberjar

## Command Line Usage

Compare two files:

    java -jar cl-file-comparator-1.0.0-SNAPSHOT-standalone.jar treestructure/tree1/file-dif-2 treestructure/tree2/file-dif-2

Compare two directory structures:

    java -jar cl-file-comparator-1.0.0-SNAPSHOT-standalone.jar treestructure/tree1/ treestructure/tree2/

## Code Usage

The top level namespace is `net.matlux.filecomparator.core`

    (use 'net.matlux.filecomparator.core)

    (diff-file-dir "treestructure/tree1/" "treestructure/tree2/")

## License

Copyright (C) 20121 Mathieu Gauthron

Distributed under the Eclipse Public License, the same as Clojure.
