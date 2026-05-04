package com.mkobit.libraryexample

import io.kotest.core.spec.style.FunSpec
import org.jvnet.hudson.test.JenkinsRule

// JenkinsRule.recipe() reads a JUnit 4 Description to process test annotations (@LocalData etc.).
// Overriding it to call only recipeLoadCurrentPlugin() makes JenkinsRule safe to use from
// Kotest's beforeSpec/afterSpec without any JUnit 4 framework dependency.
abstract class JenkinsFunSpec(
  body: JenkinsFunSpec.() -> Unit,
) : FunSpec() {
  val rule: JenkinsRule =
    object : JenkinsRule() {
      override fun recipe() = recipeLoadCurrentPlugin()
    }

  init {
    beforeSpec {
      rule.timeout = 30
      rule.before()
    }
    afterSpec { rule.after() }
    body()
  }
}
