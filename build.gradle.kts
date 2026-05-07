import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.plugins.quality.CodeNarc
import org.gradle.api.tasks.compile.GroovyCompile

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
}

codenarc {
  toolVersion = libs.versions.codenarc.get()
  configFile = file("config/codenarc/codenarc-src.xml")
  reportFormat = "text"
}

// Configure built-in suites — kotlin("jvm") doesn't know about our non-standard source layout.
testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      sources {
        kotlin.setSrcDirs(listOf("test/unit/kotlin"))
      }
    }
    val integrationTest by getting(JvmTestSuite::class) {
      sources {
        kotlin.setSrcDirs(listOf("test/integration/kotlin"))
      }
      dependencies {
        runtimeOnly(libs.junit.vintage.engine)
      }
    }
  }
}

val integrationTestJunit5 by testing.suites.registering(JvmTestSuite::class) {
  sharedLibrary.jenkinsTestRunnerSuite(this)
  sources {
    java.setSrcDirs(listOf("test/integration-junit5/java"))
  }
  dependencies {
    // LocalLibraryRetriever is generated and compiled by the integrationTest source set.
    implementation(
      sourceSets.integrationTest
        .get()
        .output.classesDirs,
    )
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
val integrationTestSpock by testing.suites.registering(JvmTestSuite::class) {
  sharedLibrary.jenkinsTestRunnerSuite(this)
  sources {
    groovy.setSrcDirs(listOf("test/integration-spock/groovy"))
  }
  dependencies {
    implementation(
      sourceSets.integrationTest
        .get()
        .output.classesDirs,
    )
    implementation(libs.spock.core)
    compileOnly(libs.groovy.core)
  }
}

val integrationTestKotest by testing.suites.registering(JvmTestSuite::class) {
  sharedLibrary.jenkinsTestRunnerSuite(this)
  useJUnitJupiter()
  sources {
    kotlin.setSrcDirs(listOf("test/integration-kotest/kotlin"))
  }
  dependencies {
    implementation(
      sourceSets.integrationTest
        .get()
        .output.classesDirs,
    )
    implementation(libs.kotest.runner)
    implementation(libs.kotest.assertions)
    implementation(libs.coroutines.core)
  }
}

val smokeTest by testing.suites.registering(JvmTestSuite::class) {
  sharedLibrary.jenkinsTestRunnerSuite(this)
  sources {
    java.setSrcDirs(listOf("test/smoke/java"))
  }
  dependencies {
    // SharedLibraryAutoRegistrar and its annotation-indexer index live in integrationTest classesDirs.
    implementation(
      sourceSets.integrationTest
        .get()
        .output.classesDirs,
    )
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

// Gradle's Groovy plugin does not auto-populate groovyClasspath for dynamically registered suites.
tasks.named<GroovyCompile>("compileIntegrationTestSpockGroovy") {
  groovyClasspath = configurations.getByName("integrationTestSpockCompileClasspath")
}

// scripts/ is not a Gradle source set; check it separately with NoDef enforced.
val codenarcScripts =
  tasks.register<CodeNarc>("codenarcScripts") {
    source = fileTree("scripts") { include("**/*.groovy") }
    configFile = file("config/codenarc/codenarc-scripts.xml")
    codenarcClasspath = configurations.codenarc.get()
    reports {
      text.required.set(true)
    }
  }

tasks.check {
  dependsOn(integrationTestJunit5, integrationTestSpock, integrationTestKotest, smokeTest, codenarcScripts)
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
