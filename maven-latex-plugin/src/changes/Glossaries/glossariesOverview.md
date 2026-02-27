<!-- markdownlint-disable no-trailing-spaces -->
<!-- markdownlint-disable no-inline-html -->

# On Glossaries 

This document is an excerpt of N Talbots [article](https://www.dickimaw-books.com/latex/auxglossaries/) 
on invoking `makeglossaries`, `bib2gls` and other auxiliary programs 
written for **document build tool developers**, like me. :)

Besides this introduction, this folder contains the example latex files. 

## No glossaries 

Here, the file [noGlossaries.tex](./noGlossaries.tex) is compiled with `lualatex` 
and the [aux-file](./noGlossaries.aux) contains really the lines 

```
\providecommand\@newglossary[4]{}
\@newglossary{main}{glg}{gls}{glo}
\@newglossary{acronym}{alg}{acr}{acn}
\@newglossary{symbols}{slg}{sls}{slo}
```

The resulting PDF contains no glossary and also invocation of `makeglossaries` or `bib2gls` fails: 

- `makeglossaries noGlossaries` yields `No \@istfilename found in 'noGlossaries.aux'.`
- `bib2gls noGlossaries` yields `Error: Missing \glsxtr@resource in aux file. ` and 
  `(Have you used \glsxtrresourcefile or \GlsXtrLoadResources?)`

So, `\@istfilename` is required for `makeglossaries` and `\glsxtr@resource` is required for `bib2gls` 
Both commands shall be in the AUX file. 
Note that `bib2gls` even tells what to insert into the TEX file to get the missing `\glsxtr@resource`. 

N. Talbot summarizes: 

    The presence of \@newglossary in the aux file, 
    doesn’t automatically mean that an external tool is required.

True. 

Next, let us consider the file [noGlossariesExtra.tex](./noGlossariesExtra.tex). 
From [noGlossaries.tex](./noGlossaries.tex) it deviates in two lines: 

| noGlossaries.tex                         | noGlossariesExtra.tex                          | line |
| ---------------------------------------- | ---------------------------------------------- | ---- |
| % lualatex noGlossaries                  | % lualatex noGlossariesExtra                   |    4 |
| \usepackage[acronym,symbols]{glossaries} | \usepackage[acronym,symbols]{glossaries-extra} |    6 |
|                                          | \setabbreviationstyle[acronym]{long-short}     |   16 |

In words: 

- Comment: invocation with respective filename 
  But what remains the same: compilation with `lualatex` without further auxiliary tool. 
- The package `glossaries` is replaced by `glossaries-extra`. 
- `glossaries-extra` requires an additional command setting the abbreviation style. 

As mentioned above, compilation is again with `lualatex` 
and the AUX file looks the same, except the additional line 

```
\providecommand\@glsxtr@savepreloctag[2]{}
```

Still, the resulting PDF contains no glossary and also invocation of `makeglossaries` or `bib2gls` fails for the same reasons. 

## Glossaries without auxiliary tool 


Now let us create [glossariesExtra.tex](./glossariesExtra.tex) 
which deviates from [noGlossariesExtra.tex](./noGlossariesExtra.tex) in the following details: 

| noGlossariesExtra.tex                                | glossariesExtra.tex                                       | line |
| ---------------------------------------------------- | --------------------------------------------------------- | ---- |
| % lualatex noGlossariesExtra                         | % lualatex glossariesExtra                                |    4 |
| \usepackage[acronym,symbols-extra]{glossaries-extra} | \usepackage[acronym,symbols,style=tree]{glossaries-extra} |    6 |
|                                                      | \printunsrtglossaries                                     |   37 |

The major difference is the additional `\printunsrtglossaries` command. 

In the according AUX file, this causes the following 3 additional lines: 

```
\@writefile{toc}{\contentsline {section}{Glossary}{1}{}\protected@file@percent }
\@writefile{toc}{\contentsline {section}{Acronyms}{1}{}\protected@file@percent }
\@writefile{toc}{\contentsline {section}{Symbols}{1}{}\protected@file@percent }
```

And indeed, in the PDF file the according 3 sections occur, 
each containing a kind of glossary. 
They would also occur in a TOC if present. 

CAUTION: **The glossaries are not sorted**. 

Accordingly, let us create [glossaries.tex](./glossaries.tex) 
which deviates from [noGlossaries.tex](./noGlossaries.tex) in the following details: 

| noGlossaries.tex                               | glossaries.tex                                      | line |
| ---------------------------------------------- | --------------------------------------------------- | ---- |
| % lualatex noGlossaries                        | % lualatex glossaries                               |    4 |
| \usepackage[acronym,symbols-extra]{glossaries} | \usepackage[acronym,symbols,style=tree]{glossaries} |    6 |
|                                                | \makenoidxglossaries                                |    8 |
|                                                | \printnoidxglossary                                 |   37 |
|                                                | \printnoidxglossary[type=acronym]                   |   38 |
|                                                | \printnoidxglossary[type=symbols,sort=use]          |   39 |

The major differences are the additional 

- `\makenoidxglossaries` command, I pressume inititates writing glossary information into the AUX file
- `\printnoidxglossary` commands extracting the various glossaries from the AUX file into the PDF file 
  in a second `lualatex` run. 

In the according AUX file, this causes the following additional lines: 

```
\providecommand\@gls@reference[3]{}
\@gls@reference{main}{sample}{\glsnoidxdisplayloc{}{page}{glsnumberformat}{1}}
\@gls@reference{acronym}{ex}{\glsnoidxdisplayloc{}{page}{glsnumberformat}{1}}
\@gls@reference{symbols}{dfx}{\glsnoidxdisplayloc{}{page}{glsnumberformat}{1}}
\@gls@reference{acronym}{abbr}{\glsnoidxdisplayloc{}{page}{glsnumberformat}{1}}
\@gls@reference{acronym}{ex}{\glsnoidxdisplayloc{}{page}{glsnumberformat}{1}}
\@gls@reference{acronym}{abbr}{\glsnoidxdisplayloc{}{page}{glsnumberformat}{1}}
```

Unlike for the above example [glossariesExtra.tex](./glossariesExtra.tex), 
here no sections are written, but the entries of the glossaries themselves. 

Whereas in the first compiler run, the AUX file is written, 
only in the second run, the glossary is written from the AUX file into the PDF file. 
The advantage is, that it is sorted in this case. 

I wonder how the entries get sorted without auxiliary tool like `makeglossaries`. 


## Glossaries with auxiliary tool `makeglossaries` 

The tool `makeglossaires` internally invokes either `makeindex` or `xindy`, 
where the first is the default. 

Accordingly, let us create [glossariesMake.tex](./glossariesmake.tex) 
which deviates from [glossaries.tex](./glossaries.tex) in the following details: 

| glossaries.tex                             | glossariesMake.tex                     | line |
| ------------------------------------------ | -------------------------------------- | ---- |
| % lualatex glossaries                      | % lualatex glossariesMake              |    4 |
| \makenoidxglossaries                       | \makeglossaries                        |    8 |
| \newglossaryentry{fx}{...,}                | \newglossaryentry{fx}{...,sort={fx}}   |   23 |
| \newglossaryentry{dfx}{...,}               | \newglossaryentry{dfx}{...,sort={f'x}} |   23 |
| \printnoidxglossary                        | \printglossary                         |   37 |
| \printnoidxglossary[type=acronym]          | \printglossary[type=acronym]           |   38 |
| \printnoidxglossary[yype=symbols,sort=use] | \printglossary[type=symbols]           |   39 |

Essentially `\makexxxglossaries` and `\printxxxglossary` changed 
and with the second variant additional sorting option is available. 

Invoking `lualatex` unveils that besides `glo` also `aco` and `slo` files may be generated. 
In `makeglossaries`' user manual, Section 2.1 the option `nomain` is described 
which suppresses creation of generation of the main glossary and so file `glo`. 
The auxiliary tool `makeglossaries` transforms this into output file `gls` and log file `glg` 
which are of course suppressed also. 
In this case some other glossary must be specified 
by according options 


| option   | acronym(s) | symbols | numbers | index |
| -------- | ---------- | ------- | ------- | ----- |
| pkg      | acn        | slo     | nlo     | idx   |
| tool out | acr        | sls     | nls     | ind   |
| tool log | alg        | slg     | nlg     | ilg   |

So I suspect, just watching out for `gls` as done presently, 
is not sufficient to find out whether to invoke `makeglossaries`. 
In fact, the file endings which may occur is an open list, 
because arbitrary new glossaries may be created in the way, e.g. acronyms are created via 

```
\newglossary[alg]{acronym}{acr}{acn}{\acronymname}
```

Thus, this builder shall be restricted to kinds of glossaries 
created by an option to package `glossaries`. 

What is created in any case is an `ist` file by default or an `xdy` file for option `xindy`, 
which is described below. 


Compared to the AUX file of `glossaries.tex`, which contains `\rovidecommand\@gls` and `\@gls@reference` directly, 
these entries are no longer present. 
Instead, we read the following: 

```
\providecommand\@newglossary[4]{}
\@newglossary{main}{glg}{gls}{glo}
\@newglossary{acronym}{alg}{acr}{acn}
\@newglossary{symbols}{slg}{sls}{slo}
\providecommand\@glsorder[1]{}
\providecommand\@istfilename[1]{}
\@istfilename{glossariesMake.ist}
\@glsorder{word}
```

The `\@newglossary` parts are the same, but we observe, there is `\@istfilename{glossariesMake.ist}`. 

A PDF file with correctly sorted glossary is created by the sequence 

```
lualatex glossariesMake
makeglossaires glossariesMake
lualatex glossariesMake
```

Be aware which tool creates which file. 

Note that the cleanup of our software must be extended 
according to the settings of this [.latexmkrc](./.latexmkrc). 

Next let us consider [glossariesXindy.tex](./glossariesXindy.tex)
which deviates from [glossariesMake.tex](./glossariesmake.tex) essentially in a single line only: 

| glossariesMake.tex           | glossariesXindy.tex                | line |
| ---------------------------- | ---------------------------------- | ---- |
| % lualatex glossariesMake    | % lualatex glossariesXindy         |    4 |
| \usepackage[...]{glossaries} | \usepackage[...,xindy]{glossaries} |    6 |

As for `glossariesMake.tex` invoking `lualatex` creates files `gls` also `acr` and `sls` 
but instead of `ist` is created a `xdy` file. 

Well, the rest looks the same. 

Let us compare the AUX files: 

What is specific for the `xindy` created aux file is immaterial: 

```
\providecommand\@xdylanguage[2]{}
\@xdylanguage{main}{english}
\providecommand\@gls@codepage[2]{}
\@gls@codepage{main}{utf8}
\providecommand\@xdylanguage[2]{}
\@xdylanguage{acronym}{english}
\providecommand\@gls@codepage[2]{}
\@gls@codepage{acronym}{utf8}
\providecommand\@xdylanguage[2]{}
\@xdylanguage{symbols}{english}
\providecommand\@gls@codepage[2]{}
\@gls@codepage{symbols}{utf8}
```

What is the core difference is `\@istfilename{glossariesXindy.xdy}` 
versus `\@istfilename{glossariesMake.ist}`, 
i.e. it is the ending of the IST file (still called IST file for `xindy` despite ending `xdy`). 

## Hybrid: glossaries with and without auxiliary tools in one document 

This option is not supported by this builder software. 
We mention it only because it is considered in the source document (see top of this page). 

## Glossaries with auxiliary tool `bib2gls` 

Creation of glossaries using `bib2gls` is quite different from what was described above. 
First, the entries of the glossaries are not stored in a TEX file 
but in one or more BIB files. 
In our example, we fit all kinds of entries into a single BIB file [termAcroSymb.bib](./termAcroSymb.bib). 
The TEX file [glossariesBib2gls.tex](./glossariesBib2gls.tex) in contrast, 
does not contain these definitions but only a link on them via command `\GlsXtrLoadResources`. 
The TEX file must include `glossaries-extra`, but the options defining kinds of glossaries are the same. 
All the glossaries are printed via `\printunsrtglossaries`. 

Running `lualatex` on a TEX file using package `glossaries-extra` and specifying `\GlsXtrLoadResources` 
creates an AUX file like the following. 

```
\providecommand\@newglossary[4]{}
\@newglossary{main}{glg}{gls}{glo}
\@newglossary{acronym}{alg}{acr}{acn}
\@newglossary{symbols}{slg}{sls}{slo}
\providecommand\@glsxtr@savepreloctag[2]{}
\providecommand*{\glsxtr@fields}[1]{}
\providecommand*{\glsxtr@resource}[2]{}
\providecommand*{\glsxtr@pluralsuffixes}[4]{}
\providecommand*{\glsxtr@shortcutsval}[1]{}
\providecommand*{\glsxtr@linkprefix}[1]{}
\glsxtr@fields{{name}{name},{sort}{sortvalue},{type}{type},{first}{first},{firstplural}{firstpl},{text}{text},{plural}{plural},{description}{desc},{descriptionplural}{descplural},{symbol}{symbol},{symbolplural}{symbolplural},{user1}{useri},{user2}{userii},{user3}{useriii},{user4}{useriv},{user5}{userv},{user6}{uservi},{long}{long},{longplural}{longpl},{short}{short},{shortplural}{shortpl},{counter}{counter},{parent}{parent},{loclist}{loclist},{location}{location},{group}{group},{see}{see},{alias}{alias},{seealso}{seealso},{category}{category}}
\providecommand*{\glsxtr@record}[5]{}
\glsxtr@pluralsuffixes{s}{s}{s}{s}
\glsxtr@texencoding{utf8}
\glsxtr@shortcutsval{none}
\glsxtr@resource{src={termAcroSymb}}{glossariesBib2gls}
\providecommand{\@glsxtr@prefixlabellist}[1]{}
\@glsxtr@prefixlabellist{}
\glsxtr@linkprefix{glo:}
\providecommand {\@mfu@excls }[1]{}
\providecommand {\@mfu@blockers }[1]{}
\providecommand {\@mfu@mappings }[1]{}
...
\glsxtr@record{sample}{}{page}{glsnumberformat}{1}
\glsxtr@record{ex}{}{page}{glsnumberformat}{1}
\glsxtr@record{dfx}{}{page}{glsnumberformat}{1}
\glsxtr@record{abbr}{}{page}{glsnumberformat}{1}
\glsxtr@record{ex}{}{page}{glsnumberformat}{1}
\glsxtr@record{abbr}{}{page}{glsnumberformat}{1}
\@writefile{toc}{\contentsline {section}{Glossary}{1}{}\protected@file@percent }
\@writefile{toc}{\contentsline {section}{Acronyms}{1}{}\protected@file@percent }
\@writefile{toc}{\contentsline {section}{Symbols}{1}{}\protected@file@percent }
\@glsxtr@mglslike{mgls,mglspl,mglsmainpl,Mgls,Mglspl,Mglsmainpl,MGls,MGlspl,MGlsmainpl,MGLS,MGLSpl,MGLSmainpl,mglsshort,mglslong,mglsfull,Mglsshort,Mglslong,Mglsfull,mglsname,Mglsname,MGlsname,mglssymbol,Mglssymbol,MGlssymbol,mglsusefield,Mglsusefield,MGlsusefield,mpgls,mpglspl,mpglsmainpl,Mpgls,Mpglspl,Mpglsmainpl,MPGls,MPGlspl,MPGlsmainpl,MPGLS,MPGLSpl,MPGLSmainpl}
```

It contains an entry `\glsxtr@resource` one for each `\GlsXtrLoadResources` in the TEX file. 
If this is in the AUX file, then it is clear that `bib2gls` shall be invoked 
as `\@istfilename` for `makeglossaries`. 

Maybe there are good reasons to avoid occurrence of both. 

Invocation of `bib2gls glossariesBib2gls` 
creates `glossariesBib2gls.glstex` and the log file `glossariesBib2gls.glg`. 

Next invocation of lualatex does not create more files, 
but only inserts the glossaries' information stored in `glossariesBib2gls.glstex` into the PDF file. 

