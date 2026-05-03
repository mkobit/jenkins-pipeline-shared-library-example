pluginManagement {
  // When used standalone or via --include-build, the sibling directory provides the plugin.
  // When included in the root composite, this is deduplicated with the root's includeBuild.
  if (file("../jenkins-pipeline-shared-libraries-gradle-plugin").isDirectory) {
    includeBuild("../jenkins-pipeline-shared-libraries-gradle-plugin")
  }
  repositories {
    gradlePluginPortal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    mavenCentral()
    maven {
      name = "jenkins"
      url = uri("https://repo.jenkins-ci.org/public/")
    }
  }
}

rootProject.name = "jenkins-pipeline-shared-library-example"
