package eu.simuline.m2latex.core;

//import java.io.File;

/**
 * The enumeration of all supported (creational) targets.
 * These are targets where tex files are converted into a target format
 * and the resulting document is included in the target folder for delivery.
 * Additional targets are <code>clr</code>,
 * <code>grp</code>, <code>chk</code> and <code>vrs</code>.
 * None of them are creational,
 * not even <code>grp</code> although graphic files are created.
 *
 *
 * Created: Fri Oct 7 13:32:21 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public enum Target {

    /**
     * The only target without artifact
     */
    chk() {

        // may throw BuildFailureException TEX01
        public void processSource(LatexProcessor latexProcessor,
                                  LatexMainDesc desc) throws BuildFailureException {
            latexProcessor.processCheck(desc);
        }

        public String getPatternOutputFiles(Settings settings) {
            return Target.NO_OUTPUT_FILES;
        }
    },
    /**
     * standalone.
     */
    dvi() {
        // FIXME: how does this fit with preprocessing??
        private final String LATEX_OUTPUT_FILES = "^(T$T\\.(dvi|xdv)|.+(\\.(ptx|eps|jpg|png)|\\d+\\.mps))$";

        // may throw BuildFailureException TEX01
        public void processSource(LatexProcessor latexProcessor,
                                  LatexMainDesc desc) throws BuildFailureException {
            latexProcessor.processLatex2dvi(desc);
        }

        public String getPatternOutputFiles(Settings settings) {
            return LATEX_OUTPUT_FILES;
        }
    },
    /**
     * standalone.
     */
    pdf() {
        private final String LATEX_OUTPUT_FILES = "^T$T\\.pdf$";

        // may throw BuildFailureException TEX01
        public void processSource(LatexProcessor latexProcessor,
                                  LatexMainDesc desc) throws BuildFailureException {
            latexProcessor.processLatex2pdf(desc);
        }

        public String getPatternOutputFiles(Settings settings) {
            return LATEX_OUTPUT_FILES;
        }

        public boolean hasDiffTool() {
            return true;
        }
    },
    /**
     * Based on {@link #pdf}
     *
     */
    html() {
        // may throw BuildFailureException TEX01
        public void processSource(LatexProcessor latexProcessor,
                                  LatexMainDesc desc) throws BuildFailureException {
            latexProcessor.processLatex2html(desc);
        }

        public String getPatternOutputFiles(Settings settings) {
            return settings.getPatternT4htOutputFiles();
        }
    },
    /**
     * Based on {@link #pdf}
     *
     */
    odt() {
        private final String OOFFICE_OUTPUT_FILES = "^T$T\\.(odt|fodt|uot|uot)$";

        // may throw BuildFailureException TEX01
        public void processSource(LatexProcessor latexProcessor, 
                                LatexMainDesc desc) throws BuildFailureException {
            latexProcessor.processLatex2odt(desc);
        }

        public String getPatternOutputFiles(Settings settings) {
            return OOFFICE_OUTPUT_FILES;
        }
    },
    /**
     * Based on {@link #odt}
     *
     */
    docx() {
        private final String MSWORD_OUTPUT_FILES = "^T$T\\.(doc(|6|.95|.x|.x7)|rtf)$";

        // may throw BuildFailureException TEX01
        public void processSource(LatexProcessor latexProcessor,
                                LatexMainDesc desc) throws BuildFailureException {
            latexProcessor.processLatex2docx(desc);
        }

        public String getPatternOutputFiles(Settings settings) {
            return MSWORD_OUTPUT_FILES;
        }
    },
    /**
     * standalone
     *
     */
    rtf() {
        private final String RTF_OUTPUT_FILES = "^T$T\\.rtf$";

        // may throw BuildFailureException TEX01
        public void processSource(LatexProcessor latexProcessor,
                                  LatexMainDesc desc) throws BuildFailureException {
            latexProcessor.processLatex2rtf(desc);
        }

        public String getPatternOutputFiles(Settings settings) {
            return RTF_OUTPUT_FILES;
        }

    },
    /**
     * Based on {@link #pdf}
     */
    txt() {
        private final String TXT_OUTPUT_FILES = "^T$T\\.txt$";

        // may throw BuildFailureException TEX01
        public void processSource(LatexProcessor latexProcessor,
                                LatexMainDesc desc) throws BuildFailureException {
            latexProcessor.processLatex2txt(desc);
        }

        public String getPatternOutputFiles(Settings settings) {
            return TXT_OUTPUT_FILES;
        }
    };

    private final static String NO_OUTPUT_FILES = ".^";


    /**
     * Processes the latex main file <code>texFile</code>
     * delegating to <code>latexProcessor</code>.
     * Logging: FIXME: may be incomplete
     * <ul>
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05 if running a command
     * to transform <code>texFile</code> failed.
     * </ul>
     *
     * @param latexProcessor
     *    the processor to process <code>texFile</code>
     * @param desc
     *    the latex main file to be processed.
     * @throws BuildFailureException
     *    TEX01 if invocation of a command
     *    to transform <code>texFile</code> failed.
     */
    public abstract void processSource(LatexProcessor latexProcessor, LatexMainDesc desc)
            throws BuildFailureException;

    /**
     * Returns the pattern of the output files.
     * For example if creating pdf,
     * this is just <code>^T$T\.pdf$</code>,
     * where <code>T$T</code> represents the name of the latex main file
     * without suffix.
     * For target {@link #html}, this is much more complicated,
     * because a lot of files are created in general,
     * not only <code>^T$T\.h?tml?$</code>.
     *
     * @param settings
     *                 the settings required to determine the pattern.
     *                 This depends on the settings for {@link #html} only.
     */
    public abstract String getPatternOutputFiles(Settings settings);

    /**
     * Returns whether this target as an associated visual diff tool. 
     * Currently this is the case for {@link #pdf} only. 
     * 
     * @return
     *    whether this target as an associated diff tool. 
     *    Currently this is the case for {@link #pdf} only. 
     */
    public boolean hasDiffTool() {
        return false;
    }

}
