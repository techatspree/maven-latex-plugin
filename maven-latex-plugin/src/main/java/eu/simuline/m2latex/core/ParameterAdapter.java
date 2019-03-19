
package eu.simuline.m2latex.core;

import java.util.SortedSet;

/**
 * Common interface to pass parameters from ant and from maven. 
 * The core method is {@link #initialize()}. 
 * Note that both implementations, the one of an ant task 
 * and the one for the maven plugin implement a method execute() 
 * but throwing specific exceptions. 
 * <p> 
 * TODO: describe getTarget()
 *
 * @see eu.simuline.m2latex.antTask.LatexCfgTask
 * @see eu.simuline.m2latex.mojo.AbstractLatexMojo
 */
public interface ParameterAdapter {

    /**
     * Sets up the parameters and initializes 
     * {@link eu.simuline.m2latex.core.LatexProcessor}. 
     */
    void initialize();

    /**
     * Returns the set of target. 
     * FIXME: Better would be Enum set but best a sorted kind of EnumSet
     * The set is an EnumSet and thus in a sense sorted, 
     * although not imlementing SortedSet: *****
     * The iterator returned by the iterator method 
     * traverses the elements in their natural order 
     * (the order in which the enum constants are declared). 
     * TODO: generalize to more than one target. 
     */
    SortedSet<Target> getTargetSet();
}
