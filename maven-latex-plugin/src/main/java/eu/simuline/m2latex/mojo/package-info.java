/**
 * Code specific for the maven plugin defined by this latex converter. 
 * Note that the twin package 
 * specific for an according maven plugin is {@link eu.simuline.m2latex.antTask}, 
 * whereas the common code base is in {@link eu.simuline.m2latex.mojo}. 
 * <p>
 * This package consists of one class per target of this plugin, 
 * <ul>
 * <li>{@link CfgLatexMojo} for target <code>cfg</code></li>
 * <li>{@link ClearMojo} for target <code>clr</code></li>
 * <li>{@link DocxMojo} for target <code>docx</code></li>
 * <li>{@link DviMojo} for target <code>dvi</code></li>
 * <li>{@link GraphicsMojo} for target <code>grp</code></li>
 * <li>{@link HtmlMojo} for target <code>html</code></li>
 * <li>{@link OdtMojo} for target <code>odt</code></li>
 * <li>{@link PdfMojo} for target <code>pdf</code></li>
 * <li>{@link RtfMojo} for target <code>rtf</code></li>
 * <li>{@link TxtMojo} for target <code>txt</code></li>
 * </ul>
 * except mojo for the the <code>help</code> target which is created. 
 * For rough explanation of the targets use <code>mvn latex:help</code>
 * In addition, there is a common base class for the target classes described above 
 * {@link AbstractLatexMojo} and an implementation for a logger 
 * {@link MavenLogWrapper} extending {@link eu.simuline.m2latex.core.LogWrapper}.
 */
package eu.simuline.m2latex.mojo;
