pluginManagement {
  // Substitutes the published plugin with the local source build for composite-build development.
  // Comment out when testing against a published release.
  includeBuild("../jenkins-pipeline-shared-libraries-gradle-plugin")
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
