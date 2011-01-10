/*
 * The akquinet maven-latex-plugin project
 *
 * Copyright (c) 2011 by akquinet tech@spree GmbH
 *
 * The maven-latex-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The maven-latex-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the maven-latex-plugin. If not, see <http://www.gnu.org/licenses/>.
 */

package org.m2latex.mojo;

import java.io.File;

import junit.framework.TestCase;

import org.apache.maven.plugin.logging.SystemStreamLog;

public class TexFileUtilsImplTest
    extends TestCase
{
    private TexFileUtilsImpl utils = new TexFileUtilsImpl( new SystemStreamLog() );

    public void testGetTargetDir()
        throws Exception
    {
        File expected = new File( "/dir2/subdir" );
        File actual = utils.getTargetDirectory( new File( "/dir1/subdir/file" ), new File( "/dir1" ),
                                                new File( "/dir2" ) );
        assertEquals( expected, actual );
    }
}
