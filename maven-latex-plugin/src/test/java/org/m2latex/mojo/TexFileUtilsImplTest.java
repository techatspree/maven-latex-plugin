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
