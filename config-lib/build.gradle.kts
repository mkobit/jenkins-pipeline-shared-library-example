plugins {
  // No version: the plugin is already on the classpath via the root project's `alias()`
  // application. Repeating `alias(libs.plugins.shared.library)` here would request the
  // catalog-pinned 0.11.0 against an includeBuild-substituted plugin of "unknown version",
  // which Gradle's plugin classpath check rejects.
  id("com.mkobit.jenkins.pipelines.shared-library")
}

// :config-lib is a peer Jenkins shared library consumed by the root :main library via
// `sharedLibrary(project(":config-lib"))`. Demonstrates the multi-project peer dependency
// scenario in issue #158. Declares :base-lib as its own peer so root reaches :base-lib only
// through source-variant transitivity (sharedLibrarySourceElements extendsFrom).
sharedLibrary {
  dependencies {
    sharedLibrary(project(":base-lib"))
  }
}
