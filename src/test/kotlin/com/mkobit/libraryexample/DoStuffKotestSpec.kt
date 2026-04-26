package com.mkobit.libraryexample

import com.lesfurets.jenkins.unit.BasePipelineTest
import hudson.model.Item
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe

/**
 * Demonstrates a Kotlin Kotest unit test in a shared library project.
 * Shows that Jenkins API types and JPU are on the Kotlin test classpath.
 */
class DoStuffKotestSpec :
  StringSpec({
    "Jenkins API types are available on the Kotlin test classpath" {
      Item::class.java shouldNotBe null
    }

    "Jenkins Pipeline Unit is available on the Kotlin test classpath" {
      BasePipelineTest::class.java shouldNotBe null
    }
  })
