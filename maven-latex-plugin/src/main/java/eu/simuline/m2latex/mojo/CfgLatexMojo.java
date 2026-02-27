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
import eu.simuline.m2latex.core.Target;
import eu.simuline.m2latex.core.LatexProcessor;

import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import java.util.SortedSet;

// documentation occurs in latex:help
/**
 * Builds documents in the formats configured in the pom from LaTeX sources. 
 * The goal is tied to the lifecycle phase <code>site</code> by default. 
 */
@Mojo(name = "cfg", defaultPhase = LifecyclePhase.SITE)
// https://maven.apache.org/plugin-tools/maven-plugin-tools-annotations/index.html
public class CfgLatexMojo extends AbstractLatexMojo {

  public SortedSet<Target> getTargetSet() throws BuildFailureException {
    return this.settings.getTargets();
  }

  /**
   * Invoked by maven executing the plugin. 
   * <p>
   * Logging: 
   * <ul>
   * <li> WFU01: Cannot read directory... 
   * <li> WFU03: cannot close
   * <li> EFU05: Cannot delete
   * <li> EFU07, EFU08, EFU09: if filtering a file fails. 
   * <li> WPP02: tex file may be latex main file 
   * <li> WPP03: Skipped processing of files with suffixes ... 
   * <li> WPP05: Included tex files which are no latex main files 
   * <li> WPP06: Included tex files which are no latex main files 
   * <li> WPP07: inluded/excluded files not identified by their names.
   * <li> EEX01, EEX02, EEX03, WEX04, WEX05: 
   *      applications for preprocessing graphic files 
   *      or processing a latex main file fails. 
   * </ul>
   * @throws MojoFailureException
   *    <ul>
   *    <li> TSS01 if 
   *    the tex source directory does either not exist 
   *    or is not a directory. 
   *    <li> TSS02 if 
   *    the tex source processing directory does either not exist 
   *    or is not a directory. 
   *    <li> TSS03 if 
   *    the output directory exists and is no directory. 
   *    <li> TEX01 if 
   *    invocation of applications for preprocessing graphic files 
   *    or processing a latex main file fails 
   *    <li> TFU01 if 
   *    the target directory that would be returned 
   *    exists already as a regular file. 
   *    <li> TFU03, TFU04, TFU05, TFU06 if 
   *    copy of output files to target folder fails. 
   *    For details see {@link LatexProcessor#create(SortedSet<Target>)}. 
   *    </ul>
   */
  public void execute() throws MojoFailureException {
    initialize();
    try {
      // may throw BuildFailureException 
      // TSS01, TSS02, TSS03, TEX01, TFU01, TFU03, TFU04, TFU05, TFU06, TLP01 
      // may log WFU01, WFU03, EFU05, EFU07, EFU08, EFU09 
      // WPP02, WPP03, WPP05, WPP06, WPP07, 
      // EEX01, EEX02, EEX03, WEX04, WEX05 
      this.latexProcessor.create(getTargetSet());
    } catch (BuildFailureException e) {
      throw new MojoFailureException(e.getMessage(), e.getCause());
    }
  }

}
