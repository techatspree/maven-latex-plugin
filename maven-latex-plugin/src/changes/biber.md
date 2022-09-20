<!-- markdownlint-disable no-trailing-spaces -->
<!-- markdownlint-disable no-inline-html -->

# Information to support `biber` and make it the default 

## Motivation 

The latex package `biblatex` offers a lot of important features over plain `latex` 
using the auxiliary program `bibtex` or that like (`bibtexu` and that like), 
e.g. back references, treatment of online sources and that like. 
The default bibliography handler for the latex package `biblatex` is no longer `bibtex`, 
although it is still available and can be configured, but the default is `biber`. 

This alone is reason enough to support `biber` and to make it the default also. 
There are other reasons, e.g. I tried to make the text for citations the label 
and this worked only with `biber`, not with `bibtex`. 
Also, the log file is much more transparent and there are many other reasons to use `biber`. 
Have a long look at the [`biblatex` manual](./../site/doc/PackagesWithTools/Bibliography/biblatex.pdf). 
Also, the corresponding [`biber` manual](./../site/doc/PackagesWithTools/Bibliography/Biber/biber.pdf) is available. 


TBD: Here references to the other subsections are missing. 

## Files written and read by `bibtex` and `biber`

This section also informs on how this plugin finds out that `bibtex` is used, 
and whether it is configured to use `biber` instead of `bibtex`. 

The following must be checked: 
  
- `biblatex` creates `jobname.run.xml` and `jobname-blx.bib`. 
  These two files indicate that in fact `biblatex` has been loaded: `\usepacakge[...]{biblatex}`. 
- The options may comprise `backend=biber` which is the default or `backend=bibtex`. 
  Backend `biber` is recognized because a `bcf` (`biber` config file) is created. 

Additional information: 
we wanted that the labels of citations occur in the citation text. 
This is done as follows: 

```[tex]
\usepackage[backend=bibtex,style=alphabetic,sorting=debug]{biblatex}
\DeclareFieldFormat{labelalpha}{\thefield{entrykey}}
\DeclareFieldFormat{extraalpha}{}
\addbibresource{bib.bib}
\nocite{*}
```

When switching to `bibtex` one has also to replace 

```[tex]
%\bibliographystyle{alpha}% replaced essentially by options of biblatex
%\bibliography{bib} % replaced by \printbibliography
\printbibliography
```


## Shape of blg files 

The ending for bibliography log files is `blg`, 
used by both `bibtex` and `biber`, 
but the shape is completely different. 
The program `biber` writes logs on standard output and the same into the log file. 

What follows is just an example. 

```[xx]
INFO - This is Biber 2.14
INFO - Logfile is 'mainsFilterAndNorms.blg'
INFO - Reading 'mainsFilterAndNorms.bcf'
INFO - Using all citekeys in bib section 0
INFO - Processing section 0
INFO - Globbing data source 'bib.bib'
INFO - Globbed data source 'bib.bib' to bib.bib
INFO - Looking for bibtex format file 'bib.bib' for section 0
INFO - LaTeX decoding ...
INFO - Found BibTeX data source 'bib.bib'
INFO - Overriding locale 'de-DE' defaults 'normalization = NFD' with 'normalization = prenormalized'
INFO - Overriding locale 'de-DE' defaults 'variable = shifted' with 'variable = non-ignorable'
INFO - Sorting list 'anyt/global//global/global' of type 'entry' with template 'anyt' and locale 'de-DE'
INFO - No sort tailoring available for locale 'de-DE'
Use of uninitialized value $outtype in concatenation (.) or string at C:\Users\ERNST~1.REI\AppData\Local\Temp\par-65726e73742e72656973736e6572\cache-3fdda9ab9b6106ff01fe44df85bbc368a2e8d9e0\inc\lib/Biber/Output/bbl.pm line 221.
INFO - Writing 'mainsFilterAndNorms.bbl' with encoding 'UTF-8'
INFO - Output to mainsFilterAndNorms.bbl
```

Seemingly, in general, one message is one line. 
Lines start with the category of the message, here only `INFO` separated by ` - ` from the proper description. 
Nevertheless, one must be prepared to have another kind of message 
which shall be treated as an error. 

Besides `INFO` we observed also `WARN` and `ERROR` as is illustrated by the following log file: 

```[x]
INFO - This is Biber 2.14
INFO - Logfile is 'mainsFilterAndNorms.blg'
INFO - Reading 'mainsFilterAndNorms.bcf'
INFO - Using all citekeys in bib section 0
INFO - Processing section 0
INFO - Globbing data source 'bib.bib'
INFO - Globbed data source 'bib.bib' to bib.bib
INFO - Looking for bibtex format file 'bib.bib' for section 0
INFO - LaTeX decoding ...
INFO - Found BibTeX data source 'bib.bib'
ERROR - BibTeX subsystem: C:\Users\ERNST~1.REI\AppData\Local\Temp\biber_tmp__hLO\bib.bib_21248.utf8, line 130, syntax error: found "key", expected "="
INFO - ERRORS: 1
```

More precisely, per line the following information is given: 
- version of biber. The first line indicates that this log file is written by `biber`
  and not, e.g. by `bibtex`. 
- Names of the log file `blg` is written and that `bcf` 
  (`biber` configuration file) file is read, ... 
- Further pieces of information 
- If an error (maybe warning also) occurred, this is given as a message. 
- If an output file `bbl` is written, the user is notified, the name is given and the endocing. 
  If this is missing no output is written. 
