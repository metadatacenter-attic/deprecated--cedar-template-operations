package org.metadatacenter.server.dao.mongodb;

import checkers.nullness.quals.NonNull;

public class TemplateElementDaoMongoDB extends GenericLDDaoMongoDB {

  public TemplateElementDaoMongoDB(@NonNull String dbName, @NonNull String collectionName, String
      linkedDataIdBasePath) {
    super(dbName, collectionName, linkedDataIdBasePath);
  }
}
