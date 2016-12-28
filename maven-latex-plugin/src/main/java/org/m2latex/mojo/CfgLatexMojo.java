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

import org.m2latex.core.BuildFailureException;
import org.m2latex.core.ParameterAdapter;
import org.m2latex.core.Target;

import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import java.util.SortedSet;

/**
 * Builds documents in the formats configured in the pom from LaTeX sources. 
 * Defines the goal <code>cfg</code> 
 * and adds it to the lifecycle phase <code>site</code>. 
 */
@Mojo(name = "cfg", defaultPhase = LifecyclePhase.SITE)
public class CfgLatexMojo extends AbstractLatexMojo {

    // api-docs inherited from ParameterAdapter 
    // FIXME: not required by ClearMojo 
    public SortedSet<Target> getTargetSet() {
	return this.settings.getTargetSet();
    }

    /**
     * Invoked by maven executing the plugin. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU01: Cannot read directory... 
     * <li> WFU03: cannot close
     * <li> EFU05: Cannot delete
     * <li> EFU06: Cannot move file 
     * <li> WPP02: tex file may be latex main file 
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> EEX01, EEX02, EEX03, WEX04, WEX05: 
     *      applications for preprocessing graphic files 
     *      or processing a latex main file fails. 
     * </ul>
     * @throws MojoFailureException
     *    FIXME 
     */
    public void execute() throws MojoFailureException {
	initialize();
	try {
	    // may throw BuildFailureException FIXME 
	    // may log WFU01, WFU03, EFU05, EFU06, 
	    // WPP02, WPP03, 
	    // EEX01, EEX02, EEX03, WEX04, WEX05 
	    this.latexProcessor.create();
	} catch (BuildFailureException e) {
	    throw new MojoFailureException(e.getMessage(), e.getCause());
	}
    }

}
