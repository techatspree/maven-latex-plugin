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
// import java.io.FileWriter;
// import java.io.Writer;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.InOrder;

import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.After;
//import org.junit.BeforeClass;
//import org.junit.AfterClass;

// FIXME: missing: test of logging 
// FIXME: mocking and verification in parallel 
// FIXME: rename: mock-->stub 
// FIXME: missing: test of warnings and errors 
public class LatexProcessorTest {

    private final static File WORKING_DIR = 
	new File(System.getProperty("testResourcesDir"));

    private final CommandExecutor executor = mock(CommandExecutor.class,
					    RETURNS_SMART_NULLS);
    private final InOrder inOrderExec = inOrder(this.executor);

    private final TexFileUtils fileUtils = mock(TexFileUtils.class,
					  RETURNS_SMART_NULLS);

    private final InOrder inOrderFileUtils = inOrder(this.fileUtils);


    private final Settings settings = new Settings();

    // mock 
    // - ignores debug and info 
    // - verifies error and warn
//     private LogWrapper log = mock(MavenLogWrapper.class,
// 				  new Answer<Void>() {
// 	public Void answer(InvocationOnMock invocation) throws Throwable {
// System.out.println("answer..");
	    
// 	    String methodName = invocation.getMethod().getName();
// System.out.println("methodName: "+methodName);
// 	    if (methodName.equals("debug") || methodName.equals("info")) {
// //		invocation.callRealMethod();
// 	    } else {
// 		assert methodName.equals("error") || methodName.equals("warn");
// 		LogWrapper lw = (LogWrapper)invocation.getMock();
// 		if (methodName.equals("error")) {
// 		    verify(lw).error(anyString());
// 		} else {
// 		    verify(lw).warn(anyString());
// 		}
// 		// Object[] args = invocation.getArguments();
// 		// Object mock = invocation.getMock();
// 	    }
// System.out.println("..answer");
// return null;
// 	}
//     });
//private LogWrapper log = mock(MavenLogWrapper.class, RETURNS_SMART_NULLS);
    private final LogWrapper log = 
	spy(new MavenLogWrapper(new SystemStreamLog()));

    private final LatexProcessor processor = new LatexProcessor
	(this.settings, this.executor, this.log,this.fileUtils,new PdfMojo());

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

    public LatexProcessorTest() {
	doThrow(new AssertionError("Found error. "))
	    .when(this.log).error(anyString());
	doThrow(new AssertionError("Found warning. "))
	    .when(this.log).warn(anyString());
	doCallRealMethod().when(this.log).info(anyString());
	doNothing().when(this.log).debug(anyString());
    }

    // FIXME: occurs also in other testclasses: 
    // to be unified. 
    private static void cleanWorkingDir() {
	assert WORKING_DIR.isDirectory() : "Expected directory. ";
	File[] files = WORKING_DIR.listFiles();
	assert files != null : "Working directory is not readable. ";
	for (File file : files) {
	    if (!file.isHidden()) {
		file.delete();
	    }
	}
    }

    @Before public void setUp() throws IOException {
	cleanWorkingDir();
	//this.texFile.createNewFile();
	//this.auxFile.createNewFile();
	this.logFile.createNewFile();
	this.blgFile.createNewFile();
	//this.pdfFile.createNewFile();
    }

    @After public void tearDown() throws IOException {
	cleanWorkingDir();
    }

    //@Ignore 
    // FIXME: does not take pdfViaDvi into account 
    @Test public void testProcessLatexSimple() throws BuildFailureException {
	
	mockProcessLatex2pdf(false, false, false);

	this.processor.processLatex2pdf(this.texFile);
	verifyProcessLatex2pdf(false, false, false);
	verifyNoMoreInteractions(this.executor);
	verifyNoMoreInteractions(this.fileUtils);
	//verifyNoMoreInteractions(this.log);
    }

    //@Ignore 
    @Test public void testProcessLatexWithBibtex() 
	throws BuildFailureException {

	mockProcessLatex2pdf(true, false, false);

        this.processor.processLatex2pdf(this.texFile);
	verifyProcessLatex2pdf(true, false, false);
	verifyNoMoreInteractions(this.executor);
	verifyNoMoreInteractions(this.fileUtils);
	//verifyNoMoreInteractions(this.log);
    }

   //@Ignore 
    @Test public void testProcessLatex2html() throws BuildFailureException {

	mockProcessLatex2html(false, false, false);

	this.processor.processLatex2html(this.texFile);

	verifyProcessLatex2html(false, false, false);
	verifyNoMoreInteractions(this.executor);
	verifyNoMoreInteractions(this.fileUtils);
	//verifyNoMoreInteractions(this.log);
    }

    private void mockProcessLatex2pdf(boolean needBibtex,
				      boolean needMakeIndex,
				      boolean needMakeGlossaries) 
	throws BuildFailureException {
	
	mockConstrLatexMainDesc();
	assert !this.settings.getPdfViaDvi().isViaDvi();
	// FIXME: here should be mockProcessLatex2dev
	mockProcessLatex2devCore(needBibtex, needMakeIndex, needMakeGlossaries);

	when(this.fileUtils.matchInFile(this.logFile, 
					LatexProcessor.PATTERN_OUFULL_HVBOX))
	    .thenReturn(Boolean.FALSE);

	when(this.fileUtils.matchInFile(this.logFile, 
					this.settings.getPatternWarnLatex()))
	    .thenReturn(Boolean.FALSE);
    }

    private void verifyProcessLatex2pdf(boolean needBibtex,
					boolean needMakeIndex,
					boolean needMakeGlossary) 
	throws BuildFailureException {

	verifyConstrLatexMainDesc();
	assert !this.settings.getPdfViaDvi().isViaDvi();
	// FIXME: here should be verifyProcessLatex2dev
	verifyProcessLatex2devCore(needBibtex, needMakeIndex, needMakeGlossary);

	this.inOrderFileUtils.verify(this.fileUtils).matchInFile
	    (this.logFile, LatexProcessor.PATTERN_OUFULL_HVBOX);
	this.inOrderFileUtils.verify(this.fileUtils).matchInFile
	    (this.logFile, this.settings.getPatternWarnLatex());
    }

    private void mockProcessLatex2html(boolean needBibtex,
				       boolean needMakeIndex,
				       boolean needMakeGlossaries) 
	throws BuildFailureException {

	mockConstrLatexMainDesc();
	mockPreProcessLatex2dev(needBibtex, needMakeIndex, needMakeGlossaries);
        mockRunLatex2html();
    }

    private void verifyProcessLatex2html(boolean needBibtex,
					 boolean needMakeIndex,
					 boolean needMakeGlossary) 
	throws BuildFailureException {

	verifyConstrLatexMainDesc();
	verifyPreProcessLatex2dev(needBibtex, needMakeIndex, needMakeGlossary);
        verifyRunLatex2html();
    }

    private void mockConstrLatexMainDesc() {
	//this.desc = this.processor.getLatexMainDesc(this.texFile);

	File texFile = this.texFile;
  	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_VOID))
	    .thenReturn(this.xxxFile);
	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_PDF))
	    .thenReturn(this.pdfFile);
	when(this.fileUtils.replaceSuffix(texFile, 
					  "."+this.settings.getPdfViaDvi()
					  .getLatexLanguage()))
	    .thenReturn(this.dviPdfFile);
	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_LOG))
	    .thenReturn(this.logFile);

	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_IDX))
	    .thenReturn(this.idxFile);
	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_IND))
	    .thenReturn(this.indFile);
	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_ILG))
	    .thenReturn(this.ilgFile);

	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_GLS))
	    .thenReturn(this.glsFile);
	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_GLO))
	    .thenReturn(this.gloFile);
	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_GLG))
	    .thenReturn(this.glgFile);
    }

    private void verifyConstrLatexMainDesc() {
	// FIXME: doubling from mockConstrLatexMainDesc()
	String[] suffixes = new String[] {
	    LatexProcessor.SUFFIX_VOID,
	    LatexProcessor.SUFFIX_PDF,
	    "."+this.settings.getPdfViaDvi().getLatexLanguage(),
	    LatexProcessor.SUFFIX_LOG,
	    LatexProcessor.SUFFIX_IDX,
	    LatexProcessor.SUFFIX_IND,
	    LatexProcessor.SUFFIX_ILG,
	    LatexProcessor.SUFFIX_GLS,
	    LatexProcessor.SUFFIX_GLO,
	    LatexProcessor.SUFFIX_GLG
	};
	for (int idx = 0; idx < suffixes.length; idx++) {
	    // FIXME: should work also in order. 
	    //this.inOrderFileUtils.
	    verify(this.fileUtils, atLeastOnce())
		.replaceSuffix(this.texFile, suffixes[idx]);
	}
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
	// FIXME: to indicate whether makeindex must be run: 
	// method creates .idx or not: use Mockito.thenAnswer for that

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
	File texFile = this.texFile;
	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_TOC))
	    .thenReturn(this.tocFile);
	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_LOF))
	    .thenReturn(this.lofFile);
	when(this.fileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_LOT))
	    .thenReturn(this.lotFile);
    }

    private void verifyPreProcessLatex2dev(boolean needBibtex,
					   boolean needMakeIndex,
					   boolean needMakeGlossaries) 
	throws BuildFailureException {
	assert !needMakeIndex && !needMakeGlossaries;

	// run latex 
        verifyRunLatex();

	// run bibtex, makeIndex and makeGlossary by need 
	verifyRunBibtexByNeed(needBibtex);
	verifyRunMakeIndexByNeed(needMakeIndex);
	verifyRunMakeGlossaryByNeed(needMakeGlossaries);

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


	// FIXME: duplicate 
	String[] suffixes = new String[] {
	    LatexProcessor.SUFFIX_TOC,
	    LatexProcessor.SUFFIX_LOF,
	    LatexProcessor.SUFFIX_LOT
	};
	for (int idx = 0; idx < suffixes.length; idx++) {
	    this.inOrderFileUtils.verify(this.fileUtils)
		.replaceSuffix(this.texFile, suffixes[idx]);
	}
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
	} else {
	    mockNeedRun(false, this.settings.getPatternReRunLatex());
	    // enter for-loop... 
	    mockNeedRun(false, this.settings.getPatternReRunMakeIndex());
	    // since both conditions are false 
	    // exit for-loop 
 	}
    }

    // FIXME: currently, neither toc nor lof nor lot exist 
    private void verifyProcessLatex2devCore(boolean needBibtex,
					    boolean needMakeIndex,
					    boolean needMakeGlossary) 
	throws BuildFailureException {

	assert !needMakeIndex && !needMakeGlossary;
	assert !this.tocFile.exists() 
	    && !this.lofFile.exists() 
	    && !this.lotFile.exists();
	// FIXME: would be safer to define = -1 
	assert this.settings.getMaxNumReRunsLatex() == 5;

	verifyPreProcessLatex2dev(needBibtex, needMakeIndex, needMakeGlossary);
	// preProcessLatex2dev returns 
	// 2 if needBibtex 
	// since currently neither makeindex nor makeglossaries are supported 
	// and neither toc, lof or lot exist: 
	// 0 else 

	if (needBibtex) {
	    // numLatexReRuns == 2 
	    verifyRunLatex();
	    // numLatexReRuns == 1 
	    // enter for-loop... 
	    verifyNeedRun(this.logFile, 
			  this.settings.getPatternReRunMakeIndex());
	    verifyRunLatex();
	    verifyNeedRun(this.logFile, 
			  this.settings.getPatternReRunLatex());
	    // second loop 
	    verifyNeedRun(this.logFile, 
			  this.settings.getPatternReRunMakeIndex());
	    // exit for-loop 
	} else {
	    verifyNeedRun(this.logFile, 
			  this.settings.getPatternReRunLatex());
	    // enter for-loop... 
	    verifyNeedRun(this.logFile, 
			  this.settings.getPatternReRunMakeIndex());
	    // since both conditions are false 
	    // exit for-loop 
	}
    }

    private void mockNeedRun(Boolean retVal, String pattern)
        throws BuildFailureException {

	when(this.fileUtils.matchInFile(this.logFile, pattern))
	    .thenReturn(retVal);
    }

    private void verifyNeedRun(File file, String pattern)
        throws BuildFailureException {
	// FIXME: should work also in order. 
	//this.inOrderFileUtils.
	verify(this.fileUtils, atLeastOnce()).matchInFile(file, pattern);
    }

    private void mockRunBibtexByNeed(Boolean runBibtex) 
	throws BuildFailureException {
	
	when(this.fileUtils.replaceSuffix
	     (this.texFile, LatexProcessor.SUFFIX_AUX))
	    .thenReturn(this.auxFile);
	when(this.fileUtils.matchInFile
	     (this.auxFile, LatexProcessor.PATTERN_NEED_BIBTEX_RUN))
	    .thenReturn(runBibtex);

	// FIXME: really needed?? 
//	Writer writer = new FileWriter(this.auxFile);
	if (!runBibtex) {
//	    writer.write("% no bibtex run");
//	    writer.flush();
	    return;
	}
//	writer.write("\\bibdata % bibtex run"); 
//	writer.flush();

	when(this.fileUtils.replaceSuffix(this.texFile, 
					  LatexProcessor.SUFFIX_BBL))
	    .thenReturn(this.bblFile);

	// when(this.executor.execute(texFile.getParentFile(),
	// 			   this.settings.getTexPath(),
	// 			   this.settings.getBibtexCommand(),
	// 			   LatexProcessor.buildArguments
	// 			   (this.settings.getBibtexOptions(), 
	// 			    this.auxFile),
	// 			   this.bblFile))
	//     .thenReturn("");

	// log file 
	when(this.fileUtils.replaceSuffix(this.texFile, 
					  LatexProcessor.SUFFIX_BLG))
	    .thenReturn(this.blgFile);
	// logging errors and warnings 
	when(this.fileUtils.matchInFile(this.blgFile, 
					this.settings.getPatternErrBibtex()))
	    .thenReturn(Boolean.FALSE);
	when(this.fileUtils.matchInFile(this.blgFile, 
					this.settings.getPatternWarnBibtex()))
	    .thenReturn(Boolean.FALSE);
    }

    private void verifyRunBibtexByNeed(Boolean runBibtex) 
	throws BuildFailureException {

	this.inOrderFileUtils.verify(this.fileUtils)
	    .replaceSuffix(this.texFile, LatexProcessor.SUFFIX_AUX);
	verifyNeedRun(this.auxFile,  LatexProcessor.PATTERN_NEED_BIBTEX_RUN);

	if (!runBibtex) {
	    return;
	}

	this.inOrderFileUtils.verify(this.fileUtils)
	    .replaceSuffix(this.texFile, LatexProcessor.SUFFIX_BBL);
	this.inOrderExec.verify(this.executor)
	    .execute(eq(WORKING_DIR),
		     isNull(),
		     eq(this.settings.getBibtexCommand()),
		     eq(LatexProcessor.buildArguments
			(this.settings.getBibtexOptions(), this.auxFile)),
		     eq(this.bblFile));

	// log file 
	this.inOrderFileUtils.verify(this.fileUtils)
	    .replaceSuffix(this.texFile, LatexProcessor.SUFFIX_BLG);
	// logging errors and warnings 
	this.inOrderFileUtils.verify(this.fileUtils)
	    .matchInFile(this.blgFile, this.settings.getPatternErrBibtex());
	this.inOrderFileUtils.verify(this.fileUtils)
	    .matchInFile(this.blgFile, this.settings.getPatternWarnBibtex());
    }

    private void mockRunMakeIndexByNeed(boolean runMakeIndex) 
	throws BuildFailureException {

	assert !runMakeIndex;

	if (!runMakeIndex) {
	    return;
	}
	mockRunMakeIndex();
    }

    private void verifyRunMakeIndexByNeed(boolean runMakeIndex) 
	throws BuildFailureException {

	assert !runMakeIndex;

	if (!runMakeIndex) {
	    return;
	}
	verifyRunMakeIndex();
    }

    private void mockRunMakeIndex() throws BuildFailureException {
	assert false;
	
	// when(this.executor.execute(this.texFile.getParentFile(),
	// 			   this.settings.getTexPath(),
	// 			   this.settings.getMakeIndexCommand(),
	// 			   LatexProcessor.buildArguments
	// 			   (this.settings.getMakeIndexOptions(), 
	// 			    this.idxFile),
	// 			   this.indFile))
	//     .thenReturn("");

	// since ilg file does not exist 
	when(this.fileUtils.matchInFile(this.ilgFile, 
					this.settings.getPatternErrMakeIndex()))
	    .thenReturn(Boolean.FALSE);
    }

    private void verifyRunMakeIndex() throws BuildFailureException {
	assert false;

	this.inOrderExec.verify(this.executor)
	    .execute(eq(WORKING_DIR),
		     isNull(),
		     eq(this.settings.getMakeIndexCommand()),
		     eq(LatexProcessor.buildArguments
			(this.settings.getMakeIndexOptions(), this.idxFile)),
		     eq(this.indFile));
	this.inOrderFileUtils.verify(this.fileUtils)
	    .matchInFile(this.ilgFile, this.settings.getPatternErrMakeIndex());
    }

    private void mockRunMakeGlossaryByNeed(boolean runMakeGlossaries) 
	throws BuildFailureException {
 
	assert !runMakeGlossaries;
	if (!runMakeGlossaries) {
	    return;
	}
	assert false;

	// when(this.executor.execute(this.texFile.getParentFile(),
	// 			   this.settings.getTexPath(),
	// 			   this.settings.getMakeGlossariesCommand(),
	// 			   LatexProcessor.buildArguments
	// 			   (this.settings.getMakeGlossariesOptions(), 
	// 			    xxxFile),
	// 			   glsFile))
	//     .thenReturn("");

	// since glg file does not exist 
	when(this.fileUtils.matchInFile
	     (this.glgFile, this.settings.getPatternErrMakeGlossaries()))
	    .thenReturn(Boolean.FALSE);
    }

    private void verifyRunMakeGlossaryByNeed(boolean runMakeGlossaries) 
	throws BuildFailureException {
 
	assert !runMakeGlossaries;
	if (!runMakeGlossaries) {
	    return;
	}
	assert false;

	this.inOrderExec.verify(this.executor)
	    .execute(eq(WORKING_DIR),
		     isNull(),
		     eq(this.settings.getMakeGlossariesCommand()),
		     eq(LatexProcessor.buildArguments
			(this.settings.getMakeGlossariesOptions(), 
			 this.xxxFile)),
		     eq(this.glsFile));
	this.inOrderFileUtils.verify(this.fileUtils)
	    .matchInFile(this.ilgFile, 
			 this.settings.getPatternErrMakeGlossaries());
    }

    private void mockRunLatex() throws BuildFailureException {

	// when(this.executor.execute(this.texFile.getParentFile(),
	// 			   this.settings.getTexPath(),
	// 			   this.settings.getLatex2pdfCommand(),
	// 			   LatexProcessor
	// 			   .buildLatexArguments(this.settings, 
	// 						this.settings
	// 						.getPdfViaDvi(), 
	// 						this.texFile),
	// 			   this.dviPdfFile))
	//     .thenReturn("");

	// Ensure that no failure occurred 
	// FIXME: missing: testcases with error 
	when(this.fileUtils.matchInFile(this.logFile, 
					this.settings.getPatternErrLatex()))
	    .thenReturn(Boolean.FALSE);
    }

    private void verifyRunLatex() throws BuildFailureException {
	
	// FIXME: should work also in order. 
	//this.inOrderExec.
	verify(this.executor, atLeastOnce())
	    .execute(eq(WORKING_DIR),
		     isNull(),
		     eq(this.settings.getLatex2pdfCommand()),
		     eq(LatexProcessor.buildLatexArguments
			(this.settings, 
			 this.settings.getPdfViaDvi(), 
			 this.texFile)),
		     eq(this.dviPdfFile));

	// FIXME: should work also in order. 
	//this.inOrderFileUtils.
	verify(this.fileUtils, atLeastOnce())
	    .matchInFile(this.logFile, this.settings.getPatternErrLatex());
    }

    private void mockRunLatex2html() throws BuildFailureException {
	// html to verify whether execution created the expected html file 
	when(this.fileUtils.replaceSuffix(this.texFile, 
					  LatexProcessor.SUFFIX_HTML))
	    .thenReturn(this.htmlFile);

	// when(this.executor.execute(this.texFile.getParentFile(),
	// 			   this.settings.getTexPath(),
	// 			   this.settings.getTex4htCommand(),
	// 			   LatexProcessor.buildHtlatexArguments
	// 			   (this.settings, this.texFile),
	// 			   this.htmlFile))
	//     .thenReturn("");


	// logging errors, bad boxes and other warnings 
	when(this.fileUtils.matchInFile(this.logFile, 
					this.settings.getPatternErrLatex()))
	    .thenReturn(Boolean.FALSE);
	when(this.fileUtils.matchInFile(this.logFile, 
					LatexProcessor.PATTERN_OUFULL_HVBOX))
	    .thenReturn(Boolean.FALSE);
	when(this.fileUtils.matchInFile(this.logFile, 
					this.settings.getPatternWarnLatex()))
	    .thenReturn(Boolean.FALSE);
    }

    private void verifyRunLatex2html() throws BuildFailureException {
	this.inOrderFileUtils.verify(this.fileUtils)
	    .replaceSuffix(this.texFile, LatexProcessor.SUFFIX_HTML);
	this.inOrderExec.verify(this.executor)
	    .execute(eq(WORKING_DIR),
		     isNull(),
		     eq(this.settings.getTex4htCommand()),
		     eq(LatexProcessor.buildHtlatexArguments
			 (this.settings, this.texFile)),
		     eq(this.htmlFile));
	String[] patterns = new String[] {
	    this.settings.getPatternErrLatex(),
	    LatexProcessor.PATTERN_OUFULL_HVBOX,
	    this.settings.getPatternWarnLatex()
	};
	for (int idx = 0; idx < patterns.length; idx++) {
	    this.inOrderFileUtils.verify(this.fileUtils)
		.matchInFile(this.logFile, patterns[idx]);
	}
    }
}
