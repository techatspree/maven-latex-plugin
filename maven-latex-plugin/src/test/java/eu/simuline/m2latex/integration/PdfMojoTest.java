package eu.simuline.m2latex.integration;

import eu.simuline.m2latex.mojo.PdfMojo;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class PdfMojoTest extends AbstractMojoTestCase {
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
	// required for mojo lookups to work
	super.setUp();
    }

    protected void tearDown() throws Exception {
        // required
        super.tearDown();
    }
     
    /**
     * Tests whether the manual is the same as the one stored for comparison. 
     * 
     * @throws Exception
     */
    public void testMojoGoal() throws Exception {
        // getBasedir() is inherited from org.codehaus.plexus.PlexusTestCase
        File thisDir = new File(getBasedir(), "src/test/resources/integration/");
        File testPom = new File(thisDir, "pom4pdf.xml");
        assertNotNull(testPom);
        assertTrue(testPom.exists());

        // cleanup the target folder 
        File target = new File(thisDir, "target/");
        FileUtils.deleteDirectory(target);
        boolean res = target.mkdir();
        assert res || !res;

        // define the file to be created and the one to be compared with 
        //File act = new File(thisDir, "target/manualLatexMavenPlugin.pdf");
        //File cmp = new File(thisDir,    "cmp/manualLatexMavenPlugin.pdf");
        File act = new File(thisDir, "target/dvi/dviFormat.pdf");
        File cmp = new File(thisDir,    "cmp/dvi/dviFormat.pdf");

        // run the pdf-goal in the pom 
	    PdfMojo testMojo = (PdfMojo)lookupMojo("pdf", testPom);
	    assertNotNull(testMojo);
        testMojo.execute();
        // Here, according to pom2pdf.xml, the generated pdf is expected at 
        // ${basedir}/src/test/resources/integration/target/manualLatexMavenPlugin.pdf
        assert act.exists() && cmp.exists();

        // check that the goal yielded the expected document. 
        // This si no longer needed as that test is done by the plugin itself 
        // and even in more generality: bitwise equality not required.
        //assertTrue(IOUtils.contentEquals(new FileInputStream(cmp), new FileInputStream(act)));

        // cleanup
        FileUtils.deleteDirectory(target);
    }
}
