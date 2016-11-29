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
import org.apache.maven.plugins.annotations.LifecyclePhase;

import org.apache.maven.plugin.MojoFailureException;

import java.util.SortedSet;
//import java.util.EnumSet;

/**
 * Clears all created files in the folders containing the LaTeX sources.
 */
@Mojo(name = "clr", defaultPhase = LifecyclePhase.CLEAN)
public class ClearMojo extends AbstractLatexMojo {

    // api-docs inherited from ParameterAdapter 
    // FIXME: not required by ClearMojo, GraphicsMojo  
     public SortedSet<Target> getTargetSet() {
    	throw new IllegalStateException();
    }

    /**
     * Invoked by maven executing the plugin. 
     */
    public void execute() throws MojoFailureException {
	initialize();
	try {
	    this.latexProcessor.clearAll();
	} catch (BuildFailureException e) {
	    throw new MojoFailureException(e.getMessage(), e.getCause());
	}
    }

}
