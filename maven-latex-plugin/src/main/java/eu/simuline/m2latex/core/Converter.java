package eu.simuline.m2latex.core;

import java.util.HashMap;
import java.util.Map;

enum Converter {
    PdfLatex {
	String getCommand() {
	    return "pdflatex";
	}
	String getVersionPattern() {
	   return "^pdfTeX ([0-9\\.-]*) \\(TeX Live";
	}
    },
    LuaLatex {
	String getCommand() {
	    return "lualatex";
	}
	String getVersionPattern() {
	    return "^This is LuaHBTeX, Version ([0-9\\.]*) \\(TeX Live";
	}
    },
    XeLatex {
	String getCommand() {
	    return "xelatex";
	}
	String getVersionPattern() {
	    return "^XeTeX ([0-9\\.-]*) \\(TeX Live";
	}
    },
    Latex2rtf {
	String getCommand() {
	    return "latex2rtf";
	}
	String getVersionPattern() {
	    return "^latex2rtf ([0-9\\.]* r[0-9]+) \\(released";
	}
    },
    Odt2doc {
	String getCommand() {
	    return "odt2doc";
	}
	String getVersionOption() {
	    return "--version";
	}
	// TBC: not clear whether this is the significant version 
	String getVersionPattern() {
	    return "^unoconv ([0-9\\.]*)\n";
	}
    },
    Pdf2txt {
	String getCommand() {
	    return "pdftotext";
	}
	String getVersionPattern() {
	    return "^pdftotext version ([0-9\\.]*)\n";
	}
    },
    Dvips {
	String getCommand() {
	    return "dvips";
	}
	String getVersionPattern() {
	    return "^This is dvips\\(k\\) ([0-9\\.]*) " + 
	"Copyright [0-9]+ Radical Eye Software \\(www\\.radicaleye\\.com\\)\n";
	}
    },
    Dvipdfm {
	String getCommand() {
	    return "dvipdfm";
	}
	String getVersionOption() {
	    return "--version";
	}
	String getVersionPattern() {
	    return "^This is xdvipdfmx Version ([0-9]{8}) " + 
	"by the DVIPDFMx project team,\n";
	}
    },
    Dvipdfmx {
	String getCommand() {
	    return "dvipdfmx";
	}
	String getVersionOption() {
	    return "--version";
	}
	String getVersionPattern() {
	    return "^This is dvipdfmx Version ([0-9]{8}) " + 
	    	"by the DVIPDFMx project team,\n";
	}
    },
    XDvipdfmx {
	String getCommand() {
	    return "xdvipdfmx";
	}
	String getVersionOption() {
	    return "--version";
	}
	String getVersionPattern() {
	    return "^This is xdvipdfmx Version ([0-9]{8}) " + 
	    	"by the DVIPDFMx project team,\n";
	}
    },
    Dvipdft {
	String getCommand() {
	    return "dvipdft";
	}
	String getVersionOption() {
	    return "--version";
	}
	String getVersionPattern() {
	    return "^dvipdft version ([0-9\\.]*) by Thomas Esser and others\n";
	}
    },
    GS {
	String getCommand() {
	    return "gs";
	}
	String getVersionPattern() {
	    return "^GPL Ghostscript ([0-9\\.]*) " + 
	"\\([0-9]{4}-[0-9]{2}-[0-9]{2}\\)\n";
	}
    },
    Chktex {
	String getCommand() {
	    return "chktex";
	}
	String getVersionOption() {
	    return "-W";
	}
	String getVersionPattern() {
	    return "^ChkTeX v([0-9\\.]*) - " + 
	"Copyright [0-9]{4}-[0-9]{2} Jens T. Berger Thielemann.\n";
	}
    },
    Bibtex {
	String getCommand() {
	    return "bibtex";
	}
	String getVersionPattern() {
	    return "^BibTeX ([0-9\\.]*[a-z]) \\(TeX Live ";
	}
    },
    Bibtexu {
	String getCommand() {
	    return "bibtexu";
	}

	/**
	 * Returns the pattern for the version string. 
	 * Note that <code>bibtexu -v</code> yields three versions, 
	 * the version of bibtex (something like 0.99d) 
	 * which is something like the specification version, 
	 * the ICU version and the release version (and date). 
	 * What is returned is the latter version.  
	 * 
	 * @return
	 *    the pattern for the version string. 
	 */
	String getVersionPattern() {
	    return "^[^\n]*\n[^\n]*\n" +
	"Release version: ([0-9\\.]+) \\([0-9]{2} [a-z]{3} [0-9]{4}\\)\n";
	}
    },
    Bibtex8 {
	String getCommand() {
	    return "bibtex8";
	}

	/**
	 * Returns the pattern for the version string. 
	 * Note that <code>bibtex8 -v</code> yields three versions, 
	 * the version of bibtex (something like 0.99d) 
	 * which is something like the specification version, 
	 * the ICU version and the release version (and date). 
	 * What is returned is the latter version.  
	 * 
	 * @return
	 *    the pattern for the version string. 
	 */
	String getVersionPattern() {
	    return "^[^\n]*\n[^\n]*\n" +
	"Release version: ([0-9\\.]+) \\([0-9]{2} [a-z]{3} [0-9]{4}\\)\n";
	}
    },
//    Makeindex {
//	String getCommand() {
//	    return "makeindex";
//	}
//	String getVersionOption() {
//	    return "-q";
//	}
//	String getVersionPattern() {
//	    return "^(.*)\n";
//	}
//    },
    // TBC: maybe this replaces makeindex 
  Upmendex {
	String getCommand() {
	    return "upmendex";
	}
	String getVersionOption() {
	    return "-h";
	}
	String getVersionPattern() {
	    return "^upmendex - index processor, version ([0-9\\.]*) " + 
	"\\(TeX Live [0-9]{4}\\).\n";
	}
  },
    Splitindex {
	String getCommand() {
	    return "splitindex";
	}
	String getVersionOption() {
	    return "--version";
	}
	String getVersionPattern() {
	    return "^splitindex.pl ([0-9\\.]*)\n";
	}
    },
    // TBC: which of the versions is the relevant one? 
    Xindy {
	String getCommand() {
	    return "xindy";
	}
	String getVersionOption() {
	    return "-V";
	}
	// TBC: not clear whether this is the significant version 
	String getVersionPattern() {
	    return "^xindy release: ([0-9\\.]*)\n";
	}
    },
    Makeglossaries {
	String getCommand() {
	    return "makeglossaries";
	}
	String getVersionOption() {
	    return "--help";
	}
	String getVersionPattern() {
	    return "^Makeglossaries Version ([0-9\\.]*) " +
	"\\([0-9]{4}-[0-9]{2}-[0-9]{2}\\)(.*)\n";
	}
    },
    Mpost {
	String getCommand() {
	    return "mpost";
	}
	String getVersionPattern() {
	    return "^MetaPost ([0-9\\.]*) \\(TeX Live ";
	}
    },
    Ebb {
	String getCommand() {
	    return "ebb";
	}
	String getVersionOption() {
	    return "--version";
	}
	// 2nd line 
	String getVersionPattern() {
	    return "^[^\n]*\nThis is ebb Version ([0-9]+)\n";
	}
    },
    Gnuplot {
	String getCommand() {
	    return "gnuplot";
	}
	String getVersionOption() {
	    return "-V";
	}
	// TBC: we allow here patchlevel 0 only. Is this appropriate? 
	String getVersionPattern() {
	    return "^gnuplot ([0-9\\.]*) patchlevel 0\n";
	}
    },
    Inkscape {
	String getCommand() {
	    return "inkscape";
	}
	String getVersionOption() {
	    return "-V";
	}
	String getVersionPattern() {
	    return "^Inkscape ([0-9\\.]*) " +
	"\\([0-9a-f]+, [0-9]{4}-[0-9]{2}-[0-9]{2}\\)\n";
	}
    },

    Fig2Dev {
	String getCommand() {
	    return "fig2dev";
	}
	String getVersionOption() {
	    return "-V";
	}
	String getVersionPattern() {
	    return "^fig2dev Version ([0-9\\.]*[a-z])\n";
	}

    };
    
    // TBC: needed? 
    final static Map<String, Converter> cmd2conv;
    static {
	cmd2conv = new HashMap<String, Converter>();
	for (Converter conv : Converter.values()) {
	    cmd2conv.put(conv.getCommand(), conv);
	}
    }
    
    abstract String getCommand();
    
    String getVersionOption() {
	return "-v";
    }
    
    abstract String getVersionPattern();
    
}
