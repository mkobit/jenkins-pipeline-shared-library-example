package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BranchPolicyTest {

  @Test
  void mainBranchIsProductionEnvironment() {
    var policy = new BranchPolicy("main");
    assertTrue(policy.isMain());
    assertFalse(policy.isRelease());
    assertFalse(policy.isPullRequest());
    assertEquals("production", policy.getEnvironment());
  }

  @Test
  void masterBranchIsProductionEnvironment() {
    var policy = new BranchPolicy("master");
    assertTrue(policy.isMain());
    assertEquals("production", policy.getEnvironment());
  }

  @Test
  void releaseBranchIsStagingEnvironment() {
    var policy = new BranchPolicy("release/1.2.3");
    assertTrue(policy.isRelease());
    assertFalse(policy.isMain());
    assertEquals("staging", policy.getEnvironment());
  }

  @Test
  void prBranchIsDevelopmentEnvironment() {
    var policy = new BranchPolicy("PR-42");
    assertTrue(policy.isPullRequest());
    assertFalse(policy.isMain());
    assertFalse(policy.isRelease());
    assertEquals("development", policy.getEnvironment());
  }

  @Test
  void featureBranchIsDevelopmentEnvironment() {
    var policy = new BranchPolicy("feature/new-thing");
    assertFalse(policy.isMain());
    assertFalse(policy.isRelease());
    assertFalse(policy.isPullRequest());
    assertEquals("development", policy.getEnvironment());
  }
}
