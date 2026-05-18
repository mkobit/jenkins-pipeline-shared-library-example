# Jenkins pipeline shared library example

[![Build](https://github.com/mkobit/jenkins-pipeline-shared-library-example/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/mkobit/jenkins-pipeline-shared-library-example/actions/workflows/build.yml)

An example [Jenkins Pipeline Shared Library](https://jenkins.io/doc/book/pipeline/shared-libraries/) built with the [Shared Library Gradle plugin](https://github.com/mkobit/jenkins-pipeline-shared-libraries-gradle-plugin).

Use your test framework of choice.
The plugin wires any `JvmTestSuite` with Jenkins test harness via `sharedLibrary.withJenkins(suite)`.
This repo shows four frameworks across Java, Groovy, and Kotlin.

## Project layout

| Path | Contents |
|---|---|
| `src/` | Groovy shared library classes |
| `vars/` | Pipeline step scripts |
| `resources/` | Files accessible via `libraryResource()` |
| `test/unit/` | [JenkinsPipelineUnit](https://github.com/lesfurets/JenkinsPipelineUnit) — fast, no Jenkins runtime |
| `test/integration/` | [JUnit 4](https://junit.org/junit4/) via `JenkinsRule` (built-in suite) |
| `test/integration-junit/` | [JUnit Jupiter](https://junit.org/junit5/) (Java) |
| `test/integration-spock/` | [Spock 2.x](https://spockframework.org/) (Groovy) |
| `test/integration-kotest/` | [Kotest](https://kotest.io/) (Kotlin) |

## Running tests

| Task | Runs |
|---|---|
| `./gradlew test` | Unit tests (fast) |
| `./gradlew integrationTest` | Built-in JUnit 4 suite |
| `./gradlew integrationTestJunit` | JUnit Jupiter |
| `./gradlew integrationTestSpock` | Spock 2.x |
| `./gradlew integrationTestKotest` | Kotest |
| `./gradlew check` | All suites |

## Framework wrappers

The Spock and Kotest suites ship thin wrappers around `JenkinsSessionFixture` that handle setup and teardown.

`JenkinsSupport` (Spock trait) — implement it and call `jenkins { }`:

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

`JenkinsFunSpec` (Kotest base class) — extend it and call `jenkins { }`:

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

1. Replace `src/com/mkobit/libraryexample/` with your package structure and update `vars/`.
2. Set `rootProject.name` in `settings.gradle.kts` to your Jenkins library name.
3. Swap the Jenkins BOM version in `gradle/libs.versions.toml` for your target LTS line.
4. Pin to a released plugin version on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.mkobit.jenkins.pipelines.shared-library).
5. Remove the `// TEMPLATE FORK` lines in `settings.gradle.kts` and drop `compatibility.yml`.
