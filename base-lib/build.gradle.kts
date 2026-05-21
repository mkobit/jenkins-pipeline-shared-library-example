plugins {
  // Plugin already on the classpath via the root project — see config-lib/build.gradle.kts.
  id("com.mkobit.jenkins.pipelines.shared-library")
}

// :base-lib is the third level of the transitive chain. :config-lib declares this as a peer,
// and root depends on :config-lib only — so :base-lib must reach Jenkins via source-variant
// transitivity (sharedLibrarySourceElements extendsFrom sharedLibraryDependencies).
sharedLibrary {
}
