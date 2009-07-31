import sbt._
abstract class AppengineProject(info: ProjectInfo) extends DefaultWebProject(info) {
  lazy val appengineSdkRoot = property[String]

  val Appengine = Configurations.config("appengine")
  override def ivyConfigurations: Iterable[Configuration] = super.ivyConfigurations  ++ (Appengine :: Nil)


  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided->default"

  val appEngineApi = "com.google.appengine" % "appengine-api-1.0-sdk" % "1.2.2" % "compile->default"
  val appEngineOrm = "com.google.appengine.orm" % "datanucleus-appengine" % "1.0.2" % "compile->default"

  val dataNucleusCore = "org.datanucleus" % "datanucleus-core" % "1.1.4" % "compile->default"
  val dataNucleusJPA = "org.datanucleus" % "datanucleus-jpa" % "1.1.4" % "compile->default"
  val javaxPersistence = "javax.persistence" % "persistence-api" % "1.0.2" % "compile->default"
  val javaxJDO2 = "javax.jdo" % "jdo2-api" % "2.3-ea" % "compile->default"
  val dataNucleusEnhancer = "org.datanucleus" % "datanucleus-enhancer" % "1.1.4" % "appengine->default"

  override def ivyXML =
    <dependencies>
      <dependency org="com.google.appengine.orm" name="datanucleus-appengine" rev="1.0.1">
        <exclude org="org.datanucleus" module="datanucleus-core"/>
        <exclude org="org.datanucleus" module="datanucleus-jpa"/>
      </dependency>
      <exclude org="junit" module="junit" conf="compile, runtime"/>
    </dependencies>

  val smackRepo = "m2-repository-smack" at "http://maven.reucon.com/public"
  val mvnsearchRepo = "mvnsearch.org" at "http://www.mvnsearch.org/maven2"
  val scalaSnapshots = "scala-snapshots" at "http://scala-tools.org/repo-snapshots"
  val devjavanet = JavaNet1Repository

  //
  lazy val appengineToolsApiJar = Path.fromFile(appengineSdkRoot.value) / "lib" / "appengine-tools-api.jar"
  def appengineClasspath = fullClasspath(Appengine) +++ compileClasspath +++ appengineToolsApiJar

  lazy val enhance = datanucleusEnhancerAction()
  lazy val enhanceCheck = datanucleusEnhancerAction("-checkonly")
  val classes = (mainCompilePath ** "*.class")
  def datanucleusEnhancerAction(opts:String*) =
    runTask(Some("org.datanucleus.enhancer.DataNucleusEnhancer"),
            appengineClasspath, List("-v") ++ opts ++ 
            classes.get.map(_.projectRelativePath)) dependsOn(compile)

  override def prepareWebappAction = super.prepareWebappAction dependsOn(enhance)

  lazy val appcfg = task { args =>
    runTask(Some("com.google.appengine.tools.admin.AppCfg"),
            appengineToolsApiJar, args)
  }

  lazy val appcfgUpdate = task { args =>
    runTask(Some("com.google.appengine.tools.admin.AppCfg"),
            appengineToolsApiJar, args ++ List("update", temporaryWarPath.projectRelativePath)) dependsOn(prepareWebapp)
  }

}
