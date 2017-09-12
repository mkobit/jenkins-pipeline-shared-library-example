import org.gradle.kotlin.dsl.version
import java.io.ByteArrayOutputStream

plugins {
  id("com.gradle.build-scan") version "1.9"
  // Fails on apply of plugin right now, need to investigate
  id("com.mkobit.jenkins.pipelines.shared-library") version "0.1.0" apply false
}

val commitSha: String by lazy {
  ByteArrayOutputStream().use {
    project.exec {
      commandLine("git", "rev-parse", "HEAD")
      standardOutput = it
    }
    it.toString(Charsets.UTF_8.name()).trim()
  }
}

buildScan {
  setLicenseAgree("yes")
  setLicenseAgreementUrl("https://gradle.com/terms-of-service")
  link("GitHub", "https://github.com/mkobit/jenkins-pipeline-shared-library-example")
  value("Revision", commitSha)
}

tasks {
  "wrapper"(Wrapper::class) {
    gradleVersion = "4.1"
  }
}

//sharedLibrary {
//  groovyVersion = "2.4.12"
//  coreVersion = "2.73"
//  testHarnessVersion = "2.24"
//  pluginDependencies {
//    workflowCpsGlobalLibraryPluginVersion = "2.8"
//    blueocean("blueocean-web", "1.2.0")
//  }
//}
