<!-- markdownlint-disable no-trailing-spaces -->
<!-- markdownlint-disable no-inline-html -->

# How to determine the latex main file 

The latex main file(s) are those which shall be processed by a latex processor. 
Some call these the root. 

At time of this writing, 
the [current concept](#ssCurrConc) to determine the main file 
is via specifying a regular expression `patternLatexMainFile` in the settings. 

This pattern is becoming more and more complex including more and more cases 
as we gain knowledge but also as the latex ecosystem grows. 
Of course the user can modify this and is not restricted on the default value, 
but no pattern we can provide [inherently](#ssFalsePosNeg) 
fits exactly the latex main files; there will be always false positives and false negatives. 
The need to adapt the pattern to the current situation makes it poorely maintainable. 
Another, maybe more critical aspect is, 
that the design goal to notify the user of any defect of the artifact, 
except misconfiguration. 
This means that if no warning pops up, the artifact is perfect. 
If the user relies on the default pattern, 
it cannot be excluded that a latex main file is not recognized 
and consequently is not compiled. 
Less treacherous but still very annoying is false positive, 
i.e. a latex file identified as main file although it is none. 
Typically this will not compile; if it does then an unwanted artifact occurs. 

So it is more appropriate to exclude than to include tex files from being main files. 
A mechanism along those lines is the magic comment: 

```[tex]
#!tex base=file
```

If a tex file starts like that, it is **no** main file. 
There are other mechanisms like explicit excludes. 

Since we design this software is likely to be used with some developing environment, 
we shall take the solutions into account offered by those environments. 
In fact, the solution above with magic comments 
is used in TeXworks (cited as {TEXworks}, see in particular the index of the 
[manual](../../site/doc/ToolsWithoutPackages/DevEnvironments/TeXWorks.pdf)) 
and also in James Yu's [LaTeX-Workshop](https://github.com/James-Yu/LaTeX-Workshop) 
which is an extension which enables vscode for LaTeX. 
The latter is particularly important as it is the preferred developing environment. 
Thus LaTeX-Workshop's main file detection mechanism is treated [separately](#ssLatexWorkshop)
An aspect also mentioned in the LaTeX-Workshop documentation 
is the `subfiles` package (cited as {SubfilesP}). 
As this contributes considerable to document development, 
we shall provide [support for subfiles](#ssSubfileP). 

Taking all this into account we come to the [new solution](#the-new-solution). 
<!-- ssNewSolution -->





## The current solution: main file by regular expression <a id='ssCurrConc'></a>



## Criticism: false negatives and false positives <a id='ssFalsePosNeg'></a>

One reason why the approach to identify a latex main file via a regular expression 
is never complete, is indicated in the following latex file: 

```[latex]
\newcommand{\hello}{world}
\documentclass{article}

\begin{document}
Hello \hello.
\end{document}
```

This really compiles and indicates that `\newcommand` may preceed `\documentclass`. 
Since the depth of stacked braces is potentially infinite, 
a regular expression can never detect all command definitions. 

Moreover, this file is a latex main file `presentation.tex`

```[latex]
\RequirePackage{etoolbox}
\newbool{isPresentation}
\setbool{isPresentation}{true}% true for presentation; false for handout 
\input{presentationOrHandout}
```

although not containing any `\documentclass` declaration. 
This comes in the following file `presentationOrHandout.tex` 

```[latex]
ifbool{isPresentation}{%
\documentclass[ignorenonframetext]{beamer}%uncomment this for presentation
\mode<presentation>%
}{%
\documentclass{article}%uncomment this for handout
\usepackage{beamerarticle}
}
```

which is not a latex main file although containing even two `\documentclass` declarations. 

The explanation to this miracle is that the `beamer` class 
allows to create a presentation `presentation.tex` and a handout 
from the same source `presentationOrHandout.tex`. 

So we have examples for latex main files not recognized as such 
but also examples for latex files identified as main files although they are not. 


## LaTeX-Workshop's main file detection  <a id='ssLatexWorkshop'></a>

LaTeX-Workshop's latex main file detection is described 
in the context of [multi-file-projects](https://github.com/James-Yu/LaTeX-Workshop/wiki/Compile#multi-file-projects). 

Here a sequence of steps is presented too determine the main file (there called root file)

- Magic comment `% !tex root=` 
  We shall allow a less resctrited pattern: `\w*%\w*![Tt][Ee][Xx]\w+ root` 
  without `=`. 
  This shall be the first line. 
  Note that magic comments are disabled by default. 
  It is advisable to activate to be in line with this software. 

- Self check: `\documentclass[...]{...}` (the `[...]` is optional), it is set as root.
  This somehow contradicts the support of the `subfiles` package described below. 
  Also, `\documentclass` may occur even when talking about document classes. 
  Also that pattern may occur quite late in the file and preceeded by a lot of material 
  which can not always be detected with regular expressions. 

  So this is no good idea. 
  In fact, this is an idea, generalized by the current implementation 
  with the parameter `patternLatexMainFile` set to a very specific value. 

  Good is the idea of narrowing: includes and excludes. 
  In contrast, we shall not use folders but regular expressions on file names. 
  An empty include reprents all and is the default; 
  also for excludes the default shall be empty. 

- Subfiles package: The difference between LaTeX workshop and this software is, 
  that we don't treat interactive builds. 
  LaTeX workshop has a parameter `latex-workshop.latex.rootFile.useSubFile` 
  whether to use subfile. 
  We in contrast shall always clean files created when processing subfiles, 
  but our parameter shall determine whether we build subfiles. 
  Default shall be false. 


## The `subfile` package <a id='ssSubfileP'></a>

As described in the package documentation, 
the main file is expected to have the form 

```[latex]
\documentclass[...]{...}
〈shared preamble〉
\usepackage{subfiles}
\begin{document}
. . .
\subfile{〈subfile name〉}
. . .
\end{document}
```
whereas each subfile (included nested subfiles) 
have the form 

```[latex]
\documentclass[〈main file name〉]{subfiles}
\begin{document}
. . .
\end{document}
```

For subfiles one shall relax the form a bit, 
maybe 

```[latex]
% optional comment lines 
\documentclass[〈main file name〉]{subfiles}
\begin{document}
. . .
\end{document}
```

The suggestion is to implement this as a regular expression 
identifying subfiles. 
This would be a new parameter in settings. 
It is not just a pattern like 


```[tex]
#!tex base=file
```

to identify non-main files, 
but it is something intermediate: 
For cleaning, it must be treated as a main file, 
whereas for creational goals it shall not. 

It does not make sense 
to require that subfiles start with a magic comment like `#!tex base=file`. 

Of course, the form of the latex main file 
provided in the documentation of the `subfile` package 
is too restrictive. 


## The new solution <a id='ssNewSolution'></a>

Taking LaTeX workshop, the `subfile` package and our design goals into account, 
the clue seems to be to rely more on excluding tex files from being main files 
than including. 
Essentially we follow the ideas in LaTeX workshop with some modifications. 

- we exclude latex main files by parameter. 
  If empty, which is the default, nothing is excluded. 
- we include latex main files from those excluded. 
  The default is again empty. 
- subfiles are identified by a pattern (details see above). 
  They are taken into account when cleaning always, 
  and if the parameter `buildSubfiles` in the settings is set (default not set), 
  they are included. 
- From the remaining latex files, 
  we honor magic comments, essentially `#!TEX root` (details see above). 
  All those with root are excluded. 
  Maybe waring if they are their own root. 

After build with recorder is supported: 
A tex file shall either be a main file or shall occur in an fls file. 
If neither is the case, then most probably, 
the according file is a main file and is not recognized. 
In this case, a warning shall be issued. 


