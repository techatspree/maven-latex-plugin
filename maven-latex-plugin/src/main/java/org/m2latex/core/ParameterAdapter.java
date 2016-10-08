
package org.m2latex.core;

import java.util.Set;

/**
 * Common interface to pass parameters from ant and from maven. 
 * The core method is {@link #initialize()}. 
 * Note that both implementations, the one of an ant task 
 * and the one for the maven plugin implement a method execute() 
 * but throwing specific exceptions. 
 * <p> 
 * TODO: describe getTarget()
 *
 * @see org.m2latex.antTask.LatexTask
 * @see org.m2latex.mojo.AbstractLatexMojo
 */
public interface ParameterAdapter {

    /**
     * Sets up the parameters and initializes 
     * {@link org.m2latex.core.LatexProcessor}. 
     */
    void initialize();

    /**
     * Returns the single target. 
     * TODO: generalize to more than one target. 
     */
    Set<Target> getTargetSet();
}
