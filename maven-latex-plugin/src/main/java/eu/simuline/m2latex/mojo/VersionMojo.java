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
import eu.simuline.m2latex.core.MetaInfo;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import org.apache.maven.plugin.MojoFailureException;

import java.util.SortedSet;
//import java.util.EnumSet;

/**
 * Displays version info of this plugin but above all on all converters 
 * (except makeindex). 
 */
@Mojo(name = "vrs", defaultPhase = LifecyclePhase.VALIDATE)
// TBD: maybe verify
// in fact, Metainfo can give more info than just versioning 
public class VersionMojo extends AbstractLatexMojo {

    // api-docs inherited from ParameterAdapter 
    // FIXME: not required by ClearMojo, GraphicsMojo, ChkMojo  
     public SortedSet<Target> getTargetSet() {
    	throw new IllegalStateException();
    }

     /**
      * Prints meta information, mainly version information 
      * on this software and on the converters used. 
      * <p>
      * WMI01: If the version string of a converter cannot be read. 
      * WMI02: If the version of a converter is not as expected. 
      * @return
      *    whether a warning has been issued. 
      * @throws MojoFailureException
      *    <ul>
      *    <li>TMI01: if the stream to either the manifest file 
      *        or to a property file, either {@LINK #VERSION_PROPS_FILE} 
      *        or {@link MetaInfo.GitProperties#GIT_PROPS_FILE} could not be created. </li>
      *    <li>TMI02: if the properties could not be read 
      *        from one of the two property files mentioned above. </li>
      *    <li>TSS05: if converters are excluded in the pom which are not known. </li>
      *    </ul>
      */
    public void execute() throws MojoFailureException {
	// TBD: redesign 
	initialize();
	try {
	    // warnings: WMI01, WMI02, 
	    // may throw build failure exception TSS05
	    this.latexProcessor.printMetaInfo(!this.getVersionsWarnOnly());
	} catch (BuildFailureException e) {
	    // may throw Exception TMI01, TMI02, TSS05
	    throw new MojoFailureException(e.getMessage(), e.getCause());
	}
    }

}
