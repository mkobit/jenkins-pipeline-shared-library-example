// TODO(M5b): restore plugin application via composite build once 0.11.0 is published.
// com.mkobit.jenkins.pipelines.shared-library 0.10.1 uses JavaPluginConvention which
// was removed in Gradle 9; the plugin cannot be applied until M1 migration is complete.

plugins {
  groovy
  id("com.diffplug.spotless") version "8.4.0"
  id("org.openrewrite.rewrite") version "6.26.0"
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

rewrite {
  activeRecipe("org.openrewrite.gradle.MigrateToGradle9")
}

dependencies {
  testImplementation(libs.spock.core)
  testImplementation(libs.assertj)
}

spotless {
  kotlinGradle {
    ktlint()
    target("*.gradle.kts")
  }
}
