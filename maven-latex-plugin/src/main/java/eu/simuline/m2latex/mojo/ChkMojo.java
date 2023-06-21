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

import org.apache.maven.plugins.annotations.Mojo;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Checks all latex main files after having created all graphical files. 
 * <p>
 * Defines the goal <code>chk</code> which is not tied to a lifecycle phase. 
 */
@Mojo(name = "chk") // maybe , defaultPhase = LifecyclePhase.POST-SITE
// https://maven.apache.org/plugin-tools/maven-plugin-tools-annotations/index.html
public class ChkMojo extends CfgLatexMojo {

   public SortedSet<Target> getTargetSet() {
       SortedSet<Target> res = new TreeSet<Target>();
       res.add(Target.chk);
       return res;
       //return EnumSet.of(Target.dvi);
   }
}
// public class ChkMojo extends AbstractLatexMojo {

//   /**
//    * Invoked by maven executing the plugin. 
//    * <p>
//    * Logging: 
//    * <ul>
//    * <li>WPP02: tex file may be latex main file 
//    * <li>WPP05: Included tex files which are no latex main files 
//    * <li>WPP06: Included tex files which are no latex main files 
//    * <li>WPP07: inluded/excluded files not identified by their names.
//    * <li>WFU01: Cannot read directory...
//    * <li>WFU03: cannot close tex file 
//    * <li>EFU05: Failed to delete file 
//    * <li>EFU07, EFU08, EFU09: if filtering a file fails. 
//    * </ul>
//    *
//    * @throws BuildFailureException 
//    *    TSS02 if the tex source processing directory does either not exist 
//    *    or is not a directory. 
//    */
//   public void execute() throws MojoFailureException {
//     initialize();
//     try {
//       // may throw BuildFailureException TSS02 
//       // may log warnings WPP02, WPP05, WPP06, WPP07, 
//       // WFU01, WFU03, EFU05 
//       // EFU07, EFU08, EFU09: if filtering a file fails. 
//       this.latexProcessor.checkAll();
//     } catch (BuildFailureException e) {
//       throw new MojoFailureException(e.getMessage(), e.getCause());
//     }
//   }

// }
