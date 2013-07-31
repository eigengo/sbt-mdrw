/*
val settings = Settings(projectRef, buildStruct, state)

def isMarkdownFile(file: File): Boolean = {
  if (!file.isFile) {
    false
  } else {
    val ext = file.ext
    ext == "md" || ext == "mdown" || ext == "markdown"
  }
}

def sourceDirectoriesFor(config: Configuration) = {
  val managedSourceDirs = {
    state.log.info("Running " + config.name + ":" + Keys.managedSources.key.label + " ...")
    EvaluateTask(buildStruct, Keys.managedSources in config, state, projectRef)
    val managedSourceRoots = settings.settingWithDefault(Keys.managedSourceDirectories in config, Seq.empty[File])
    def listSubdirectories(f: File) = Option(f.listFiles()).map(_.toSeq.filter(_.isDirectory)).getOrElse(Seq.empty[File])
    managedSourceRoots.flatMap(listSubdirectories)
  }

  val baseDirs = {
    val baseDir = settings.setting(Keys.baseDirectory, "Missing base directory!")
    val baseDirDirectlyContainsSources = baseDir.listFiles().exists(isMarkdownFile)
    if (config.name == "compile" && baseDirDirectlyContainsSources) Seq[File](baseDir) else Seq[File]()
  }

  settings.settingWithDefault(Keys.unmanagedSourceDirectories in config, Nil) ++ managedSourceDirs ++ baseDirs
}

def resourceDirectoriesFor(config: Configuration) = {
  settings.settingWithDefault(Keys.unmanagedResourceDirectories in config, Nil)
}

def directoriesFor(config: Configuration) = {
  Directories(
    sourceDirectoriesFor(config),
    resourceDirectoriesFor(config),
    settings.optionalSetting(Keys.classDirectory in config))
}

val compileDirectories: Directories = directoriesFor(Configurations.Compile)
val docsDirectories: Directories = directoriesFor(Configurations.Docs)
val sourceDirectories: Directories = directoriesFor(Configurations.Sources)

println("*** sources: " + sourceDirectories.sources)
println("*** docs: " + docsDirectories.sources)
println("*** compile: " + compileDirectories.sources)
*/

