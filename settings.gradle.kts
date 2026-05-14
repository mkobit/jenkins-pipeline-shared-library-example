pluginManagement {
  // TEMPLATE FORK: remove the next two lines once you pin to a released portal version
  val pluginProjectDir = file("../jenkins-pipeline-shared-libraries-gradle-plugin")
  if (pluginProjectDir.exists()) {
    includeBuild(pluginProjectDir)
  }
  repositories {
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.develocity") version "4.4.1"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { System.getenv("DEVELOCITY_PUBLISH") == "1" }
  }
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
