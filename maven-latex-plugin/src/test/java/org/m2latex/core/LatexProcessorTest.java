/*
 * The akquinet maven-latex-plugin project
 *
 * Copyright (c) 2011 by akquinet tech@spree GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.m2latex.core;

import org.m2latex.mojo.MavenLogWrapper;
import org.m2latex.mojo.PdfMojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import org.easymock.MockControl;
import org.mockito.Mockito;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.After;
//import org.junit.BeforeClass;
//import org.junit.AfterClass;

// FIXME: missing: test of logging 
public class LatexProcessorTest {

    private final static String WORKING_DIR = 
	System.getProperty("testResourcesDir");

    // FIXME: removed with Mockito? 
    private MockControl executorCtrl = MockControl
	.createStrictControl(CommandExecutor.class);

    private CommandExecutor executor = (CommandExecutor) executorCtrl.getMock();
    private CommandExecutor executor2 = Mockito.mock(CommandExecutor.class);


    private MockControl fileUtilsCtrl = MockControl
	.createStrictControl(TexFileUtils.class);

    private TexFileUtils fileUtils = (TexFileUtils) fileUtilsCtrl.getMock();
    private TexFileUtils fileUtils2 = Mockito.mock(TexFileUtils.class);


    private Settings settings = new Settings();

    private LogWrapper log = new MavenLogWrapper(new SystemStreamLog());

    private LatexProcessor processor = new LatexProcessor
	(settings, this.executor, log, fileUtils, new PdfMojo());


    private File texFile = new File(WORKING_DIR, "test.tex");
    private File pdfFile = new File(WORKING_DIR, "test.pdf");
    private File dviPdfFile = new File
	(WORKING_DIR, "test."+settings.getPdfViaDvi().getLatexLanguage());
    private File htmlFile= new File(WORKING_DIR, "test.html");
    private File auxFile = new File(WORKING_DIR, "test.aux");
    private File logFile = new File(WORKING_DIR, "test.log");

    private File bblFile = new File(WORKING_DIR, "test.bbl");
    private File blgFile = new File(WORKING_DIR, "test.blg");

    private File idxFile = new File(WORKING_DIR, "test.idx");
    private File indFile = new File(WORKING_DIR, "test.ind");
    private File ilgFile = new File(WORKING_DIR, "test.ilg");

    private File gloFile = new File(WORKING_DIR, "test.glo");
    private File istFile = new File(WORKING_DIR, "test.ist");
    private File xdyFile = new File(WORKING_DIR, "test.xdy");
    private File glsFile = new File(WORKING_DIR, "test.gls");
    private File glgFile = new File(WORKING_DIR, "test.glg");
    // this one does never exist. 
    private File xxxFile = new File(WORKING_DIR, "test");

    private File tocFile = new File(WORKING_DIR, "test.toc");
    private File lofFile = new File(WORKING_DIR, "test.lof");
    private File lotFile = new File(WORKING_DIR, "test.lot");

    private static void cleanWorkingDir() {
	File wDir = new File(WORKING_DIR);
	assert wDir.isDirectory() : "Expected directory. ";
	File[] files = wDir.listFiles();
	assert files != null : "Working directory is not readable. ";
	for (File file : files) {
	    file.delete();
	}
    }

    @Before public void setUp() throws IOException {
	cleanWorkingDir();
	this.logFile.createNewFile();
	this.blgFile.createNewFile();
    }

    @After public void tearDown() throws IOException {
	cleanWorkingDir();
    }

    //@Ignore 
    // FIXME: does not take pdfViaDvi into account 
    @Test public void testProcessLatexSimple()
	throws BuildFailureException {
	
	mockProcessLatex2pdf(false, false, false);

        this. executorCtrl.replay();
        this.fileUtilsCtrl.replay();
        processor.processLatex2pdf(this.texFile);
        this. executorCtrl.verify();
        this.fileUtilsCtrl.verify();
    }

    //@Ignore 
    @Test public void testProcessLatexWithBibtex() 
	throws BuildFailureException {

	mockProcessLatex2pdf(true, false, false);

	this. executorCtrl.replay();
        this.fileUtilsCtrl.replay();
        processor.processLatex2pdf(this.texFile);
	this. executorCtrl.verify();
        this.fileUtilsCtrl.verify();
    }

    //@Ignore 
    @Test public void testProcessLatex2html() throws BuildFailureException {

	mockProcessLatex2html(false, false, false);

        this. executorCtrl.replay();
        this.fileUtilsCtrl.replay();
        processor.processLatex2html(this. texFile);
	this. executorCtrl.verify();
        this.fileUtilsCtrl.verify();
    }

    private void mockProcessLatex2pdf(boolean needBibtex,
				      boolean needMakeIndex,
				      boolean needMakeGlossaries) 
	throws BuildFailureException {
	mockConstrLatexMainDesc();
	assert !this.settings.getPdfViaDvi().isViaDvi();
	// FIXME: here should be mockProcessLatex2dev
	mockProcessLatex2devCore(needBibtex, needMakeIndex, needMakeGlossaries);

	fileUtils.matchInFile(logFile, LatexProcessor.PATTERN_OUFULL_HVBOX);
	fileUtilsCtrl.setReturnValue(Boolean.FALSE);
	fileUtils.matchInFile(logFile, this.settings.getPatternWarnLatex());
	fileUtilsCtrl.setReturnValue(Boolean.FALSE);
    }

    private void mockProcessLatex2html(boolean needBibtex,
				       boolean needMakeIndex,
				       boolean needMakeGlossaries) 
	throws BuildFailureException {

	mockConstrLatexMainDesc();
	mockPreProcessLatex2dev(needBibtex, needMakeIndex, needMakeGlossaries);
        mockRunLatex2html();
    }

    private void mockConstrLatexMainDesc() {
   	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_VOID);
   	fileUtilsCtrl.setReturnValue(this.xxxFile);
   	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_PDF);
   	fileUtilsCtrl.setReturnValue(this.pdfFile);
   	// FIXME 
   	fileUtils.replaceSuffix(this.texFile, 
   				"."+this.settings.getPdfViaDvi()
   				.getLatexLanguage());
   	fileUtilsCtrl.setReturnValue(this.dviPdfFile);
   	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_LOG);
   	fileUtilsCtrl.setReturnValue(this.logFile);

   	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_IDX);
   	fileUtilsCtrl.setReturnValue(this.idxFile);
   	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_IND);
   	fileUtilsCtrl.setReturnValue(this.indFile);
   	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_ILG);
   	fileUtilsCtrl.setReturnValue(this.ilgFile);

   	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_GLS);
   	fileUtilsCtrl.setReturnValue(this.glsFile);
   	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_GLO);
   	fileUtilsCtrl.setReturnValue(this.gloFile);
   	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_GLG);
   	fileUtilsCtrl.setReturnValue(this.glgFile);
    }

    // FIXME: pdf never via dvi 
    // FIXME: parametrization of needMakeIndex, needMakeGlossaries 
    // 
    private void mockPreProcessLatex2dev(boolean needBibtex,
					 boolean needMakeIndex,
					 boolean needMakeGlossaries) 
	throws BuildFailureException {
	assert !needMakeIndex && !needMakeGlossaries;

 	// run latex 
        mockRunLatex();

	// run bibtex, makeIndex and makeGlossary by need 
	mockRunBibtexByNeed(needBibtex);
	mockRunMakeIndexByNeed(needMakeIndex);
	mockRunMakeGlossaryByNeed(needMakeGlossaries);

	if (needBibtex) {
	    return;
	}
	// FIXME: not the truth if makeindex and makeglossaries are included 
	// and also not if TOC exists. 

	assert !this.tocFile.exists() 
	    && !this.lofFile.exists() 
	    && !this.lotFile.exists();
	// determine from presence of toc, lof, lot (and idx and other criteria)
	// whether to rerun latex: no 
	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_TOC);
	fileUtilsCtrl.setReturnValue(this.tocFile);
	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_LOF);
	fileUtilsCtrl.setReturnValue(this.lofFile);
	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_LOT);
	fileUtilsCtrl.setReturnValue(this.lotFile);
    }

    // FIXME: currently, neither toc nor lof nor lot exist 
    private void mockProcessLatex2devCore(boolean needBibtex,
					  boolean needMakeIndex,
					  boolean needMakeGlossaries) 
	throws BuildFailureException {
	assert !needMakeIndex && !needMakeGlossaries;
	assert !this.tocFile.exists() 
	    && !this.lofFile.exists() 
	    && !this.lotFile.exists();
	// FIXME: would be safer to define = -1 
	assert this.settings.getMaxNumReRunsLatex() == 5;

	mockPreProcessLatex2dev(needBibtex, needMakeIndex, needMakeGlossaries);
	// preProcessLatex2dev returns 
	// 2 if needBibtex 
	// since currently neither makeindex nor makeglossaries are supported 
	// and neither toc, lof or lot exist: 
	// 0 else 

	if (needBibtex) {
	    // numLatexReRuns == 2 
	    mockRunLatex();
	    // numLatexReRuns == 1 
	    // enter for-loop... 
	    mockNeedRun(false, this.settings.getPatternReRunMakeIndex());
	    mockRunLatex();
	    mockNeedRun(false, this.settings.getPatternReRunLatex());
	    // second loop 
	    mockNeedRun(false, this.settings.getPatternReRunMakeIndex());
	    // exit for-loop 
	    return;
	} else {
	    mockNeedRun(false, this.settings.getPatternReRunLatex());
	    // enter for-loop... 
	    mockNeedRun(false, this.settings.getPatternReRunMakeIndex());
	    // since both conditions are false 
	    // exit for-loop 
	    return;
 	}
    }

    private void mockNeedRun(Boolean retVal, String pattern)
        throws BuildFailureException {

        fileUtils.matchInFile(this.logFile, pattern);
        fileUtilsCtrl.setReturnValue(retVal);
    }

    private void mockRunBibtexByNeed(Boolean runBibtex) 
	throws BuildFailureException {

        fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_AUX);
        fileUtilsCtrl.setReturnValue(this.auxFile);
        fileUtils.matchInFile(this.auxFile, 
			      LatexProcessor.PATTERN_NEED_BIBTEX_RUN);
        fileUtilsCtrl.setReturnValue(runBibtex);

	if (!runBibtex) {
	    return;
	}

	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_BBL);
        fileUtilsCtrl.setReturnValue(this.bblFile);
  
        this.executor.execute(texFile.getParentFile(),
			      this.settings.getTexPath(),
			      this.settings.getBibtexCommand(),
			      LatexProcessor.buildArguments
			      (this.settings.getBibtexOptions(), this.auxFile),
			      this.bblFile);
        this.executorCtrl.setMatcher(MockControl.ARRAY_MATCHER);
        this.executorCtrl.setReturnValue(null);

	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_BLG);
	fileUtilsCtrl.setReturnValue(this.blgFile);

	// logging 
	fileUtils.matchInFile(blgFile, 
	                      this.settings.getPatternErrBibtex());
	fileUtilsCtrl.setReturnValue(Boolean.FALSE);
	fileUtils.matchInFile(blgFile, 
	                      this.settings.getPatternWarnBibtex());
	fileUtilsCtrl.setReturnValue(Boolean.FALSE);
    }

    private void mockRunMakeIndexByNeed(boolean runMakeIndex) 
	throws BuildFailureException {

	assert !runMakeIndex;

	if (!runMakeIndex) {
	    return;
	}
	mockRunMakeIndex();
    }

    private void mockRunMakeIndex() throws BuildFailureException {
	assert false;
        this.executor.execute(this.texFile.getParentFile(),
			      this.settings.getTexPath(),
			      this.settings.getMakeIndexCommand(),
			      LatexProcessor.buildArguments
			      (this.settings.getMakeIndexOptions(), 
			       this.idxFile),
			      this.indFile);
	this.executorCtrl.setMatcher(MockControl.ARRAY_MATCHER);
        this.executorCtrl.setReturnValue(null);

	// since ilg file does not exist 
	// fileUtils.matchInFile(ilgFile, 
	//                       this.settings.getPatternErrMakeIndex());
	// fileUtilsCtrl.setReturnValue(Boolean.FALSE);
      }

    private void mockRunMakeGlossaryByNeed(boolean runMakeGlossaries) 
	throws BuildFailureException {
 
	assert !runMakeGlossaries;
	if (!runMakeGlossaries) {
	    return;
	}
	assert false;

        this.executor.execute(this.texFile.getParentFile(),
			      this.settings.getTexPath(),
			      this.settings.getMakeGlossariesCommand(),
			      LatexProcessor.buildArguments
			      (this.settings.getMakeGlossariesOptions(), 
			       xxxFile),
			      glsFile);
	this.executorCtrl.setMatcher(MockControl.ARRAY_MATCHER);
        this.executorCtrl.setReturnValue(null);

	// since glg file does not exist 
	// fileUtils.matchInFile(glgFile, 
	//                       this.settings.getPatternErrMakeGlossaries());
	// fileUtilsCtrl.setReturnValue(Boolean.FALSE);
    }

    private void mockRunLatex() throws BuildFailureException {

        this.executor.execute(this.texFile.getParentFile(),
			      this.settings.getTexPath(),
			      this.settings.getLatex2pdfCommand(),
			      LatexProcessor.buildLatexArguments
			      (this.settings, settings.getPdfViaDvi(), texFile),
			      dviPdfFile);
        this.executorCtrl.setMatcher(MockControl.ARRAY_MATCHER);
        this.executorCtrl.setReturnValue(null);

	// FIXME: since log file does not exist 
	fileUtils.matchInFile(logFile, this.settings.getPatternErrLatex());
	fileUtilsCtrl.setReturnValue(Boolean.FALSE);
    }

    private void mockRunLatex2html() throws BuildFailureException {
        this.executor.execute(texFile.getParentFile(),
			      this.settings.getTexPath(),
			      this.settings.getTex4htCommand(),
			      LatexProcessor.buildHtlatexArguments
			      (this.settings, texFile),
			      htmlFile);
        this.executorCtrl.setMatcher(MockControl.ARRAY_MATCHER);
        this.executorCtrl.setReturnValue(null);

	// html
	fileUtils.replaceSuffix(this.texFile, LatexProcessor.SUFFIX_HTML);
	fileUtilsCtrl.setReturnValue(this.htmlFile);

	// logging 
	fileUtils.matchInFile(logFile, this.settings.getPatternErrLatex());
	fileUtilsCtrl.setReturnValue(Boolean.FALSE);
	fileUtils.matchInFile(logFile, LatexProcessor.PATTERN_OUFULL_HVBOX);
	fileUtilsCtrl.setReturnValue(Boolean.FALSE);
	fileUtils.matchInFile(logFile, this.settings.getPatternWarnLatex());
	fileUtilsCtrl.setReturnValue(Boolean.FALSE);
    }
}
