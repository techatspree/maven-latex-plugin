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
import eu.simuline.m2latex.core.Settings;

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
   * and moves to base directory of tex sources {@link Settings#texSrcDirectory}. 
   *
   * @throws MojoFailureException
   *    <ul>
   *    <li>TMI01: if the stream to a template for an rc file could not be created. </li>
   *    <li>TLP03: if the rc file cannot be written completely and reliably for some reason. </li>
   *    </ul>
   */
  public void execute() throws MojoFailureException {
    // TBD: redesign 
    initialize();
    try {
      // TBD: update 
      // warnings: WMI01, WMI02, 
      // may throw build failure exception TLP03, TMI01
      this.latexProcessor.processRcFiles();
    } catch (BuildFailureException e) {
      // may throw Exception TBD
      throw new MojoFailureException(e.getMessage(), e.getCause());
    }
  }

}
