package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SemVerTest {

  @Test
  void parsesWithAndWithoutVPrefix() {
    assertEquals("v1.2.3", SemVer.parse("1.2.3").toString());
    assertEquals("v1.2.3", SemVer.parse("v1.2.3").toString());
  }

  @Test
  void bumpMajorResetsMinorAndPatch() {
    assertEquals("v2.0.0", SemVer.parse("v1.2.3").bumpMajor().toString());
  }

  @Test
  void bumpMinorResetsPatch() {
    assertEquals("v1.3.0", SemVer.parse("v1.2.3").bumpMinor().toString());
  }

  @Test
  void bumpPatchIncrementsPatch() {
    assertEquals("v1.2.4", SemVer.parse("v1.2.3").bumpPatch().toString());
  }

  @Test
  void comparesToOtherVersionsCorrectly() {
    assertTrue(SemVer.parse("v1.2.3").compareTo(SemVer.parse("v1.2.4")) < 0);
    assertTrue(SemVer.parse("v2.0.0").compareTo(SemVer.parse("v1.9.9")) > 0);
    assertEquals(0, SemVer.parse("v1.0.0").compareTo(SemVer.parse("v1.0.0")));
  }
}
