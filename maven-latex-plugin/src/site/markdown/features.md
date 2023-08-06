<!-- markdownlint-disable no-trailing-spaces -->
<!-- markdownlint-disable no-inline-html -->

# Features

- Supports many output formats like PDF, DVI, HTML, DOCX, RTF, TXT and others. 
- Perform checks with `chktex` and logs the result. 
- Supports many graphical input formats like PNG, MP, FIG, gnuplot. 
  Also provides a separate goal creating them, `grp`. 
- Support of bibliography, index, glossary and embedded code. 
  In particular supports split index. 
- Performs version checks on used tools. 
- Perform checks on sources and results and log files. 
- Orchestration of various tools detecting need for execution 
  e.g. of `bibtex` including rerunfilecheck for `lualatex` and friends. 
- Supports document development, mainly by cooperating with editor, viewer 
  and with other tools in the build chain. 
  Offers [installation script](./fromMain/instVScode4tex.sh) 
  for extensions of VS Code. 
- Offers check whether a document could have been reproduced. 

# Planned Features

- Support `biber` replacing `bibtex` as preferred tool
- Support `xindy` replacing `makeindex` as preferred tool
- Support `bib2gls` replacing `makeglossary` as preferred tool
- Execute `glosstex` if needed
- Usage of the `multibib` macros
- ...

# Feature Requests 

Feature requests from users are always welcome: [write me](rei3ner@arcor.de). 
