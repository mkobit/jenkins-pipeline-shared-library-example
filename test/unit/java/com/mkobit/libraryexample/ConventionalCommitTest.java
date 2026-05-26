package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ConventionalCommitTest {

  @Test
  void parsesFeatureWithScope() {
    var c = ConventionalCommit.parse("feat(auth): add OAuth login");
    assertNotNull(c);
    assertEquals("feat", c.getType());
    assertFalse(c.isBreaking());
    assertEquals("minor", c.getBumpType());
  }

  @Test
  void detectsBreakingChangeViaBang() {
    var c = ConventionalCommit.parse("feat!: redesign public API");
    assertNotNull(c);
    assertTrue(c.isBreaking());
    assertEquals("major", c.getBumpType());
  }

  @Test
  void fixAndPerfArePatchBumps() {
    assertEquals("patch", ConventionalCommit.parse("fix: null check").getBumpType());
    assertEquals("patch", ConventionalCommit.parse("perf: cache results").getBumpType());
  }

  @Test
  void choreAndDocsHaveNoBump() {
    assertNull(ConventionalCommit.parse("chore: update deps").getBumpType());
    assertNull(ConventionalCommit.parse("docs: fix typo").getBumpType());
  }

  @Test
  void returnsNullForNonConventionalMessage() {
    assertNull(ConventionalCommit.parse("fix some stuff without a colon"));
    assertNull(ConventionalCommit.parse("WIP"));
  }

  @Test
  void highestBumpReturnsMajorWhenBreakingPresent() {
    assertEquals(
        "major",
        ConventionalCommit.highestBump(
            List.of("feat: new thing", "fix!: breaking fix", "chore: cleanup")));
  }

  @Test
  void highestBumpReturnsMinorForFeatWithoutBreaking() {
    assertEquals("minor", ConventionalCommit.highestBump(List.of("fix: a", "feat: b", "chore: c")));
  }

  @Test
  void highestBumpReturnsPatchWhenOnlyFixes() {
    assertEquals("patch", ConventionalCommit.highestBump(List.of("fix: a", "chore: b", "docs: c")));
  }

  @Test
  void highestBumpReturnsNullForNoReleasableCommits() {
    assertNull(ConventionalCommit.highestBump(List.of("chore: a", "docs: b")));
  }
}
