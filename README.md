# File-and-Directory-Structure-Comparison

A commandline written in Clojure that compares two files or two nested directory structures. MD5 hashing is used to determine file equality.

## Install

    cd [directory where you have downloaded the project]

    lein uberjar

## Command Line Usage

### NAME

   fileComp.sh -- A file and directory structure comparison tool written in Clojure. Can work locally or remotly over ssh.

### SYNOPSIS
   fileComp.sh [options] path1 path2

### EXAMPLES

   local file comparison:
      fileComp.sh treestructure/tree1 treestructure/tree2
   ssh file comparison:
      fileComp.sh --identity /home/username/.ssh/id_rsa treestructure/tree1 ssh://user@hostname:/tmp/treestructure/tree2
### Usage:

 Switches               Default        Desc                
 --------               -------        ----                
 -p, --port             22             not implemented yet 
 -h, --no-help, --help  false          Show help           
 -i, --identity         ~/.ssh/id_rsa  ssh key is required comparison over ssh                    

## Code Usage

The top level namespace is `net.matlux.filecomparator.app`

    (use 'net.matlux.filecomparator.app)

local example:

    (diff-targets {:proto "local" :path "treestructure/tree1/"} {:proto "local" :path "treestructure/tree2/"})
    
ssh example:    

    (diff-targets {:proto "local" :path "treestructure/tree1/"} 
                  {:user nil, :proto "ssh" :hostname "192.168.1.1" 
                   :port 22 :identity "~/.ssh/id_rsa" :path "/tmp/tree2/"})

## License

Copyright (C) 2012 Mathieu Gauthron

Distributed under the Eclipse Public License, the same as Clojure.
