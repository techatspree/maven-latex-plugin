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

package eu.simuline.m2latex.mojo;

import eu.simuline.m2latex.core.Target;
import eu.simuline.m2latex.core.BuildFailureException;

import org.apache.maven.plugins.annotations.Mojo;

import org.apache.maven.plugin.MojoFailureException;

import java.util.SortedSet;
//import java.util.EnumSet;

/**
 * Transforms all graphic files into formats 
 * which can be included into LaTeX files 
 * for the goal <code>grp</code> which is not tied to a lifecycle phase. 
 */
@Mojo(name = "grp")
public class GraphicsMojo extends AbstractLatexMojo {

    /**
     * Invoked by maven executing the plugin. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU01: Cannot read directory 
     * <li> WFU03: cannot close file 
     * <li> EFU07, EFU08, EFU09: if filtering a file fails. 
     * <li> WPP02: tex file may be latex main file 
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> EEX01, EEX02, EEX03, WEX04, WEX05: 
     * if running graphic processors failed. 
     * </ul>
     *
     * @throws MojoFailureException
     *    <ul>
     *    <li> 
     *    TSS02 if the tex source processing directory does either not exist 
     *    or is not a directory. 
     *    <li> 
     *    TEX01 invoking FIXME
     *    </ul>
     */
    public void execute() throws MojoFailureException  {
	initialize();
	try {
	    // may throw BuildFailureException TSS02, TEX01 
	    // may log warnings WFU01, WFU03, WPP02, WPP03, 
	    // EEX01, EEX02, EEX03, WEX04, WEX05: EFU07, EFU08, EFU09
	    this.latexProcessor.processGraphics();
	} catch (BuildFailureException e) {
	    throw new MojoFailureException(e.getMessage(), e.getCause());
	}
    }
}
