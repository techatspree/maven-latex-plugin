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

package org.m2latex.mojo;

import org.m2latex.core.Target;
import org.m2latex.core.BuildFailureException;

import org.apache.maven.plugins.annotations.Mojo;

import org.apache.maven.plugin.MojoFailureException;

import java.util.SortedSet;
//import java.util.EnumSet;

/**
 * Transforms all graphic files into formats 
 * which can be included into LaTeX files.
 * Defines the goal <code>grp</code> which is not tied to a lifecycle phase. 
 */
@Mojo(name = "grp")
public class GraphicsMojo extends AbstractLatexMojo {

    // api-docs inherited from ParameterAdapter 
    // FIXME: not required by ClearMojo, GraphicsMojo 
     public SortedSet<Target> getTargetSet() {
    	throw new IllegalStateException();
    }

    /**
     * Invoked by maven executing the plugin. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU01: Cannot read directory 
     * <li> WFU03: cannot close 
     * <li> WPP02: tex file may be latex main file 
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> EEX01, EEX02, WEX03, WEX04, WEX05: 
     * if running graphic processors failed. 
     * </ul>
     *
     * @throws MojoFailureException
     *    <ul>
     *    <li> 
     *    TSS01 if the tex source directory does either not exist 
     *    or is not a directory. 
     *    <li> 
     *    TEX01 invoking FIXME
     *    </ul>
     */
    public void execute() throws MojoFailureException  {
	initialize();
	try {
	    // may throw BuildFailureException TSS01, TEX01 
	    // may log warnings WFU01, WFU03, WPP02, WPP03, 
	    // EEX01, EEX02, WEX03, WEX04, WEX05: 
	    this.latexProcessor.processGraphics();
	} catch (BuildFailureException e) {
	    throw new MojoFailureException(e.getMessage(), e.getCause());
	}
    }
}
