plugins {
  alias(libs.plugins.shared.library)
  alias(libs.plugins.openrewrite)
  alias(libs.plugins.spotless)
  codenarc
  kotlin("jvm") version "2.0.21"
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

// Allow CI to inject a different Jenkins LTS line via -PjenkinsVersion, -PjenkinsBom,
// and -PjenkinsTestHarnessVersion for cross-version compatibility testing.
sharedLibrary {
  (findProperty("jenkinsVersion") as String?)?.let { jenkins.version.set(it) }
  (findProperty("jenkinsTestHarnessVersion") as String?)?.let { jenkins.testHarnessVersion.set(it) }
}

dependencies {
  val overrideBom = findProperty("jenkinsBom") as String?
  if (overrideBom != null) {
    jenkinsPlugin(platform(overrideBom))
  } else {
    jenkinsPlugin(platform(libs.jenkins.bom))
  }
  // pipeline-groovy-lib, workflow-job, workflow-basic-steps, and
  // workflow-durable-task-step are provided by the plugin by default.
  // workflow-cps, workflow-api, and their deps are transitives of pipeline-groovy-lib.
  jenkinsPlugin("org.jenkins-ci.plugins.workflow:workflow-multibranch")
  jenkinsPlugin("org.jenkinsci.plugins:pipeline-model-definition")
  jenkinsPlugin("org.6wind.jenkins:lockable-resources")

  testImplementation(libs.spock.core)
  testImplementation(libs.assertj)
  testImplementation(libs.kotest.runner)
  testImplementation(libs.kotest.assertions)
  integrationTestImplementation(libs.spock.core)
  integrationTestImplementation(libs.spock.junit4)
  integrationTestRuntimeOnly(libs.junit.vintage.engine)
}

codenarc {
  toolVersion = libs.versions.codenarc.get()
  configFile = file("config/codenarc/codenarc.xml")
  reportFormat = "text"
}

tasks.wrapper {
  gradleVersion = "9.4.1"
  distributionType = Wrapper.DistributionType.ALL
}

spotless {
  groovy {
    greclipse().configFile("config/greclipse.properties")
    target("src/**/*.groovy", "vars/**/*.groovy", "test/**/*.groovy")
  }
  kotlinGradle {
    ktlint()
    target("*.gradle.kts")
  }
}
