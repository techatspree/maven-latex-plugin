<!-- markdownlint-disable no-trailing-spaces -->
<!-- markdownlint-disable no-inline-html -->

# Features

- Transform LaTeX documents into PDF and/or HTML 
  during the maven site phase by calling the standard LaTeX scripts
- Find TeX main documents underneath a TEX directory 
- in the maven site (e.g. `src/site/tex/doc/Doc.tex`)
- Goal to create PDF or DVI files in the target folder, 
  e.g. `target/site/doc/Doc.pdf`
- Goal to create HTML files using tex4ht in the target folder, 
  e.g. `target/site/doc/Doc.html`
- Configurable, for which level of sections 
  a separate HTML file will be created (multipage / single-page)
- Using CSS stylesheets.
- Dynamic execution of the latex operations 
  by analyzing log information created by the LaTeX scripts
- Execute latex as many times as needed
- Execute bibtex if needed

# Planned Features

- Execute `makeindex` if needed
- Execute `glosstex` if needed
- Usage of the `multibib` macros