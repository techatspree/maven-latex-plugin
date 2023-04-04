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
 * Consequently, each category has a method {@link #getCommandFieldname()}
 * returning the according field in the settings. 
 *
 */
// TBD: take advantage of the fact,
//that most entries are related to the fieldname: 
// lowercase and add 'Command'. 
enum ConverterCategory {
	// LaTeX: mostly to pdf but also to dvi
	// This is slightly inconsistent.
	// TBD: shall be Latex2dev
	LaTeX() {
		String getExtName() {
			return "latex2pdf";// TBD: bad name because also dvi and xdv 
		}
	},
	// TBD: clarify: not yet used really
	Latex2Html() {
		String getExtName() {
			throw new UnsupportedOperationException();
		}
	},
	// TBD: check: not used.
	// LaTeX2Odt() {
	// String getFieldname() {
	// throw new UnsupportedOperationException();
	// }
	// },
	LaTeX2Rtf() {
		String getExtName() {
			return "latex2rtf";
		}
	},
	BibTeX() {
		String getExtName() {
			return "bibtex";
		}
	},
	MakeIndex() {
		String getExtName() {
			return "makeIndex";
		}
	},
	MakeGlossaries() {
		String getExtName() {
			return "makeGlossaries";
		}
	},
	SplitIndex() {
		String getExtName() {
			return "splitIndex";
		}
	},
	Pythontex() {
		String getExtName() {
			return "pythontex";
		}
	},
	DePythontex() {
		String getExtName() {
			return "depythontex";
		}
	},
	LatexChk() {// TBD: eliminate inconsistency
		String getExtName() {
			return "chkTex";
		}
	},
	DiffPdf() {
		String getExtName() {
			return "diffPdf";
		}
	},
  /**
   * Converters transforming <code>dvi</code> files into pdf files. 
   * Some of them can also convert <code>xdv</code> to <code>pdf</code>, 
   * but this is immaterial, 
   * because the <code>xdv</code> format is not supported. 
   * The currently registered converters, {@link Converter#Dvipdfm}, 
   * {@link Converter#Dvipdfmx}, {@link Converter#XDvipdfmx} 
   * and {@link Converter#Dvipdft} 
   * can all convert <code>xdv</code> files. 
   * At least for texlive, 
   * all of these converters can be run with filename including the extension 
   * or without extension. 
   * If both extensions, <code>dvi</code> and <code>xdv</code> are found, 
   * the <code>xdv</code> is processed, else the file which is found. 
   * Of course if neither extension is found an error is emitted. 
   * <p>
   * Note also, 
   * that <code>dvipdf</code> is currently not among the {@link Converter}s, 
   * and if it is it does not belong to this category 
   * becausse it does not provide proper options. 
   */
	Dvi2Pdf() {
		String getExtName() {
			return "dvi2pdf";
		}
	},
	MetaPost() {
		String getExtName() {
			return "metapost";
		}
	},
	Svg2Dev() {
		String getExtName() {
			return "svg2dev";
		}
	},
	Fig2Dev() {
		String getExtName() {
			return "fig2dev";
		}
	},
	Gnuplot2Dev() {
		String getExtName() {
			return "gnuplot";
		}
	},
	Odt2Doc() {
		String getExtName() {
			return "odt2doc";
		}
	},
	Pdf2Txt() {
		String getExtName() {
			return "pdf2txt";
		}
	},
	EbbCmd() {
		String getExtName() {
			return "ebb";
		}
	},

	// Those are not usable
	// TBD: clarify: For latex to html/odt
	// dvips: for conversion to ps??
	// gs
	// pdfinfo, exiftool
	// upmendex: right interface?
	// xindy
	// pythontex, depythontex
	// latexmk
	Unspecific {
		String getExtName() {
			throw new UnsupportedOperationException();
		}
	};

	// may throw UnsupportedOperationException
  /**
   * Returns the name of the field in the configuration 
   * containing the command of the converter of this category. 
   * 
   * @return
   *    the name of the field in the configuration 
   *    containing the command of the converter of this category. 
   *    This is just {@link #getExtName()} 
   *    followed by the literal <code>Command</code>. 
   */
	String getCommandFieldname() {
		return this.getExtName() + "Command";
	}

	// may throw UnsupportedOperationException
  /**
   * Returns the name under which the category occurs in the configuration. 
   * This applies at least to the command (see {@link #getCommandFieldname()}) 
   * and to the options. 
   * 
   * @return
   *    the name under which the category occurs in the configuration. 
   */
	abstract String getExtName();

}
