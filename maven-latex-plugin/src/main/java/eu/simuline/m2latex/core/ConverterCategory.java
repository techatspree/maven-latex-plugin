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
	String getFieldname() {
		return this.getExtName() + "Command";
	}

	// may throw UnsupportedOperationException
	abstract String getExtName();

}
