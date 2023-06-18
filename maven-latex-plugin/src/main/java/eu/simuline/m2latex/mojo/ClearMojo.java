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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import org.apache.maven.plugin.MojoFailureException;

import java.util.SortedSet;
// import java.util.EnumSet;

/**
 * Clears all created files in the folders containing the LaTeX sources 
 * for the goal <code>clr</code> 
 * tied to the lifecycle phase <code>clean</code>. 
 */
@Mojo(name = "clr", defaultPhase = LifecyclePhase.CLEAN)
// TBD: research: 
// https://maven.apache.org/plugin-tools/maven-plugin-tools-annotations/index.html
// @Execute( goal = "<goal-name>",
//           phase = LifecyclePhase.<phase>,
//           lifecycle = "<lifecycle-id>" )
public class ClearMojo extends AbstractLatexMojo {

  // api-docs inherited from ParameterAdapter 
  // FIXME: not required by ClearMojo, GraphicsMojo, ChkMojo  
  public SortedSet<Target> getTargetSet() {
    throw new IllegalStateException();
  }

  /**
   * Invoked by maven executing the plugin. 
   * <p>
   * Logging: 
   * <ul>
   * <li> WPP02: tex file may be latex main file 
   * <li> WFU01: Cannot read directory...
   * <li> WFU03: cannot close tex file 
   * <li> EFU05: Failed to delete file 
   * </ul>
   *
   * @throws BuildFailureException 
   *    TSS02 if the tex source processing directory does either not exist 
   *    or is not a directory. 
   */
  public void execute() throws MojoFailureException {
    initialize();
    try {
      // may throw BuildFailureException TSS02 
      // may log warnings WPP02, WFU01, WFU03, EFU05 
      this.latexProcessor.clearAll();
    } catch (BuildFailureException e) {
      throw new MojoFailureException(e.getMessage(), e.getCause());
    }
  }

}
