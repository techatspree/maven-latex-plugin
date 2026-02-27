
package eu.simuline.m2latex.core;


// TBD: re-add
 
/**
 * Common interface to pass parameters from ant and from maven. 
 * The core method is {@link #initialize()}. 
 * Note that both implementations, the one of an ant task 
 * and the one for the maven plugin implement a method execute() 
 * but throwing specific exceptions. 
 *
 * TODO: eliminate this interface
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

}
