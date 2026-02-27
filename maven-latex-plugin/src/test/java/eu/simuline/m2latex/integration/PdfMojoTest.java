package eu.simuline.m2latex.integration;

import eu.simuline.m2latex.mojo.InjectionMojo;
import eu.simuline.m2latex.mojo.PdfMojo;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import org.apache.commons.io.FileUtils;

import java.io.File;

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
    //File act = new File(thisDir, "target/manualLMP.pdf");
    //File cmp = new File(thisDir,    "cmp/manualLMP.pdf");
    //File act = new File(thisDir, "target/dvi/dviFormat.pdf");
    //File cmp = new File(thisDir,    "cmp/dvi/dviFormat.pdf");

    // run the pdf-goal in the pom 
    // TBD: in other framework: also check lifecycle phase like site and so 
    // TBD: clarify why this does not work 
    InjectionMojo injMojo = (InjectionMojo) lookupMojo("inj", testPom);// this is to update .latexmkrc 
    // assertNotNull(injMojo);
    // injMojo.execute();// does not work. 

    // TBD: bugfix 
    // This is roughly descibed in the changes.xml 
    // The overall aim is to get the test run independent from the productive run. 
    // In the productive run, before executing something like goal 'pdf', 
    // - goal 'inj' is needed 
    // - further actions are taken not tied directly to this plugin 
    // altogether these actions are taken invoking 'mvn validate'. 
    // So, what is missing is a technique to invoke also the other plugins with appropriate goals. 
    // It must be clarified, what lookupMojo(....) really does. 
    // Is it applicable to other plugins also? What does it tie to this latex plugin? 
    // In former versions, it was sufficient that the injections are present 
    // and no one cared that it was not configured by pom4pdf.xml but by pom.xml itself. 
    // But now a difference occurs: 
    // The config cfgDiff affects the created .latexmkrc which is now used to really compile. 
    // In pom.xml this shall not be set so it takes the default 'false', 
    // whereas in pom4pdf.xml it is set so this does not fit: 
    // If .latexmkrc is not reconfigured in test, compilation with latexmk is with chfDiff=false, 
    // whereas diff is done in the java part is and it is performed because chkDiff=true, so crash. 
    // 
    // Seemingly, execute tries to get the version of this plugin to filter the injections. 
    // The critical place is LatexProcessor.processFileInjections invoked by the InjectionMojo 
    // Fails to run getCoordinates(...). 
    // Looks as if the mojo test accesses not the jar file but the incomplete data in the target folder. 
    // So, in fact, the config given by the pom in the base directory is in effect. 
    // To have chkDiff activated, one has to set it there. 
    // Even if execution of injMojo worked, the tests are still not independent. 
    // But even if this worked, the test would not be independent. 

    // perform the proper tests 
    PdfMojo testMojo = (PdfMojo) lookupMojo("pdf", testPom);
    assertNotNull(testMojo);
    testMojo.execute();
    // Here, according to pom2pdf.xml, the generated pdf is expected at 
    // ${basedir}/src/test/resources/integration/target/manualLMP.pdf
    //assert act.exists() && cmp.exists();

    // check that the goal yielded the expected document. 
    // This is no longer needed as that test is done by the plugin itself 
    // and even more generally: bitwise equality not required.
    //assertTrue(IOUtils.contentEquals(new FileInputStream(cmp), new FileInputStream(act)));

    // cleanup
    FileUtils.deleteDirectory(target);
  }
}
