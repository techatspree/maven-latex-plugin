package org.m2latex.mojo;

import java.io.File;

import org.codehaus.plexus.util.cli.CommandLineException;

public interface CommandExecutor
{

    public abstract String execute( File workingDir, File pathToExecutable, String executable, String[] args )
        throws CommandLineException;

}