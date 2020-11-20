package eu.simuline.m2latex.core;

//import org.apache.maven.project.io.xpp3.MavenXpp3Reader;
//import org.apache.maven.project.Model;

import java.io.InputStream;

import java.io.IOException;
import java.io.File;

import java.util.Properties;
import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.jar.Attributes;
import java.net.URISyntaxException;
import java.net.URL;

// TBD: extract this class into a separate git repository 
// and use it as a submodule. 
/**
 * Gives access to meta info on this piece of software stored in the jar-file 
 * which provides this class. 
 * Start with version information. 
 */
public class MetaInfo {
    

    /**
     * Name of the folder <code>META-INF</code> in the jar file which provides this class. 
     * This contains 
     * <ul>
     * <li>the folder <code>maven</code> created by maven and containing information 
     * like pom properties and pom itself which we currently do not consider</li>
     * <li>the manifest file named {@link #MANIFEST_FILE}.</li>
     * </ul>
     */
    private final static String META_FOLDER = "META-INF/";
    
    
    /**
     * Creates the stream for the file given by <code>fileName</code>. 
     * @param fileName
     *    a filename
     * @return
     *    an input stream to read from <code>fileName</code>. 
     * @throws BuildFailureException
     *    TMI01: if the stream to <code>fileName</code> could not be created. 
     */
    static InputStream getStream(String fileName) 
	    throws BuildFailureException {
	InputStream res = MetaInfo.class.getClassLoader().getResourceAsStream(fileName);
	if (res == null) {
	    throw new BuildFailureException("TMI01: Cannot get stream to file '" 
		    + fileName + "'. ");
	}
	return res;
    }
    
    /**
     * @param fileName
     *    an input stream to read from <code>fileName</code>. 
     * @return
     *    Properties read from <code>fileName</code>. 
     * @throws BuildFailureException
     *    <ul>
     *    <li>TMI01: if the stream to <code>fileName</code> could not be created. </li>
     *    <li>TMI02: if the properties could not be read from <code>fileName</code>. </li>
     *    </ul>
     */
    static Properties getProperties(String fileName) 
	    throws BuildFailureException {
	try {
	    Properties properties = new Properties();
	    // may throw TFU01
	    properties.load(MetaInfo.getStream(fileName));
	    return properties;
	} catch(IOException e) {
	    // TBD: assign exception identifier 
	    throw new BuildFailureException("TMI02: Cannot load properties from file '" 
		    + fileName + "'. ");
	}
    }
  
    

    /**
     * Reflects the pieces of information given by the manifest file 
     * with manifest version {@link #MANIFEST_VERSION} augmented 
     * by {@link #getCreatedBy()} and by {@link #getBuildJdkSpec()} 
     * which are both due to to the maven jar plugin and specific version 
     * given by {@link #MVN_JAR_PLUGIN}. 
     *  
     * TBD: change user and see whether author changes then also 
     * @author ernst
     */
    static class ManifestInfo {
	
	/**
	 * The manifest version this class is designed for. 
	 * When creating an instance, that version is checked 
	 * and if the version found differs from that one, an exception is thrown. 
	 */
	private final static String MANIFEST_VERSION = "1.0";
	
	/**
	 * An indicator that the jar file 
	 * containing the manifest represented by this object 
	 * was created by maven jar plugin with proper version. 
	 */
	private final static String MVN_JAR_PLUGIN = "Maven Jar Plugin 3.2.0";

	/**
	 * The name of the manifest file which is in the folder {@link #META_FOLDER} 
	 * of the jar file which provides this class. 
	 */
	private final static String MANIFEST_FILE = "MANIFEST.MF";

	/**
	 * These attributes are added by the maven jar plugin in version 3.2.0 and is specific for that. 
	 * Thus the according value is exactly the specification of that plugin with version info. 
	 */
	private final static Attributes.Name CREATED_BY     = new Attributes.Name("Created-By");
	
	/**
	 * The version of the jdk used to build the jar containing the manifest 
	 * represented by this object. 
	 * These attributes are added by the maven jar plugin version 3.2.0 and is specific for that. 
	 */
	private final static Attributes.Name BUILD_JDK_SPEC = new Attributes.Name("Build-Jdk-Spec");
	  
	// TBD: decide whether this is sensible 
	private final Manifest manifest;
	
	/**
	 * The main attributes of the manifest. 
	 */
	private final Attributes mAtts;

	/**
	 * Creates Manifest info instance 
	 * @throws BuildFailureException
	 *    TFU01: if the stream to the manifest file is not readable 
	 *    TFU03: the manifest file is not readable although the stream is ok. 
	 */
	ManifestInfo() throws BuildFailureException {
	    
	    //System.out.println("MANIFEST: ");
	    try {
		// getStream may throw TFU01
		this.manifest = new Manifest(getStream(META_FOLDER + MANIFEST_FILE));
	    } catch (IOException e) {
		throw new BuildFailureException
		("TMI03: IOException reading manifest. ");
	    }
	    this.mAtts = this.manifest.getMainAttributes();
	    // check the manifest version 
	    if (!MANIFEST_VERSION.equals(getManifestVersion())) {
		throw new IllegalStateException("Found manifest with version '" 
			+ getManifestVersion() + " where expected version "
			+ MANIFEST_VERSION + ". ");
	    }
	    // CAUTION: don't use getCreatedBy() as this throws an exception 
	    // if this jar is not created with the correct maven plugin 
	    // (or even without maven at all)
	    if (!MVN_JAR_PLUGIN.equals(this.mAtts.get(CREATED_BY))) {
		// MVN_JAR_PLUGIN includes the version also 
		throw new IllegalStateException("Found manifest not created by '" 
			+ MVN_JAR_PLUGIN + "'. ");
	    }	    

//		Map<String, Attributes> entriesMf = mf.getEntries();


		// seems to correspond with Attributes.Name
		// MANIFEST_VERSION,
		// IMPLEMENTATION_TITLE, IMPLEMENTATION_VERSION, IMPLEMENTATION_VENDOR,
		// SPECIFICATION_TITLE, SPECIFICATION_VERSION, SPECIFICATION_VENDOR,
		//
		// defined but not occurring:
		// SIGNATURE_VERSION, CONTENT_TYPE, CLASS_PATH, MAIN_CLASS, SEALED,
		// EXTENSION_LIST, EXTENSION_NAME, EXTENSION_INSTALLATION,
		// IMPLEMENTATION_VENDOR_ID
		// IMPLEMENTATION_URL,
		//
		// occurring but not defined in Attributes.Name:
		// Created-By: Maven Jar Plugin 3.2.0, Build-Jdk-Spec: 11

		//this.implVersion = this.mAtts.get(Attributes.Name.IMPLEMENTATION_VERSION).toString();

		// TBC: seems to be empty in this case.
		// in maven-jar-plugin set by manifestSections
		// System.out.println("Manifest entries"+entriesMf);

		// Enumeration entriesJar = mf.entries();
		// System.out.println("jar entries"+entriesJar);
	}

	private String getAttrValue(Object name) {
	    // is in fact a string always but this is to detect null pointer exceptions 
	    return this.mAtts.get(name).toString();
	}
	
	/**
	 * Returns the version of the implementation. 
	 * This is the version given by the maven coordinates.
	 * 
	 *  @return
	 */
	protected String getImplVersion() {
	    return getAttrValue(Attributes.Name.IMPLEMENTATION_VERSION);
	}
	
	/**
	 * Returns the vendor of the implementation. 
	 * This is the vendor given by the maven <code>project.organization.name</code>.
	 * 
	 *  @return
	 */
	protected String getImplVendor() {
	    return getAttrValue(Attributes.Name.IMPLEMENTATION_VENDOR);
	}

	protected String getManifestVersion() {
	    return getAttrValue(Attributes.Name.MANIFEST_VERSION);
	}
	
	protected String getSpecVersion() {
	    return getAttrValue(Attributes.Name.SPECIFICATION_VERSION);
	}
	
	// specific for Maven Jar Plugin 3.2.0 which is also the string returned. 
	protected String getCreatedBy() {
	    return getAttrValue(CREATED_BY);
	}
	
	// specific for Maven Jar Plugin 3.2.0 
	protected String getBuildJdkSpec() {
	    return getAttrValue(BUILD_JDK_SPEC);
	}

	
	protected String getPackageImplVersion() {
	    return this.getClass().getPackage().getImplementationVersion();
	}
	// TBD: tie names to values. 
	public String toString() {
	    StringBuilder stb = new StringBuilder();
	    for (String line : toStringArr()) {
		stb.append(line);
		stb.append('\n');
	    }
	    return stb.toString();
	}
	
	public String[] toStringArr() {
//	    List<String> lines = new ArrayList<String>();
//	    Object key, value;
//	    for (Map.Entry<Object,Object> entry : mAtts.entrySet()) {
//		key   = entry.getKey();
//		value = entry.getValue();
//		lines.add("   '" + key   + "' cls: " + key.getClass() +
//			"'->'" + value + "' cls: " + value.getClass());
//	    }
//	    return lines.toArray(new String[lines.size()]);
	    
	    return new String[] {
		    "MANIFEST: ("+ getManifestVersion() + ")",
		    "       Implementation-Version: '" + getImplVersion() + "'",
		    "PackageImplementation-Version: '" + getPackageImplVersion() + "'"
			    //"creator: '" + getCreatedBy() + "'",
		    //"version jdk: '" + getBuildJdkSpec() + "'"
	    };
	}
	
    } // class ManifestInfo


    /**
     * Reflects the pieces of information 
     * given by the <code>git-commit-id-plugin</code> in the current(?) version. 
     * 
     * 
     * @author ernst
     */
    class GitProperties {
	private final static String GIT_PROPS_FILE = "git.properties";
	
	private final static String GIT_BUILD_VERSION = "git.build.version";
	private final static String GIT_COMMIT_ID_DESCRIBE = "git.commit.id.describe";
	private final static String GIT_CLOSEST_TAG_NAME = "git.closest.tag.name";
	private final static String GIT_CLOSEST_TAG_COMMIT_COUNT = "git.closest.tag.commit.count";
	private final static String GIT_COMMIT_ID_ABBREV = "git.commit.id.abbrev";
	private final static String GIT_DIRTY = "git.dirty";
	private final static String GIT_BUILD_TIME = "git.build.time";

	
	private final Properties properties;
	
	/**
	 * @throws BuildFailureException
	 *    <ul>
	 *    <li>TMI01: if the stream to {@link #GIT_PROPS_FILE} could not be created. </li>
	 *    <li>TMI02: if the properties could not be read from @link #GIT_PROPS_FILE}. </li>
	 *    </ul>
	 */
	GitProperties() throws BuildFailureException {
	    // TBD: extract properties 
		this.properties = getProperties(GIT_PROPS_FILE);
		
		//String gitBuildVersion = getBuildVersion();
		// latex-maven-plugin-1.4-44-g555bde8-dirty
		// is constructed from 
		//String gitCommitIdDescribe = getCommitIdDescribe();
		// also interesting: 
	}
	
	private String getAttrValue(String key) {
	    // is in fact a string always but this is to detect null pointer exceptions 
	    return this.properties.get(key).toString();
	}

	/**
	 * Returns the build version which is the same as the version in the coordinate 
	 * provided the <code>maven-release-plugin</code> is used correctly. 
	 * @return
	 *    the build version. 
	 */
	String getBuildVersion() {
	    return getAttrValue(GIT_BUILD_VERSION);
	}
	
	// latex-maven-plugin-1.4-44-g555bde8-dirty
	/**
	 * Returns the commit identifiers description TBC which consists of 
	 * <ul>
	 * <li> the closest tag name as given by {@link #getClosestTagName()} </li>
	 * <li> the number of commits since the last tag 
	 *      given by {@link #getClosestTagCommitCount()}</li>
	 * <li> something unknown which leads to the 'g'</li>
	 * <li> git.commit.id.abbrev=555bde8</li>
	 * <li> 'dirty' if {@link #getDirty()} returns true. </li>
	 * </ul>
	 * each segment separated by a dash
	 * 
	 * @return
	 *    the commit identifiers description
	 */
	String getCommitIdDescribe() {
	    return getAttrValue(GIT_COMMIT_ID_DESCRIBE);
	}
	
	// latex-maven-plugin-1.4
	/**
	 * This is the artifact id and, separated by a dash, 
	 * by the current version tag, if there is one, 
	 * else, i.e. for snapshots, by the next lowest. 
	 * The tag given is current, iff {@link #getClosestTagCommitCount()} returns 0.  
	 *
	 * @return
	 *    the closest tag name for the last commit. 
	 */
	String getClosestTagName() {
	    return getAttrValue(GIT_CLOSEST_TAG_NAME);
	}
	
	// TBD: shall be a number, not a string 
	/**
	 * Returns the number of commits since the last tag 
	 * which is given by {@link #getClosestTagName()}. 
	 * 
	 * @return
	 *    the number of commits since the last tag. 
	 */
	String getClosestTagCommitCount() {
	    return getAttrValue(GIT_CLOSEST_TAG_COMMIT_COUNT);
	}
	
	// TBC: maybe a string maybe an int or what. 
	/**
	 * Returns the abbreviated hash of the last commit.
	 * 
	 * @return
	 *    the abbreviated hash of the last commit.
	 */
	String getCommitIdAbbrev() {
	    return getAttrValue(GIT_COMMIT_ID_ABBREV);
	}

	// TBD: this shall not be a string but a boolean 
	/**
	 * Returns whether the current working environment is dirty, 
	 * i.e. there is something not committed inside. 
	 * 
	 * @return
	 *    whether the current working environment is dirty.
	 */
	String getDirty() {
	    return getAttrValue(GIT_DIRTY);
	}

	/**
	 * Returns the build time TBD: not just a string 
	 * @return
	 * the build time as a string. 
	 */
	String getBuildTime() {
	    return getAttrValue(GIT_BUILD_TIME);
	}
	
	public String toString() {
	    StringBuilder stb = new StringBuilder();
	    stb.append("build version:  '" + getBuildVersion()     + "'\n");
	    stb.append("commit id desc: '" + getCommitIdDescribe() + "'\n");
	    stb.append("buildTime:      '" + getBuildTime()        + "'\n");
	    return stb.toString();
	}
	
	void log() {
	    MetaInfo.this.log.info("build version:  '" + getBuildVersion()     + "'");
	    MetaInfo.this.log.info("commit id desc: '" + getCommitIdDescribe() + "'");
	    MetaInfo.this.log.info("buildTime:      '" + getBuildTime()        + "'");
	}

    } // class GitProperties

    static class Version {

	Matcher matcher;
	String versionStr;
	List<String> segments;

	Version(String env, String pattern, String text) {
	    this.matcher = Pattern.compile(String.format(env, pattern))
		    .matcher(text);
	    if (!this.matcher.find()) {
		this.versionStr = null;
		this.segments = null;
		return;
	    }
	    this.versionStr = this.matcher.group(1);
	    this.segments = new ArrayList<String>(this.matcher.groupCount());
	    for (int idx = 2; idx <= this.matcher.groupCount(); idx++) {
		this.segments.add(this.matcher.group(idx));
	    }
	}

	boolean isMatching() {
	    return this.versionStr != null;
	}

	String getString() {
	    return this.versionStr;
	}

	List<String> getSegments() {
	    return this.segments;
	}

    } // class Version 
    
    /**
     * Executor to find the version string.  
     */
    private final CommandExecutor executor;
    
    
    /**
     * Logs information on versions. 
     * Typically, just info are logged, 
     * but if a version could not be read or if a version is not as expected, 
     * also a warning is logged. 
     */
    private final LogWrapper log;

    MetaInfo(CommandExecutor executor, 
		   LogWrapper log) {
	this.executor = executor;
	this.log = log;
    }
    
    private final static String VERSION_PROPS_FILE = "version.properties";
    private final static String TOOL_VERSION_FORMAT = "%-15s '%s'/'%s'";

    // CAUTION, depends on the maven-jar-plugin and its version 
    /**
     * Prints meta information, mainly version information 
     * on this software and on the converters used. 
     * <p>
     * WMI01: If the version string of a converter cannot be read. 
     * WMI02: If the version of a converter is not as expected. 
     * @return
     *    whether a warning has been issued. 
     * @throws BuildFailureException
     *    <ul>
     *    <li>TMI01: if the stream to either the manifest file 
     *        or to a property file, either {@LINK #VERSION_PROPS_FILE} 
     *        or {@link MetaInfo.GitProperties#GIT_PROPS_FILE} could not be created. </li>
     *    <li>TMI02: if the properties could not be read 
     *        from one of the two property files mentioned above. </li>
     *    </ul>
     */
    public boolean printMetaInfo() throws BuildFailureException {
	ManifestInfo manifestInfo = new ManifestInfo();
	// TBC: how does maven determine that version?? 
	//String versionMf = manifestInfo.getImplVersion();
	//System.out.println("mf version: '" + versionMf + "'");
	this.log.info("Manifest properties: ");
	for (String line : manifestInfo.toStringArr()) {
	    this.log.info(line);
	}

	String mavenDir = META_FOLDER + "maven/";
	URL url = MetaInfo.class.getClassLoader().getResource(mavenDir);
//	try {
//	    //System.out.println("path: "+url);
//	    //System.out.println("path: "+url.toURI());
//	    //File[] files = new File(url.toURI()).listFiles();
//	    //System.out.println("cd maven; ls: "+java.util.Arrays.asList(files));
//	} catch (URISyntaxException e) {
//	    // TODO Auto-generated catch block
//	    throw new IllegalStateException("Found unexpected type of url: "+url);
//	}
	String propertyFileName = META_FOLDER
	    + "maven/"
	    + "eu.simuline.m2latex/"
	    + "latex-maven-plugin/"
	    + "pom.properties";
	this.log.info("pom properties:");
	Properties properties = getProperties(propertyFileName);
	assert "[groupId, artifactId, version]"
	    .equals(properties.stringPropertyNames().toString())
	    : "Found unexpected properties ";
	String coordGroupId    = properties.getProperty("groupId");
	String coordArtifactId = properties.getProperty("artifactId");
	String coordVersion    = properties.getProperty("version");
	
	this.log.info("coordinate.groupId:    '" + coordGroupId    + "'");
	this.log.info("coordinate.artifactId: '" + coordArtifactId + "'");
	this.log.info("coordinate.version:    '" + coordVersion    + "'");

//	propertyFileName = "META-INF/"
//	    + "maven/"
//	    + "eu.simuline.m2latex/"
//	    + "latex-maven-plugin/"
//	    + "pom.xml";
//	url = this.getClass().getClassLoader()
//	    .getResource(propertyFileName);
//	System.out.println("url:"+url);
//	MavenXpp3Reader reader = new MavenXpp3Reader();
//	Model model = reader.read(new InputStreamReader(url.openStream()));
	

	GitProperties gitProperties = new GitProperties();
	this.log.info("git properties: ");
	gitProperties.log();
	String gitBuildVersion = gitProperties.getBuildVersion();
	//System.out.println("git.build.version: " + gitBuildVersion);
	assert gitBuildVersion.equals(manifestInfo.getImplVersion());

//	String gitCommitIdDescribe = gitProperties.getCommitIdDescribe();
//	System.out.println("git.commit.id.describe: " + gitCommitIdDescribe);
//	String gitBuildTime = gitProperties.getBuildTime();
//	System.out.println("git.build.time: " + gitBuildTime);

// TBD: rework; this is just the beginning 
//	System.out.println("version properties of converters: ");
//	Properties properties = getProperties("version.properties");
//
//	System.out.println("version.makeindex:"
//		   +properties.getProperty("version.makeindex"));

	// headlines 
	this.log.info("tool versions: ");
	this.log.info(String.format(TOOL_VERSION_FORMAT, 
		"command:", "actual version", "expected version"));

	Properties versionProperties = getProperties(VERSION_PROPS_FILE);
	if (versionProperties.size() > Converter.values().length) {
	    // Relation < need not be checked since all converters are checked below 
	    throw new IllegalStateException("Number of version properites " + 
	            versionProperties.size() +
		    " does not fit number of converters " + 
	            Converter.values().length + ". ");
	}

	//System.out.println("version props"+versionProperties);
	String cmd, line, actVersion, expVersion, logMsg;
	Version actVersionObj, expVersionObj;
	boolean doWarn, doWarnAny = false;
	for (Converter conv : Converter.values()) {
	    doWarn = false;
	    cmd = conv.getCommand();
	    line = this.executor.execute(null, null, 
		    cmd, new String[] {
		    conv.getVersionOption()});

	    actVersionObj = new Version(conv.getVersionEnvironment(), 
		    conv.getVersionPattern(), line);
	    if (actVersionObj.isMatching()) {
		actVersion = actVersionObj.getString();
	    } else {
		doWarnAny = doWarn = true;
		this.log.warn("WMI01: Version string '" + line + 
			"' from converter " + conv + " did not match expected form. ");
		actVersion = "????";
	    }

	    expVersion = versionProperties.getProperty(cmd);
	    if (expVersion == null) {
		throw new IllegalStateException
		("Found no expected version for converter " + conv + ". ");
	    }


	    expVersionObj = new Version("^%s$", conv.getVersionPattern(), expVersion);
	    
	    if (!expVersionObj.isMatching()) {
		throw new IllegalStateException
		(String.format("Expected version '%s' does not match expression '%s'. ",
			expVersion, conv.getVersionPattern()));
	    }

	    if (!expVersion.equals(expVersionObj.getString())) {
		throw new IllegalStateException
		(String.format("Expected version '%s' reconstructed as '%s'. ",
			expVersion, expVersionObj.getString()));
	    }

	    if (!expVersion.equals(actVersion)) {
		doWarnAny = doWarn = true;
	    }

	    logMsg = String.format(TOOL_VERSION_FORMAT, cmd+":", actVersion, expVersion);
	    //this.log.info("actVersion: "+actVersionObj.getSegments());
	    //this.log.info("expVersion: "+expVersionObj.getSegments());
	    if (doWarn) {
		this.log.warn("WMI02: Conflict " + logMsg);
	    } else {
		this.log.info(logMsg);
	    }
	}
	// TBD: extend also so that it works for version of GS
	return doWarnAny;
    }
}
