package eu.simuline.m2latex.core;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
	    return "^pdfTeX 3\\.[0-9]*-[0-9]+\\.[0-9]+-%s \\(TeX Live [0-9]{4}/";
	}

	ConverterCategory getCategory() {
	    return ConverterCategory.LaTeX;
	}

    },
    // TBC: relation lualatex, luahbtex and so on 
    LuaLatex {
	String getCommand() {
	    return "lualatex";
	}
	String getVersionPattern() {
	    return X_X_X;
	}
	String getVersionEnvironment() {
	    return "^This is LuaHBTeX, Version %s \\(TeX Live [0-9]{4}/";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.LaTeX;
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
	    return "^XeTeX 3\\.[0-9]*-[0-9]+\\.[0-9]+-%s \\(TeX Live [0-9]{4}/";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.LaTeX;
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
	    return "^" + getCommand() + " %s \\(released [A-Z][a-z]{2} [0-9]+, [0-9]{4}\\)\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.LaTeX2Rtf;
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
	ConverterCategory getCategory() {
	    return ConverterCategory.Odt2Doc;
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
	    return "^" + getCommand() + " version %s\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Pdf2Txt;
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
	    return "^This is " + getCommand() + "\\(k\\) %s " + "(?:\\(TeX Live [0-9]+\\)  )?" +
		    "Copyright [0-9]+ Radical Eye Software \\(www\\.radicaleye\\.com\\)\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Unspecific;
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
	    return "^This is " + getCommand() +
		    " Version %s by the DVIPDFMx project team,\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Dvi2Pdf;
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
	    return "^This is " + getCommand() +
		    " Version %s by the DVIPDFMx project team,\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Dvi2Pdf;
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
	    return "^This is " + getCommand() +
		    " Version %s by the DVIPDFMx project team,\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Dvi2Pdf;
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
	    return "^" + getCommand() + " version %s by Thomas Esser and others\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Dvi2Pdf;
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
	ConverterCategory getCategory() {
	    return ConverterCategory.Unspecific;
	}
    },
    ChkTeX {
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
	    return "^" + ChkTeX + " v%s - " +
		    "Copyright [0-9]{4}-[0-9]{2} Jens T. Berger Thielemann.\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.LatexChk;
	}
},
	DiffPdfVisualLy {
		String getCommand() {
			return "diff-pdf-visually";
	}
	// TBD: bugfix: this is just the help option. 
	// -v is the verbose option 
	String getVersionOption() {
		return "-h";
}
String getVersionPattern() {
	return "(([0-9]+))";
}
// TBD: rework 
String getVersionEnvironment() {
	//return "default: %s";
	return //"^usage: " + getCommand() + ".*\n"+
	"Compare two PDFs visually. The exit code is %s ";
}
// TBD: make specific 
ConverterCategory getCategory() {
	return ConverterCategory.DiffPdf;
}
	},
		DiffPdf {
			String getCommand() {
				return "diff-pdf";
		}
		// TBD: bugfix: this is just the help option. 
		// -v is the verbose option 
		String getVersionOption() {
	    return "-h";
	}
	String getVersionPattern() {
		return "(([0-9]+))";
	}
	String getVersionEnvironment() {
		//return "default: %s";
		return "^Usage: [^\n]*\n[^\n]*\n[^\n]*\n[^\n]*\n[^\n]*\n[^\n]*\n[^\n]*\n[^\n]*\n" + 
		".*\\w*rasterization resolution \\(default: %s dpi\\)";
	}
	// TBD: make specific 
	ConverterCategory getCategory() {
		return ConverterCategory.DiffPdf;
	}
		},
		Diff {
			String getCommand() {
				return "diff";
		}
		// TBD: bugfix: this is just the help option. 
		// -v is the verbose option 
		String getVersionOption() {
	    return "-v";
	}
	String getVersionPattern() {
		return X_X;
	}
	String getVersionEnvironment() {
		//return "default: %s";
		return "^" + getCommand() + " \\(GNU diffutils\\) %s";
	}
	// TBD: make specific 
	ConverterCategory getCategory() {
		return ConverterCategory.DiffPdf;
	}
		},
		PdfInfo {
			String getCommand() {
				return "pdfinfo";
		}
		String getVersionOption() {
	    return "-v";
		}
		String getVersionPattern() {
			return X_X_X;
		}
		String getVersionEnvironment() {
			return "^" + getCommand() +" version %s";
		}
			// TBD: make specific 
	ConverterCategory getCategory() {
		return ConverterCategory.Unspecific;
	}
		},
		ExifTool {
			String getCommand() {
				return "exiftool";
			}
			String getVersionOption() {
				return "-ver";
			}
			String getVersionPattern() {
				return X_X;
			}
			String getVersionEnvironment() {
				return "^%s";
			}
			// TBD: make specific 
			ConverterCategory getCategory() {
				return ConverterCategory.Unspecific;
			}
		},
    BibTeX {
	String getCommand() {
	    return "bibtex";
	}
	String getVersionPattern() {
	    return "((0\\.[0-9]*)([a-z]))";
	}
	String getVersionEnvironment() {
	    return "^" + BibTeX + " %s \\(TeX Live [0-9]{4}/";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.BibTeX;
	}
    },
    BibTeXu {
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
	    return "^This is " + BibTeXu + ": a UTF-8 Big " + BibTeX + " version [^\n]*\n" +
		    "Implementation: [^\n]*\n" +
		    "Release version: %s \\([0-9]{2} [a-z]{3} [0-9]{4}\\) \\(TeX Live [0-9]{4}\\)\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.BibTeX;
	}
    },
    BibTeX8 {
	String getCommand() {
	    return "bibtex8";
	}
	// TBD: maybe the line endings are not platform independent.
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
	    return "^This is 8-bit Big " + BibTeX + " version [^\n]*\n" +
		    "Implementation: [^\n]*\n" +
		    "Release version: %s \\([0-9]{2} [a-z]{3} [0-9]{4}\\) \\(TeX Live [0-9]{4}\\)\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.BibTeX;
	}
    },
    Makeindex {
	String getCommand() {
	    return "makeindex";
	}
	String getVersionOption() {
	    return TexFileUtils.getEmptyIdx().getName().toString();
	}
	String getVersionPattern() {
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^This is " + getCommand() + ", version %s " +
		    "\\[TeX Live [0-9]{4}\\] \\(kpathsea \\+ Thai support\\).\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.MakeIndex;
	}

    },
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
	    return "^" + getCommand() + " - index processor, version %s \\(TeX Live [0-9]{4}\\).\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Unspecific;
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
	    return "^" + getCommand() + ".pl %s\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.SplitIndex;
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
	    return "^" + getCommand() + " release: %s\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Unspecific;
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
	    return "^" + Makeglossaries + " Version %s " +
	"\\([0-9]{4}-[0-9]{2}-[0-9]{2}\\)\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.MakeGlossaries;
	}
    },
    // TBD: add a category 
   PythonTeX {
	String getCommand() {
	    return "pythontex";
	}
	String getVersionOption() {
	    return "--version";
	}
	String getVersionPattern() {
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^" + PythonTeX + " %s\n";
	}
    // TBD: add a category 
	ConverterCategory getCategory() {
	    return ConverterCategory.MakeGlossaries;
	}
    },
    // TBD: add a category 
   Latexmk {
	String getCommand() {
	    return "latexmk";
	}
	String getVersionPattern() {
	    return "(([0-9]+)\\.([0-9]+)([a-z]?))";
	}
	String getVersionEnvironment() {
	    return "^\n?" + Latexmk +
		", John Collins, .*[0-9]+ [A-Z][a-z]+\\.? [0-9]+. Version %s\n";
	}
    // TBD: add a category 
	ConverterCategory getCategory() {
	    return ConverterCategory.MakeGlossaries;
	}
    },
    MetaPost {
	String getCommand() {
	    return "mpost";
	}
	String getVersionPattern() {
	    return X_X;
	}
	String getVersionEnvironment() {
	    return "^" + MetaPost + " %s \\(TeX Live [0-9]{4}/";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.MetaPost;
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
	    return "^[^\n]*\nThis is " + getCommand() + " Version %s\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.EbbCmd;
	}
    },
    Gnuplot {
	String getCommand() {
	    return "gnuplot";
	}
	String getVersionOption() {
	    return "-V";
	}
	String getVersionPattern() {
	    return "(([0-9]+)\\.([0-9]+) patchlevel ([0-9]+))";
	}
	String getVersionEnvironment() {
	    return "^" + getCommand() + " %s\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Gnuplot2Dev;
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
	    return "^(?:[^\n]*\n)?" // eliminates pango version popping up sparsely
	+ Inkscape + " %s \\([0-9a-f]+, [0-9]{4}-[0-9]{2}-[0-9]{2}\\)\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Svg2Dev;
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
	    return "^" + getCommand() + " Version %s\n";
	}
	ConverterCategory getCategory() {
	    return ConverterCategory.Fig2Dev;
	}

    };
    
    private final static String X_X_X = "(([0-9]+)\\.([0-9]+)\\.([0-9]+))";
    private final static String X_X   = "(([0-9]+)\\.([0-9]+))";
    private final static String YYYYMMDD = "(([0-9]{4})([0-9]{2})([0-9]{2}))";
    
    // TBC: needed? 
    private final static Map<String, Converter> cmd2conv;
		static {
			cmd2conv = new HashMap<String, Converter>();
			for (Converter conv : Converter.values()) {
				cmd2conv.put(conv.getCommand(), conv);
			}
    }

    // CAUTION: this may return null also, 
    // because the appropriate exception depends on the context. 
    /**
     * Given a command <code>cmd</code>, returns the according {@link Converter} 
     * such that {$link {@link Converter#getCommand()} returns <code>cmd</code> again.
     * @param cmd
     *    a valid command string.
     * @return
     *    The converter tied to <code>cmd</code> or <code>null</code>.
     */
    public static Converter cmd2Conv(String cmd) {
			return cmd2conv.get(cmd);
		}

    /**
     * Returns a comma separated list of command names of converters of <code>convs</code>,
     * i.e. for all elements of <code>convs</code> 
     * the according command is added to the string returned and the commands are separated 
     * by comma plus blank.
     *
     * @param convs
     *    a collection of converters.
     * @return
     *    a comma separated list of commands of the converters given.
     */
  	static String toCommandsString(Collection<Converter> convs) {
			return convs.stream().map(x -> x.getCommand()).collect(Collectors.joining(", "));
		}
    
		/**
		 * Returns a comma separated list of command names of alll converters.
		 * 
		 * @return
		 *         a comma separated list of commands of all converters.
		 */
		static String toCommandsString() {
			return toCommandsString(Arrays.asList(Converter.values()));
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
    
    // TBD: rework, eliminate Unspecific 
    abstract ConverterCategory getCategory();


    /**
     * Runs this converter in <code>executor</code> 
     * to determine a string containing version information.
     * 
     * @param executor
     *    the executor to run this converter with: 
     *    The executable given by {@link #getCommand()} 
     *    is passed options {@link #getVersionOption()} 
     *    to get version option and run 
     *    in the directory containing {@link TexFileUtils#EMPTY_IDX}. 
     * @return
     *    the output of the invocation 
     *    which matches {@link #getVersionEnvironment()}. 
     * @throws BuildFailureException
     *    TEX01 if invocation of <code>command</code> fails very basically: 
     *    <ul>
     *    <li><!-- see Commandline.execute() -->
     *    the file expected to be the working directory 
     *    does not exist or is not a directory. 
     *    <li><!-- see Commandline.execute() -->
     *    {@link Runtime#exec(String, String[], File)} fails 
     *    throwing an {@link java.io.IOException}. 
     *    <li> <!-- see CommandLineCallable.call() -->
     *    an error inside systemOut parser occurs 
     *    <li> <!-- see CommandLineCallable.call() -->
     *    an error inside systemErr parser occurs 
     *    <li> Wrapping an {@link InterruptedException} 
     *    on the process to be executed thrown by {@link Process#waitFor()}. 
     *    </ul>
     */
    String getVersionInfo(CommandExecutor executor)
		throws BuildFailureException {
			return executor.execute(TexFileUtils.getEmptyIdx().getParentFile(), null,
					getCommand(), new String[] { getVersionOption() }).output;
		}

}
