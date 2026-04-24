plugins {
  alias(libs.plugins.shared.library)
  alias(libs.plugins.openrewrite)
  alias(libs.plugins.spotless)
  codenarc
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

dependencies {
  jenkinsPlugin(platform(libs.jenkins.bom))
  jenkinsPlugin("org.jenkins-ci.plugins.workflow:workflow-api")
  jenkinsPlugin("org.jenkins-ci.plugins.workflow:workflow-basic-steps")
  jenkinsPlugin("org.jenkins-ci.plugins.workflow:workflow-cps")
  jenkinsPlugin("org.jenkins-ci.plugins.workflow:workflow-job")
  jenkinsPlugin("org.jenkins-ci.plugins.workflow:workflow-multibranch")

  testImplementation(libs.spock.core)
  testImplementation(libs.assertj)
}

codenarc {
  toolVersion = "3.5.0"
  configFile = file("config/codenarc/codenarc.xml")
}

spotless {
  groovy {
    greclipse()
    target("src/**/*.groovy", "vars/**/*.groovy", "test/**/*.groovy")
  }
  kotlinGradle {
    ktlint()
    target("*.gradle.kts")
  }
}
