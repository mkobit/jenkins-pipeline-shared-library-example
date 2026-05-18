# Jenkins pipeline shared library example

[![Build](https://github.com/mkobit/jenkins-pipeline-shared-library-example/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/mkobit/jenkins-pipeline-shared-library-example/actions/workflows/build.yml)

An example [Jenkins Pipeline Shared Library](https://jenkins.io/doc/book/pipeline/shared-libraries/) built with the [Shared Library Gradle plugin](https://github.com/mkobit/jenkins-pipeline-shared-libraries-gradle-plugin).

The plugin works with any JVM test framework.
This repo demonstrates four across three languages: JUnit 4 and JUnit Jupiter (Java), Spock 2.x (Groovy), and Kotest (Kotlin).

## Project layout

```
src/                    Groovy shared library classes
vars/                   pipeline step scripts
resources/              files accessible via libraryResource()
test/
  unit/                 JenkinsPipelineUnit (fast, no Jenkins runtime)
  integration/          built-in JenkinsRule suite (JUnit 4, Java)
  integration-junit/    JUnit Jupiter suite (Java)
  integration-spock/    Spock 2.x suite (Groovy)
  integration-kotest/   Kotest suite (Kotlin)
```

## Running tests

```shell
./gradlew test                   # unit tests (fast)
./gradlew integrationTest        # built-in JUnit 4 suite
./gradlew integrationTestJunit   # JUnit Jupiter suite
./gradlew integrationTestSpock   # Spock 2.x suite
./gradlew integrationTestKotest  # Kotest suite
./gradlew check                  # all suites
```

## Framework wrappers

The Spock and Kotest suites include thin wrappers that handle `JenkinsSessionFixture` setup and teardown.
Wire additional suites into Jenkins using `sharedLibrary.withJenkins(suite)` in `build.gradle.kts`.

**Spock** — implement `JenkinsSupport` and call `jenkins { }`:

```groovy
class MySpec extends Specification implements JenkinsSupport {
    def 'my step runs'() {
        jenkins {
            def job = createProject(WorkflowJob, 'test')
            job.definition = new CpsFlowDefinition('myStep()', false)
            buildAndAssertSuccess(job)
        }
    }
}
```

**Kotest** — extend `JenkinsFunSpec` and call `jenkins { }`:

```kotlin
class MySpec : JenkinsFunSpec({
    test("my step runs") {
        jenkins { rule ->
            val job = rule.createProject(WorkflowJob::class.java, "test")
            job.definition = CpsFlowDefinition("myStep()", true)
            rule.buildAndAssertSuccess(job)
        }
    }
})
```

## Using this as a template

1. Replace `src/com/mkobit/libraryexample/` with your package structure.
2. Update `vars/` and `rootProject.name` in `settings.gradle.kts`.
3. Swap the Jenkins BOM version in `gradle/libs.versions.toml` for your target LTS line.
4. Pin to a released plugin version on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.mkobit.jenkins.pipelines.shared-library).
5. Remove the composite build hook in `settings.gradle.kts` (marked `// TEMPLATE FORK`).
6. Drop `compatibility.yml` — it only applies when developing the plugin alongside the example.

## Scripts

`scripts/export-jenkins-catalog.groovy` runs in the Jenkins Script Console and exports installed plugins as a `jenkins.versions.toml` Gradle version catalog.
