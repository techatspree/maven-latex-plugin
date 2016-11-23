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

import java.io.File;

import java.util.SortedSet;

import org.m2latex.core.LatexProcessor;
import org.m2latex.core.BuildFailureException;
import org.m2latex.core.BuildExecutionException;
import org.m2latex.core.ParameterAdapter;
import org.m2latex.core.Settings;
import org.m2latex.core.Target;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;

/**
 * Builds documents in the formats configured in the pom. 
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
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
	initialize();
	try {
	    this.latexProcessor.create();
	} catch (BuildExecutionException e) {
	    throw new MojoExecutionException(e.getMessage(), e.getCause());
	} catch (BuildFailureException e) {
	    throw new MojoFailureException(e.getMessage(), e.getCause());
	}
    }

}
