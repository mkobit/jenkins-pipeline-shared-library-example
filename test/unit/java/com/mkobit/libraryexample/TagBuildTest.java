package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.Test;

class TagBuildTest extends PipelineTest {

  @Test
  void setsDisplayNameAndDescriptionOnCurrentBuild() {
    var mockBuild = new groovy.util.Expando();
    mockBuild.setProperty("number", 1);
    mockBuild.setProperty("displayName", "");
    mockBuild.setProperty("description", "");
    base.getBinding().setProperty("currentBuild", mockBuild);
    InvokerHelper.invokeMethod(base.loadScript("vars/tagBuild.groovy"), "call", "v2.0.0");
    assertEquals("#1 v2.0.0", mockBuild.getProperty("displayName").toString());
    assertEquals("v2.0.0", mockBuild.getProperty("description").toString());
  }
}
