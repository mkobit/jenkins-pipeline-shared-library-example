plugins {
  id("com.mkobit.jenkins.pipelines.shared-library")
}

codenarc {
  configFile = rootProject.file("config/codenarc/codenarc-src.xml")
}
