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

import eu.simuline.m2latex.core.BuildFailureException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import org.apache.maven.plugin.MojoFailureException;

// documentation occurs in latex:help
/**
 * Creates rc file <code>.latexmkrc</code> for latexmk 
 * and <code>.chktexrc</code> for chktex. 
 * The goal is tied to the lifecycle phase <code>validate</code> by default.  
 */
@Mojo(name = "dvl", defaultPhase = LifecyclePhase.VALIDATE)
// TBD: maybe verify
// in fact, Metainfo can give more info than just versioning 
public class RecordFilesMojo extends AbstractLatexMojo {

  /**
   * Creates rc file <code>.latexmkrc</code> for latexmk 
   * and <code>.chktexrc</code> for chktex 
   * and moves to base directory of tex sources {@link Settings#texSourceDirectory}. 
   *
   * @throws MojoFailureException
   *    <ul>
   *    <li>TMI01: if the stream to either the manifest file 
   *        or to a property file, either {@LINK #VERSION_PROPS_FILE} 
   *        or {@link eu.simuline.m2latex.core.MetaInfo.GitProperties#GIT_PROPS_FILE} could not be created. </li>
   *    <li>TMI02: if the properties could not be read 
   *        from one of the two property files mentioned above. </li>
   *    <li>TSS05: if converters are excluded in the pom which are not known. </li>
   *    </ul>
   */
  public void execute() throws MojoFailureException {
    // TBD: redesign 
    initialize();
    try {
      // TBD: update 
      // warnings: WMI01, WMI02, 
      // may throw build failure exception TSS XXXX TBD
      this.latexProcessor.processRcFiles();
    } catch (BuildFailureException e) {
      // may throw Exception TBD
      throw new MojoFailureException(e.getMessage(), e.getCause());
    }
  }

}
