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

// JPU 1.29 transitively brings groovy-all:2.4 which conflicts with Spock's Groovy 3 AST transform.
configurations.named("testImplementation") {
  exclude(group = "org.codehaus.groovy", module = "groovy-all")
}

codenarc {
  toolVersion = libs.versions.codenarc.get()
  configFile = file("config/codenarc/codenarc.xml")
}

tasks.wrapper {
  gradleVersion = "9.4.1"
  distributionType = Wrapper.DistributionType.ALL
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
