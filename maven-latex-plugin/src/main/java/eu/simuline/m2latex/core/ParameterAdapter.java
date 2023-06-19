
package eu.simuline.m2latex.core;

import java.util.SortedSet;

// TBD: re-add
//  * @see eu.simuline.m2latex.antTask.LatexCfgTask
//  * @see eu.simuline.m2latex.mojo.AbstractLatexMojo
 
/**
 * Common interface to pass parameters from ant and from maven. 
 * The core method is {@link #initialize()}. 
 * Note that both implementations, the one of an ant task 
 * and the one for the maven plugin implement a method execute() 
 * but throwing specific exceptions. 
 * <p> 
 * TODO: describe getTarget()
 * TODO: redesign: split up in two interfaces: 
 * {@link initialize()} is needed by all goals/tasks, 
 * whereas  
 *
*/
public interface ParameterAdapter {

    /**
     * Sets up the parameters and initializes 
     * {@link eu.simuline.m2latex.core.LatexProcessor}. 
     */
    void initialize();

    /**
     * Returns the set of targets. 
     * FIXME: Better would be Enum set but best a sorted kind of EnumSet
     * The set is an EnumSet and thus in a sense sorted, 
     * although not implementing SortedSet: *****
     * The iterator returned by the iterator method 
     * traverses the elements in their natural order 
     * (the order in which the enum constants are declared). 
     *
     * @throws BuildFailureException
     *    if the configuration does not provide a valid target set.
     */
    SortedSet<Target> getTargetSet() throws BuildFailureException;
}
