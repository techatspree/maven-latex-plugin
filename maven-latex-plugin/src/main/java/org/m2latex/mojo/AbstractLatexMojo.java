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

/**
 * Abstract base class for all mojos. 
 *
 */
abstract class AbstractLatexMojo extends AbstractMojo 
    implements ParameterAdapter {

    /**
     * The base directory of this maven project. 
     * Reinitializes {@link Settings#baseDirectory} via {@link #initialize()}. 
     * 
     * @parameter property="basedir"
     * @readonly
     */
    protected File baseDirectory;

    /**
     * The target directory of this maven project. 
     * Reinitializes {@link Settings#targetDirectory} 
     * via {@link #initialize()}. 
     * 
     * @parameter property="project.build.directory"
     * @readonly
     */
    protected File targetDirectory;

    /**
     * The target site directory of this maven project. 
     * Reinitializes {@link Settings#baseDirectory} via {@link #initialize()}. 
     * 
     * @parameter property="project.reporting.outputDirectory"
     * @readonly
     */
    protected File targetSiteDirectory;

    /**
     * Contains all parameters for executing this maven plugin. 
     * If not set in the pom prior to execution, 
     * is set in {@link #initialize()}. 
     * 
     * @parameter
     */
    protected Settings settings;

    // set by {@link #initialize()}. 
    protected LatexProcessor latexProcessor;


    // api-docs inherited from ParameterAdapter 
    public void initialize() {
	if (this.settings == null) {
	    // Here, no configuration is defined in pom, 
	    // i.e. object is not created by Maven
	    this.settings = new Settings();
	}
	this.settings.setBaseDirectory( this.baseDirectory );
	this.settings.setTargetSiteDirectory( this.targetSiteDirectory );
	this.settings.setTargetDirectory( this.targetDirectory );

	this.latexProcessor = new LatexProcessor(this.settings,  
						 new MavenLogWrapper(getLog()), 
						 this);
    }
}
