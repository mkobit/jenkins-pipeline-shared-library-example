# Jenkins pipeline shared library example

[![Build](https://github.com/mkobit/jenkins-pipeline-shared-library-example/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/mkobit/jenkins-pipeline-shared-library-example/actions/workflows/build.yml)

An example [Jenkins Pipeline Shared Library](https://jenkins.io/doc/book/pipeline/shared-libraries/) built with the [Shared Library Gradle plugin](https://github.com/mkobit/jenkins-pipeline-shared-libraries-gradle-plugin).

## Project layout

| Path | Contents |
|---|---|
| `src/` | Groovy shared library classes |
| `vars/` | Pipeline step scripts |
| `resources/` | Files accessible via `libraryResource()` |
| `test/unit/` | [JenkinsPipelineUnit](https://github.com/lesfurets/JenkinsPipelineUnit) — fast, no Jenkins runtime |
| `test/integration/` | Plugin-provided integration suite — [JUnit Jupiter](https://junit.org/junit5/) via `@WithJenkins` |

## Library contents

### `src/` — shared classes

| Class | Purpose |
|---|---|
| `PipelineLogger` | Interface with numeric log levels (1=DEBUG, 2=INFO, 3=WARN, 4=ERROR) read from `PIPELINE_LOG_LEVEL` env var |
| `BasicScriptStepsLogger` | `PipelineLogger` implementation — plain `echo` output |
| `AnsiColorScriptStepsLogger` | `PipelineLogger` implementation — ANSI-colored output (requires [AnsiColor plugin](https://plugins.jenkins.io/ansicolor/)) |
| `BuildContext` | Reads job metadata from `env` eagerly; holds no `script` reference after construction |
| `BranchPolicy` | Classifies a branch name into environment (`production`, `staging`, `development`); `@CompileStatic` |
| `ConventionalCommit` | Parses and classifies a [Conventional Commit](https://www.conventionalcommits.org/) message |
| `SemVer` | Parses, compares, and bumps semantic version strings |

### `vars/` — pipeline steps

| Step | Signature | Purpose |
|---|---|---|
| `captureBuildInfo` | `call(PipelineLogger log = null)` | Logs job metadata; demonstrates optional logger injection |
| `eventually` | `call(Map opts = [:], Closure condition)` | Polls a boolean closure with exponential backoff |
| `nextReleaseVersion` | `call(String fallback = 'v0.0.0')` | Computes next version from git tags and conventional commits |
| `notifyBuild` | `call(String status)` | Emits a build notification message with job metadata |
| `requireConventionalPrTitle` | `call()` | Fails the build if the PR title is not a conventional commit |
| `requireEnv` | `call(String... names)` | Fails the build if any named env vars are absent |
| `tagBuild` | `call(String label)` | Sets `currentBuild.displayName` and `description` |
| `withParallelMatrix` | `call(Map axes, Closure body)` | Runs a closure for every combination of axis values in parallel |

## Design patterns demonstrated

**Dependency injection via optional parameter** — `captureBuildInfo` accepts a `PipelineLogger` defaulting to `null`, resolved with an Elvis operator:

```groovy
def call(PipelineLogger log = null) {
    def effectiveLog = log ?: new BasicScriptStepsLogger(this, 'captureBuildInfo')
    ...
}
```

Callers can inject `AnsiColorScriptStepsLogger`, a test mock, or omit the argument entirely.

**CPS safety** — Groovy GDK methods such as `padRight` and collection methods with closures are CPS-unsafe in Jenkins pipelines.
Methods that use them are annotated `@NonCPS`.
Plain `for` loops and `.collectMany`/`.collectEntries` without closures are CPS-safe.

**Serialization pattern** — Three distinct patterns across the src classes:

| Class | Holds `script` ref | `transient` | When serialized |
|---|---|---|---|
| `BasicScriptStepsLogger` | Yes | Yes (field not serialized) | Logger reconstructed on resume |
| `BuildContext` | No | N/A | Fully safe; all primitives |
| `BranchPolicy` | No | N/A | Fully safe; all primitives |

**`@CompileStatic` on value classes** — `BranchPolicy` uses `@CompileStatic` because all its methods are `@NonCPS` and the class holds only plain primitives.

## Running tests

| Task | Runs |
|---|---|
| `./gradlew test` | Unit tests (fast, no Jenkins runtime) |
| `./gradlew integrationTest` | Integration suite (starts embedded Jenkins) |
| `./gradlew check` | All suites + CodeNarc + Spotless |

## Using this as a template

1. Replace `src/com/mkobit/libraryexample/` with your package structure and update `vars/`.
2. Set `rootProject.name` in `settings.gradle.kts` to your Jenkins library name.
3. To target a different Jenkins LTS line, set `sharedLibrary { jenkins { version = "..." } }` in `build.gradle.kts` (default: `2.479.1`).
4. Pin to a released plugin version on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.mkobit.jenkins.pipelines.shared-library).
5. Drop the composite-build wiring used during plugin development:
   - Remove `includeBuild("../jenkins-pipeline-shared-libraries-gradle-plugin")` from `settings.gradle.kts`.
   - Remove the `// TEMPLATE FORK` blocks in `.github/workflows/build.yml`.
