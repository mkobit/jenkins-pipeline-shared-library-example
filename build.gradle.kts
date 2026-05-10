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

dependencies {
  jenkinsPlugin("org.jenkins-ci.plugins.workflow:workflow-multibranch")
  jenkinsPlugin("org.jenkinsci.plugins:pipeline-model-definition")
  jenkinsPlugin("org.6wind.jenkins:lockable-resources")

  testImplementation(libs.spock.core)
  testImplementation(libs.assertj)
  testImplementation(libs.kotest.engine)
  testRuntimeOnly(libs.kotest.runner)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.decoroutinator)
}

val scriptsSourceSet =
  sourceSets.create("scripts") {
    groovy.setSrcDirs(listOf("scripts"))
    java.setSrcDirs(emptyList<String>())
    resources.setSrcDirs(emptyList<String>())
  }
tasks.named("compileScriptsGroovy") { enabled = false }

codenarc {
  toolVersion = libs.versions.codenarc.get()
  configFile = file("config/codenarc/codenarc-src.xml")
  reportFormat = "text"
  sourceSets = sourceSets + listOf(scriptsSourceSet)
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
      sources {
        kotlin.setSrcDirs(listOf("test/unit/kotlin"))
      }
    }
    named<JvmTestSuite>("integrationTest") {
      sources {
        kotlin.setSrcDirs(listOf("test/integration/kotlin"))
      }
    }
  }
}

val integrationTestJunit5 =
  testing.suites.register<JvmTestSuite>("integrationTestJunit5") {
    sharedLibrary.useJenkinsTestRunnerSuite(this)
    sources {
      java.setSrcDirs(listOf("test/integration-junit5/java"))
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
val integrationTestSpock =
  testing.suites.register<JvmTestSuite>("integrationTestSpock") {
    sharedLibrary.useJenkinsTestRunnerSuite(this)
    sources {
      groovy.setSrcDirs(listOf("test/integration-spock/groovy"))
    }
    dependencies {
      implementation(libs.spock.core)
      compileOnly(libs.groovy.core)
    }
  }

val integrationTestKotest =
  testing.suites.register<JvmTestSuite>("integrationTestKotest") {
    sharedLibrary.useJenkinsTestRunnerSuite(this)
    useJUnitJupiter()
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

val smokeTest =
  testing.suites.register<JvmTestSuite>("smokeTest") {
    sharedLibrary.useJenkinsTestRunnerSuite(this)
    sources {
      java.setSrcDirs(listOf("test/smoke/java"))
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

tasks.withType<Test>().configureEach {
  testLogging {
    events("failed", "skipped")
    showExceptions = true
    showCauses = true
    showStackTraces = true
    exceptionFormat = TestExceptionFormat.FULL
    stackTraceFilters = setOf(TestStackTraceFilter.TRUNCATE)
  }
}

tasks.named<GroovyCompile>("compileIntegrationTestSpockGroovy") {
  groovyClasspath = configurations.getByName("integrationTestSpockCompileClasspath")
}

tasks.check {
  dependsOn(integrationTestJunit5, integrationTestSpock, integrationTestKotest, smokeTest)
}

tasks.wrapper {
  gradleVersion = "9.5.0"
  distributionType = Wrapper.DistributionType.ALL
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
}
