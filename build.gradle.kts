import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.plugins.quality.CodeNarc
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestStackTraceFilter

plugins {
  alias(libs.plugins.shared.library)
  alias(libs.plugins.openrewrite)
  alias(libs.plugins.spotless)
  codenarc
  alias(libs.plugins.kotlin.jvm)
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

val scriptsSourceSet =
  sourceSets.create("scripts") {
    groovy.setSrcDirs(listOf("scripts"))
    java.setSrcDirs(emptyList<String>())
    resources.setSrcDirs(emptyList<String>())
  }
tasks.named("compileScriptsGroovy") { enabled = false }

val mainCompileOnly = configurations.named(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
val mainImplementation = configurations.named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
configurations.named(scriptsSourceSet.compileOnlyConfigurationName) {
  extendsFrom(mainCompileOnly, mainImplementation)
}

codenarc {
  toolVersion = libs.versions.codenarc.get()
  configFile = file("config/codenarc/codenarc-src.xml")
  reportFormat = "text"
  sourceSets = sourceSets + listOf(scriptsSourceSet)
}

sharedLibrary {
  plugins {
    plugins(jenkinsPlugins.bundles.allPlugins)
    plugin("org.6wind.jenkins:lockable-resources:1305.v1a_3035fa_9065")
  }
}

tasks.named<CodeNarc>("codenarcScripts") {
  config = resources.text.fromFile("config/codenarc/codenarc-scripts.xml")
}

val kotestParallelism =
  providers
    .gradleProperty("kotest.parallelism")
    .map { it.toInt() }
    .orElse(Runtime.getRuntime().availableProcessors())

testing {
  suites {
    named<JvmTestSuite>("test") {
      useJUnitJupiter(libs.versions.junit.jupiter)
      sources {
        java.srcDirs("test/unit/java")
        groovy.srcDirs("test/unit/groovy")
        kotlin.srcDirs("test/unit/kotlin")
      }
      dependencies {
        implementation(libs.spock.core)
        implementation(libs.assertj)
        implementation(libs.kotest.engine)
        runtimeOnly(libs.kotest.runner)
        implementation(libs.kotest.assertions)
        implementation(libs.kotest.decoroutinator)
        implementation(libs.jenkins.pipeline.unit)
      }
    }
  }
}

val integrationTestJunit =
  testing.suites.register<JvmTestSuite>("integrationTestJunit") {
    sharedLibrary.withJenkins(this)
    sources {
      java.setSrcDirs(listOf("test/integration-junit/java"))
    }
    dependencies {
      implementation(libs.junit.jupiter.api)
      runtimeOnly(libs.junit.jupiter.engine)
      runtimeOnly(libs.junit.platform.launcher)
    }
    targets.all {
      testTask.configure {
        useJUnitPlatform()
      }
    }
  }

val integrationTestSpock =
  testing.suites.register<JvmTestSuite>("integrationTestSpock") {
    sharedLibrary.withJenkins(this)
    sources {
      groovy.setSrcDirs(listOf("test/integration-spock/groovy"))
    }
    dependencies {
      implementation(libs.spock.core)
    }
  }

val integrationTestKotest =
  testing.suites.register<JvmTestSuite>("integrationTestKotest") {
    sharedLibrary.withJenkins(this)
    useJUnitJupiter(libs.versions.junit.jupiter)
    sources {
      kotlin.setSrcDirs(listOf("test/integration-kotest/kotlin"))
    }
    dependencies {
      implementation(libs.kotest.engine)
      runtimeOnly(libs.kotest.runner)
      implementation(libs.kotest.assertions)
      implementation(libs.kotest.decoroutinator)
      implementation(libs.coroutines.core)
    }
    targets.all {
      testTask.configure {
        systemProperty("kotest.framework.parallelism", kotestParallelism)
      }
    }
  }

tasks {
  withType<Test>().configureEach {
    systemProperty("kotest.framework.config.fqn", "testsupport.kotest.ProjectConfig")
    maxParallelForks = 1
    testLogging {
      events("failed", "skipped")
      showExceptions = true
      showCauses = true
      showStackTraces = true
      exceptionFormat = TestExceptionFormat.FULL
      stackTraceFilters = setOf(TestStackTraceFilter.TRUNCATE)
    }
  }

  named<GroovyCompile>("compileIntegrationTestSpockGroovy") {
    groovyClasspath = configurations.getByName("integrationTestSpockCompileClasspath")
  }

  check {
    dependsOn(integrationTestJunit, integrationTestSpock, integrationTestKotest)
  }

  wrapper {
    gradleVersion = "9.4.1"
    distributionType = Wrapper.DistributionType.ALL
  }
}

spotless {
  groovy {
    greclipse().configFile("config/greclipse.properties")
    target("src/**/*.groovy", "vars/**/*.groovy", "test/**/*.groovy", "scripts/**/*.groovy")
  }
  java {
    googleJavaFormat()
    target("test/**/*.java")
  }
  kotlin {
    ktlint()
    target("test/**/*.kt")
  }
  kotlinGradle {
    ktlint()
    target("*.gradle.kts")
  }
  toml {
    versionCatalog()
    target("gradle/libs.versions.toml")
  }
}
