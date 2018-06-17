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
 * Displays a help text defining for the goal <code>help</code> 
 * which is not tied to a lifecycle phase. 
 */
@Mojo(name = "help")
public class HelpMojo extends AbstractLatexMojo {

    // api-docs inherited from ParameterAdapter 
    // FIXME: not required by ClearMojo, GraphicsMojo, ChkMojo, HelpMojo  
     public SortedSet<Target> getTargetSet() {
    	throw new IllegalStateException();
    }

    /**
     * Invoked by maven executing the plugin. 
     * <p>
     * No logging. 
     *
     */
    public void execute() throws MojoFailureException {
	System.out.println("This plugin has the following goals: ");
	System.out.println("- chk  to check latex sources ");
	System.out.println("- clr  to clear artifacts in the working directory ");
	System.out.println("- docx to create doc(x) documents ");
	System.out.println("- dvi  to create dvi documents ");
	System.out.println("- grp  to perform preprocessing (above all graphics) ");
	System.out.println("- help to obtain this help message ");
	System.out.println("- html to create (x)html documents ");
	System.out.println("- odt  to create open document documents ");
	System.out.println("- rtf  to create rtf documents ");
	System.out.println("- txt  to create formatted text documents ");



	// **** explanation for each parameter is required. 
	// but this is only if parameter -Ddetail=true 
	// To this end: new parameter in configuration 
    }

}
