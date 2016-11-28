/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.filefilter;

import java.io.File;

/**
 * This filter accepts <code>File</code>s that are files (not directories).
 * <p>
 * For example, here is how to print out a list of the real files
 * within the current directory:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( FileFileFilter.FILE );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since Commons IO 1.3
 * @version $Revision: 155419 $ $Date: 2006-08-28 08:39:07 +0200 (Mo, 28 Aug 2006) $
 */
public class FileFileFilter extends AbstractFileFilter {

    /** Singleton instance of file filter */
    public static final IOFileFilter FILE = new FileFileFilter();

    /**
     * Restrictive consructor.
     */
    protected FileFileFilter() {
    }

    /**
     * Checks to see if the file is a file.
     *
     * @param file  the File to check
     * @return true if the file is a file
     */
    public boolean accept(File file) {
        return file.isFile();
    }

}
