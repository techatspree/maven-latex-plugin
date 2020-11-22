package eu.simuline.m2latex.core;

import java.util.HashMap;
import java.util.Map;

enum Converter {

    PdfLatex {
	String getCommand() {
	    return "pdflatex";
	}

	/**
	 * The version pattern at a first sight consists of three parts: 
	 * <ul>
	 * <li>the tex version which is an approximation of pi 
	 * and which will be frozen as Donald Knuth will die at $\pi$ 
	 * (meant maybe in double precision)</li>
	 * <li>the etex version which is a two integer version. 
	 * It has to be clarified, whether there is a new release to be expected. </li>
	 * <li>the pdftex version. </li>
	 * </ul>
	 * It is quite sure from the examples found, 
	 * that the latter does not start from the beginning 
	 * when the former is increased. 
	 * This means that the first of the two version numbers are informative only
	 * and must thus be included in the environment. 
	 * @see #getVersionEnvironment()
	 */
	String getVersionPattern() {
	    // 3.1415926-2.3-1.40.12
	    // 3.14159265-2.6-1.40.21
	   return X_X_X;
	}
	
	/**
	 * For an explanation why part of the version string is in the environment, 
	 * see {@link #getVersionPattern()}. 
	 */
	String getVersionEnvironment() {
	    return "^pdfTeX 3\\.[0-9]*-[0-9]+\\.[0-9]+-%s \\(TeX Live";
	}
    },
    LuaLatex {
	String getCommand() {
	    return "lualatex";
	}
	String getVersionPattern() {
	    return X_X_X;
	}
	String getVersionEnvironment() {
	    return "^This is LuaHBTeX, Version %s \\(TeX Live";
	}
    },
    XeLatex {
	String getCommand() {
	    return "xelatex";
	}

	/**
	 * The version pattern at a first sight consists of three parts: 
	 * <ul>
	 * <li>the tex version which is an approximation of pi 
	 * and which will be frozen as Donald Knuth will die at $\pi$ 
	 * (meant maybe in double precision)</li>
	 * <li>the etex version which is a two integer version. 
	 * It has to be clarified, whether there is a new release to be expected. </li>
	 * <li>the xetex version. </li>
	 * </ul>
	 * It is quite sure from the examples found, 
	 * that the latter does not start from the beginning 
	 * when the former is increased. 
	 * This means that the first of the two version numbers are informative only
	 * and must thus be included in the environment. 
	 * @see #getVersionEnvironment()
	 */
	String getVersionPattern() {
	    return "((0\\.[0-9]*))";
	}

	/**
	 * For an explanation why part of the version string is in the environment, 
	 * see {@link #getVersionPattern()}. 
	 */
	String getVersionEnvironment() {
	    return "^XeTeX 3\\.[0-9]*-[0-9]+\\.[0-9]+-%s \\(TeX Live";
	}
    },
    Latex2rtf {
	String getCommand() {
	    return "latex2rtf";
	}
	String getVersionPattern() {
	    return "(([0-9]+)\\.([0-9]+)\\.([0-9]+) r([0-9]+))";
	}
	String getVersionEnvironment() {
	    return "^latex2rtf %s \\(released";
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
	    return X_X_X;
	}
	String getVersionEnvironment() {
	    return "^unoconv %s\n";
	}
    },
    Pdf2txt {
	String getCommand() {
	    return "pdftotext";
	}
	String getVersionPattern() {
	    return X_X_X;
	}
	String getVersionEnvironment() {
	    return "^pdftotext version %s\n";
	}
    },
    Dvips {
	String getCommand() {
	    return "dvips";
	}
	String getVersionPattern() {
	    return "(([0-9\\.]{4})\\.([0-9]))";
	}
	String getVersionEnvironment() {
	    return "^This is dvips\\(k\\) %s " + 
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
	    return YYYYMMDD;
	}
	String getVersionEnvironment() {
	    return "^This is xdvipdfmx Version %s " + 
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
	    return YYYYMMDD;
	}
	String getVersionEnvironment() {
	    return "^This is dvipdfmx Version %s " + 
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
	    return YYYYMMDD;
	}
	String getVersionEnvironment() {
	    return "^This is xdvipdfmx Version %s " + 
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
	    return "(([0-9]{4})([0-9]{2})([0-9]{2})\\.([0-9]{4}))";
	}
	String getVersionEnvironment() {
	    return "^dvipdft version %s by Thomas Esser and others\n";
	}
    },
    GS {
	String getCommand() {
	    return "gs";
	}
	String getVersionPattern() {
	    return "(([0-9]+)\\.([0-9]+)(?:\\.([0-9]+))?)";
	}
	String getVersionEnvironment() {
	    return "^GPL Ghostscript %s \\([0-9]{4}-[0-9]{2}-[0-9]{2}\\)\n";
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
	    return X_X_X;
	}
	String getVersionEnvironment() {
	    return "^ChkTeX v%s - " + 
		    "Copyright [0-9]{4}-[0-9]{2} Jens T. Berger Thielemann.\n";
	}
    },
    Bibtex {
	String getCommand() {
	    return "bibtex";
	}
	String getVersionPattern() {
	    return "((0\\.[0-9]*)([a-z]))";
	}
	String getVersionEnvironment() {
	    return "^BibTeX %s \\(TeX Live ";
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
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^[^\n]*\n[^\n]*\n" +
	"Release version: %s \\([0-9]{2} [a-z]{3} [0-9]{4}\\)\n";
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
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^[^\n]*\n[^\n]*\n" +
			"Release version: %s \\([0-9]{2} [a-z]{3} [0-9]{4}\\)\n";
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
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^upmendex - index processor, version %s " + 
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
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^splitindex.pl %s\n";
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
	    return X_X_X;
	}
	String getVersionEnvironment() {
	    return "^xindy release: %s\n";
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
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^Makeglossaries Version %s " +
	"\\([0-9]{4}-[0-9]{2}-[0-9]{2}\\)\n";
	}
    },
    Mpost {
	String getCommand() {
	    return "mpost";
	}
	String getVersionPattern() {
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^MetaPost %s \\(TeX Live ";
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
	    return YYYYMMDD;
	}
	String getVersionEnvironment() {
	    return "^[^\n]*\nThis is ebb Version %s\n";
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
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^gnuplot %s patchlevel 0\n";
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
	    return X_X_X;
	}
	// TBD: sometimes the pango line '    Pango version: 1.46.2' comes first. 
	String getVersionEnvironment() {
	    return "^Inkscape %s \\([0-9a-f]+, [0-9]{4}-[0-9]{2}-[0-9]{2}\\)\n";
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
	    return "(([0-9]+)\\.([0-9]+)\\.([0-9]+)([a-z]))";
	}
	String getVersionEnvironment() {
	    return "^fig2dev Version %s\n";
	}

    };
    
    private final static String X_X_X = "(([0-9]+)\\.([0-9]+)\\.([0-9]+))";
    private final static String X_X   = "(([0-9]+)\\.([0-9]+))";
    private final static String YYYYMMDD = "(([0-9]{4})([0-9]{2})([0-9]{2}))";
    
    // TBC: needed? 
    final static Map<String, Converter> cmd2conv;
    static {
	cmd2conv = new HashMap<String, Converter>();
	for (Converter conv : Converter.values()) {
	    cmd2conv.put(conv.getCommand(), conv);
	}
    }

    /**
     * Returns the command which which to invoke this converter. 
     * 
     * @return
     *    the command of this converter. 
     */
    abstract String getCommand();

    /**
     * Returns the option which just displays information 
     * given by {@link #getVersionEnvironment()} and among that version information 
     * as descried by {@link #getVersionPattern()}. 
     * 
     * @return
     *    the option to display (a string containing) version information. 
     *    As a default, this is <code>-v</code> which is the most common such option.
     */
    String getVersionOption() {
	return "-v";
    }

    /**
     * Returns the pattern of the version for this converter as a regular expression. 
     * All is enclosed by brackets indicating a capturing group. 
     * Non capturing groups may occur without restriction 
     * but capturing groups except the outermost one must come sequential. 
     * This patters is part of the converters output 
     * as indicated by {@link #getVersionEnvironment()}. 
     * 
     * @return
     *    the pattern of the version for this converter. 
     */
    abstract String getVersionPattern();

    /**
     * Returns the pattern of the output of this converter 
     * if invoked with command {@link #getCommand()} and option {@link #getVersionOption()}. 
     * Here, the literal <code>%s</code> indicates the location 
     * of the proper version pattern given by {@link #getVersionPattern()}.
     * If this is included a regular expression emerges. 
     * 
     * @return
     *    the pattern of the output of this converter containing its version. 
     */
    abstract String getVersionEnvironment();
    
}
