plugins {
  alias(libs.plugins.shared.library)
  id("com.diffplug.spotless") version "8.4.0"
  id("org.openrewrite.rewrite") version "6.26.0"
}

rewrite {
  activeRecipe("org.openrewrite.gradle.MigrateToGradle9")
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

spotless {
  kotlinGradle {
    ktlint()
    target("*.gradle.kts")
  }
}
