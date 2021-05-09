package eu.simuline.m2latex.core;

// TBD: make complete
// TBD: specify what a category is: options, invocation, file interface 
enum ConverterCategory {
    // TBD: clarify: why not LaTeX2Dev?
LaTeX, Latex2Html, LaTeX2Odt, LaTeX2Rtf, 
BibTeX, MakeIndex, MakeGlossaries, SplitIndex, 

LatexChk, Dvi2Pdf, MetaPost, 
Svg2Dev, Fig2Dev, Gnuplot2Dev, 
Odt2Doc, Pdf2Txt, EbbCmd, 

// Those are not usable 
// TBD: clarify: For latex to html/odt 
// dvips: for conversion to ps??
// gs
// upmendex: right interface? 
// xindy 
Unspecific
}
