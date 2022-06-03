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
//import java.util.EnumSet;

/**
 * Build HTML documents and XHTML documents from LaTeX sources 
 * for the goal <code>html</code> which is not tied to a lifecycle phase. 
 */
@Mojo(name = "html")
public class HtmlMojo extends CfgLatexMojo {

   public SortedSet<Target> getTargetSet() {
       SortedSet<Target> res = new TreeSet<Target>();
       res.add(Target.html);
       return res;
       //return EnumSet.of(Target.html);
   }
}
