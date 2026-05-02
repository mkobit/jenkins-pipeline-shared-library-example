package com.mkobit.libraryexample

import org.jvnet.hudson.test.JenkinsRule

// JenkinsRule.recipe() reads a JUnit 4 Description to process test annotations (@LocalData etc.).
// Overriding it to skip that lookup makes JenkinsRule usable under JUnit Platform runners like
// Kotest without any JUnit 4 framework dependency.
internal class KotestJenkinsRule : JenkinsRule() {
  override fun recipe() = recipeLoadCurrentPlugin()
}
