package eu.simuline.m2latex.antTask;

import org.apache.tools.ant.BuildException;

import eu.simuline.m2latex.core.BuildFailureException;
import eu.simuline.m2latex.core.ParameterAdapter;
import eu.simuline.m2latex.core.Target;
import eu.simuline.m2latex.core.LatexProcessor;

import java.util.SortedSet;

public class LatexCfgTask extends AbstractLatexTask {

    // api-docs inherited from ParameterAdapter
    public SortedSet<Target> getTargetSet() {
	return this.settings.getTargetSet();
    }

    /**
     * Invoked by ant executing the task. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU01: Cannot read directory... 
     * <li> WFU03: cannot close 
     * <li> WPP02: tex file may be latex main file 
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> EEX01, EEX02, EEX03, WEX04, WEX05: 
     *      applications for preprocessing graphic files 
     *      or processing a latex main file fails. 
     * </ul>
     * @throws BuildException
     *    <ul>
     *    <li> TSS01 if 
     *    the tex source directory does either not exist 
     *    or is not a directory. 
     *    <li> TSS02 if 
     *    the tex source processing directory does either not exist 
     *    or is not a directory. 
     *    <li> TSS03 if 
     *    the output directory exists and is no directory. 
     *    <li> TEX01 if 
     *    invocation of applications for preprocessing graphic files 
     *    or processing a latex main file fails 
     *    <li> TFU01 if 
     *    the target directory that would be returned 
     *    exists already as a regular file. 
     *    <li> TFU03, TFU04, TFU05, TFU06 if 
     *    copy of output files to target folder fails. 
     *    For details see {@link LatexProcessor#create()}. 
     *    </ul>
     */
    public void execute() throws BuildException {
 	initialize();
	try {
	    // may throw BuildFailureException 
	    // TSS01, TSS02, TSS03, TEX01, TFU01, TFU03, TFU04, TFU05, TFU06 
	    // may log WFU01, WFU03, EFU05, EFU06, 
	    // WPP02, WPP03, 
	    // EEX01, EEX02, EEX03, WEX04, WEX05 
	    this.latexProcessor.create();
	} catch (BuildFailureException e) {
	    throw new BuildException(e.getMessage(), e.getCause());
	}
     }
 }
