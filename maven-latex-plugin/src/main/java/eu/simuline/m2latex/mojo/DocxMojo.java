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

import java.util.SortedSet;
import java.util.TreeSet;
//import java.util.EnumSet;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * Build documents in msword formats, above all docx from LaTeX sources 
 * for the goal <code>docx</code> which is not tied to a lifecycle phase. 
 */
@Mojo(name = "docx")
public class DocxMojo extends CfgLatexMojo {

    public SortedSet<Target> getTargetSet() {
	SortedSet<Target> res = new TreeSet<Target>();
	res.add(Target.docx);
	return res;
	//return EnumSet.of(Target.docx);
    }


}
