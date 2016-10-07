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

package org.m2latex.core;

import java.io.File;

import junit.framework.TestCase;

import org.apache.maven.plugin.logging.SystemStreamLog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CommandExecutorImplTest
{
    @Test public void testExecute()
        throws Exception
    {
        CommandExecutorImpl executor = 
	    new CommandExecutorImpl(new MavenLogWrapper(new SystemStreamLog()));
        String echoText = "LaTeX";
        String output = executor.execute( new File( "." ), 
					  null, 
					  "echo", new String[] { echoText } );
        assertEquals( echoText, output.subSequence( 0, echoText.length() ) );
    }
}
