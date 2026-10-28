package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceDocs.expected.types;

import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceDocs.expected.Generated;
import java.lang.String;

/**
 * Anything with a title!
 */
@Generated
public interface Titled {
  String getTitle();

  void setTitle(String title);
}
