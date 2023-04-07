# Ways to include metapost in latex

Classically, `mpost` is a separate application run before the latex run. 
This has the disadvantage that there is no way to pass information from the latex file 
into the mp file, e.g. definitions. 

Also this makes it difficult to keep up the dependency mechanism provided by FLS files. 
Note although `mpost` offers also a `-recorder` option to write its own FLS file. 

At least two packages provide solutions: the `gmp` package, the `emp` package 
which both work on all tex engines 
and the `luamplib` package which works on `lualatex` only. 

An alternative to all this is using `tikz`. 
This works also for all tex engines but I feel it is complicated and it is also much slower. 
Note also that lua can be integrated into metapost giving it a tremendous power. 
