package org.m2latex.core;

import java.io.File;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * Describe class DirNode here.
 *
 *
 * Created: Tue Dec  6 03:05:24 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public class DirNode {

    // null iff this DirNode is invalid according to isValid() 
    private final Set<File> regularFiles;
    private final Map<String, DirNode> name2node;

    /**
     * Creates a new <code>DirNode</code> instance.
     * <p>
     * Logging: 
     * WFU01 Cannot read directory 
     *
     * @param dir
     *    The directory this node represents 
     *    including subdirectories recursively. 
     *    This is the latex source directory or a subdirectory recursively. 
     * @param fileUtils
     *    
     */
    public DirNode(File dir, TexFileUtils fileUtils) {
	assert dir.isDirectory();
	// may log WFU01 Cannot read directory 
	File[] files = fileUtils.listFilesOrWarn(dir);
	if (files == null) {
	    // Here, this node is irregular 
	    this.regularFiles = null;
	    this.name2node = null;
	    return;
	}
	this.regularFiles = new TreeSet<File>();
	this.name2node = new TreeMap<String, DirNode>();
	DirNode node;
	for (File file : files) {
	    assert file.exists();
	    if (file.isDirectory()) {
		// may log WFU01 Cannot read directory 
		node = new DirNode(file, fileUtils);
		if (node.isValid()) {
		    this.name2node.put(file.getName(), node);
		}
	    } else {
		// FIXME: skip hidden files 
		this.regularFiles.add(file);
	    }
	}
    }

    boolean isValid() {
	assert (this.regularFiles == null) == (this.name2node == null);
	return this.regularFiles != null;
    }

    Set<File> getRegularFiles() {
	return this.regularFiles;
    }

    Map<String, DirNode> getSubdirs() {
	return this.name2node;
    }


    /**
     * Deletes all files in <code>currNode</code> 
     * which are not in <code>this</code> recursively including subdirectories. 
     * The background is, that <code>this</code> represents the files 
     * originally in the directory and <code>currNode</code> 
     * the current ones at the end of the creating goal. 
     *
     * @param currNode
     *    the node representing the current files. 
     *    This is the latex source directory or a subdirectory. 
     */
     // FIXME: warn if deletion failed. 
    // used in TexFileUtilsImpl.cleanUp only 
    void cleanUpRec(DirNode currNode) {
    	assert this.name2node.keySet().equals(currNode.getSubdirs().keySet());
    	for (String key : getSubdirs().keySet()) {
    	    this.getSubdirs().get(key)
    		.cleanUpRec(currNode.getSubdirs().get(key));
    	}
    	Collection<File> currFiles = currNode.getRegularFiles();
    	currFiles.removeAll(this.getRegularFiles());
    	for (File file : currFiles) {
    	    // FIXME: should be: deleteOrWarn 
    	    file.delete();
    	}
     }

}
