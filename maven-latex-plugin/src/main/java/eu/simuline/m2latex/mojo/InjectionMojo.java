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
import eu.simuline.m2latex.core.Injection;
import eu.simuline.m2latex.core.Settings;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import java.util.Set;
import org.apache.maven.plugin.MojoFailureException;

// documentation occurs in latex:help
/**
 * Creates rc file <code>.latexmkrc</code> for latexmk 
 * and <code>.chktexrc</code> for chktex and further files for various use cases. 
 * The goal is tied to the lifecycle phase <code>validate</code> by default.  
 */
@Mojo(name = "inj", defaultPhase = LifecyclePhase.VALIDATE)
// TBD: maybe verify
// in fact, Metainfo can give more info than just versioning 
public class InjectionMojo extends AbstractLatexMojo {

  /**
   * Indicates the files injected by the goal <code>inj</code>. 
   * This is a comma separated list of {@link Injection}s 
   * without blanks. 
   * The injections are in one of three categories: representing 
   * <ul>
   * <li>configuration files: 
   *  <ul>
   *  <li><code>latexmkrc</code> represents the config file <code>.latexmkrc</code> 
   *      of <code>latexmk</code></li>
   *  <li><code>chktexrc</code> represents the config file <code>.chktexrc</code> 
   *      of <code>chktex</code></li>
   *  </ul></li>
   * <li>header files to be input into TEX files: 
   *  <ul>
   *  <li><code>header</code> represents the file <code>header.tex</code> 
   *      which is intended to be included in any latex main file. </li>
   *  <li><code>headerGrp</code> represents the file <code>headerGrp.tex</code> 
   *      which shall be included in latex main files using the <code>graphicx</code> pacakge. </li>
   *  <li><code>headerSuppressMetaPDF</code> represents the file <code>headerSuppressMetaPDF.tex</code> 
   *      which serves to suppress meta info of a latex main file  
   *      which may be considered a safety risk. </li>
   *  </ul></li>
   * <li>script files: 
   *  <ul>
   *  <li><code>vscodeExt</code> represents the file <code>instVScode4tex.sh</code> 
   *      installing all extensions on VS Code recommended for development of latex documents. </li>
   *  <li><code>ntlatex</code> represents the file <code>ntlatex</code> 
   *      performing latex compilation with epoch time 0. </li>
   *  <li><code>pythontexW</code> represents the file <code>pythontexW</code> 
   *      which essentially invokes <code>pythontex</code> 
   *      and redirects output to a log file</li>
   *  <li><code>vmdiff</code> represents the file <code>vmdiff</code> 
   *      which performs a diff based on 
   *      the visual diff <code>diff-pdf-visually</code> and 
   *      on the diff on meta-data given by <code>pdfinfo</code>. </li>
   *  </ul></li>
   * </ul>
   * <p>
   * The default value is <code>latexmkrc,chktexrc</code>. 
   */
  @Parameter(name = "injections", defaultValue = "latexmkrc,chktexrc",
      property = "latex.injections")
  private Set<Injection> injections;

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
      this.latexProcessor.processFileInjections(this.injections);
    } catch (BuildFailureException e) {
      // may throw Exception TBD
      throw new MojoFailureException(e.getMessage(), e.getCause());
    }
  }

}
