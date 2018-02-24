import com.gradle.scan.plugin.BuildScanPlugin
import org.gradle.kotlin.dsl.version
import java.io.ByteArrayOutputStream

plugins {
  id("com.gradle.build-scan") version "1.11"
  id("com.mkobit.jenkins.pipelines.shared-library") version "0.4.0"
  id("com.github.ben-manes.versions") version "0.17.0"
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
    gradleVersion = "4.4.1"
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  testImplementation("org.spockframework:spock-core:1.1-groovy-2.4")
  testImplementation("org.assertj:assertj-core:3.9.1")
}

sharedLibrary {
  coreVersion = "2.95"
  groovyVersion = "2.4.12"
  pluginDependencies(Action {
    dependency("org.jenkinsci.plugins", "pipeline-model-api", "1.2.5")
    dependency("org.jenkinsci.plugins", "pipeline-model-declarative-agent", "1.1.1")
    dependency("org.jenkinsci.plugins", "pipeline-model-definition", "1.2.5")
    dependency("org.jenkinsci.plugins", "pipeline-model-extensions", "1.2.5")
  })
}
