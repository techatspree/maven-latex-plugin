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

import org.apache.maven.plugins.annotations.Mojo;

/**
 * Build HTML documents and XHTML documents from LaTeX sources.
 *
 * @goal html
 * @phase site
 */
//@Mojo( name = "html")
public class HtmlMojo extends AbstractLatexMojo {

    private static final String[] HTML_OUTPUT_FILES = new String[] {
	"*.html", "*.xhtml", "*.htm", ".css", "*.png", "*.svg"
    };

    // implements AbstractLatexMojo#processSource(File)
    public void processSource(File texFile) throws BuildExecutionException {
	this.latexProcessor.processLatex2html(texFile);
    }

    // implements AbstractLatexMojo#getOutputFileSuffixes()
    public String[] getOutputFileSuffixes() {
	return HTML_OUTPUT_FILES;
    }
}
