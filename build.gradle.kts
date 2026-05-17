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
    languageVersion = JavaLanguageVersion.of(17)
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
    plugin("org.jenkins-ci.plugins.workflow:workflow-multibranch")
    plugin("org.jenkinsci.plugins:pipeline-model-definition")
    plugin("org.6wind.jenkins:lockable-resources")
  }
}

tasks.named<CodeNarc>("codenarcScripts") {
  config = resources.text.fromFile("config/codenarc/codenarc-scripts.xml")
}

// TODO: https://github.com/gradle/gradle/issues/24972 — bare accessor syntax
//   (e.g. `test { }`, `integrationTest { }`) does not compile inside `testing.suites { }` even
//   though both suites are known before this script evaluates. The KTS accessor generator does not
//   produce accessors for ExtensiblePolymorphicDomainObjectContainer<TestSuite>. Until fixed,
//   `named<JvmTestSuite>()` is the only non-deprecated option.
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
    named<JvmTestSuite>("integrationTest") {
      useJUnitJupiter(libs.versions.junit.jupiter)
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

// sandbox=false: Spock 2.x (groovy:3.x) conflicts with groovy-all:2.4.x injected by the plugin
// for SandboxTransformer (compiled against Groovy 2.4 AST); avoids the transformer entirely.
// RealJenkinsFixture would isolate the runtimes but requires test-dependencies/index infrastructure
// that the Gradle plugin does not yet generate. Tracked in the backlog.
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
  }

tasks {
  withType<Test>().configureEach {
    systemProperty("kotest.framework.config.fqn", "testsupport.kotest.ProjectConfig")
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
    target("src/**/*.groovy", "vars/**/*.groovy", "test/**/*.groovy")
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
