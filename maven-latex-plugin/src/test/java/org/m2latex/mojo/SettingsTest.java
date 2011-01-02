package org.m2latex.mojo;

import java.io.File;

import junit.framework.TestCase;

public class SettingsTest
    extends TestCase
{
    public void testSettings()
        throws Exception
    {
        Settings settings = new Settings();
        settings.setBaseDirectory( new File( System.getProperty( "java.io.tmpdir" ) ) );
        assertNotNull( settings.getBaseDirectory() );

    }
}
