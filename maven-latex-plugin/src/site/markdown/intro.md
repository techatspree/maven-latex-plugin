<!-- markdownlint-disable no-trailing-spaces -->
<!-- markdownlint-disable no-inline-html -->

# Latex Maven Plugin

This site is under construction.
Thus, we refer the reader
to the [development site](https://github.com/Reissner/maven-latex-plugin)
and in particularly the 'README.md' therein.
Check in particular the section on the maven plugin. 
For a really detailed description in contrast see 
[manual as a PDF](http://www.simuline.eu/LatexMavenPlugin/manualLMP.pdf).
Note that this manual is created by this `maven-latex-plugin`
and serves also as a test suite for the plugin. 
It is also available as an [HTML page](http://www.simuline.eu/LatexMavenPlugin/manualLMP.html), 
created from the same source, 
but as HMTL creation is not really mature and comprehensive enough, 
we still recommend the PDF version.

If you have problems applying this `maven-latex-plugin`
or feel a feature is missing, please don't hesitate
and [let me know](mailto:rei3ner@arcor.de). 

The `latex-maven-plugin` translates LaTeX documents into PDF,
HTML or other LaTeX output formats like DVI or Postscript,
during the maven site lifecycle.
Internally, the plugin calls the standard LaTeX macros,
like `lualatex` for PDF and tex4ht for HTML output.
Hence, a LaTeX distribution has to be installed on the
computer running the maven build.

By default, LaTeX documents and resources like graphics files
are assumed to reside in the maven project's
`src/site/tex` folder.
The resulting files will be in the folder `target/site/tex`,
making them a part of the maven site.
Thus, other maven site artifacts (like HTML pages build from apt files)
may link to them.

The `maven-latex-plugin` tries to be smart
by analyzing the LaTeX macros' output and running the macros as often
as needed (running LaTeX more than once is needed when 
e.g. building a table of contents or when using `bibtex`
to maintain a bibliography and citations). 

## Goals Overview 

Most of the goals are on creating output in a given format 

- `latex:cfg` creates output in the list of formats configured as a `target` 
  as shown in (example_targets.html)[]. 


