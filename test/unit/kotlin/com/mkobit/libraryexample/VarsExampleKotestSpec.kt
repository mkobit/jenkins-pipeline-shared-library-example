package com.mkobit.libraryexample

import hudson.model.Item
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe

class VarsExampleKotestSpec :
  StringSpec({
    "Jenkins API types are available on unit test classpath" {
      Item::class.java shouldNotBe null
    }
  })
