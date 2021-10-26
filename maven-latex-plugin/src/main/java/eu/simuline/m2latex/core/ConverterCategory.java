package eu.simuline.m2latex.core;

// TBD: make complete
// TBD: specify what a category is: options, invocation, file interface 
/**
 * This enum represents categores of converters. 
 * Two converters are in the same category,
 * if they can used in the same command setting in {@link Settings} 
 * and, if used as a maven plugin equivalently,
 * a command setting in the configuration in the pom.
 * So in a sense the category represents the commands settings. 
 * Consequently, each category has a method {@link #getFieldname()}
 * returning the according field in the settings. 
 *
 */
// TBD: take advantage of the fact,
//that most entries are related to the fieldname: 
// lowercase and add 'Command'. 
enum ConverterCategory {
    // LaTeX: mostly to pdf but also to dvi 
    // This is slightly inconsistent. 
    LaTeX() {
	String getFieldname() {
	    return "latex2pdfCommand";
	}
    },
    // TBD: clarify: not yet used really 
    Latex2Html() {
	String getFieldname() {
	    throw new UnsupportedOperationException();
	}
    },
    // TBD: check: not used. 
//    LaTeX2Odt() {
//	String getFieldname() {
//	    throw new UnsupportedOperationException();
//	}
//    },
    LaTeX2Rtf() {
	String getFieldname() {
	    return "latex2rtfCommand";
	}
    },
    BibTeX() {
	String getFieldname() {
	    return "bibtexCommand";
	}
    },
    MakeIndex() {
	String getFieldname() {
	    return "makeIndexCommand";
	}
    },
    MakeGlossaries() {
	String getFieldname() {
	    return "makeGlossariesCommand";
	}
    },
    SplitIndex() {
	String getFieldname() {
	    return "splitIndexCommand";
	}
    },
    LatexChk() {
	String getFieldname() {
	    return "chkTexCommand";
	}
    },
    Dvi2Pdf() {
	String getFieldname() {
	    return "dvi2pdfCommand";
	}
    },
    MetaPost() {
	String getFieldname() {
	    return "metapostCommand";
	}
    },
    Svg2Dev() {
	String getFieldname() {
	    return "svg2devCommand";
	}
    },
    Fig2Dev() {
	String getFieldname() {
	    return "fig2devCommand";
	}
    },
    Gnuplot2Dev() {
	String getFieldname() {
	    return "gnuplotCommand";
	}
    },
    Odt2Doc() {
	String getFieldname() {
	    return "odt2docCommand";
	}
    },
    Pdf2Txt() {
	String getFieldname() {
	    return "pdf2txtCommand";
	}
    },
    EbbCmd() {
	String getFieldname() {
	    return "ebbCommand";
	}
    },

    // Those are not usable 
    // TBD: clarify: For latex to html/odt 
    // dvips: for conversion to ps??
    // gs
    // upmendex: right interface? 
    // xindy 
    Unspecific {
	String getFieldname() {
	    throw new UnsupportedOperationException();
	}
    };

    // may throw UnsupportedOperationException
    abstract String getFieldname();
    
}
