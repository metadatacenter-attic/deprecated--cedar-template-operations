package org.metadatacenter.server.dao.mongodb;

import checkers.nullness.quals.NonNull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.metadatacenter.server.dao.GenericDao;
import org.metadatacenter.server.service.FieldNameInEx;
import org.metadatacenter.util.FixMongoDirection;
import org.metadatacenter.util.MongoFactory;
import org.metadatacenter.util.json.JsonUtils;

import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;
import static com.mongodb.client.model.Filters.eq;

/**
 * Service to manage elements in a MongoDB database
 */
public class GenericLDDaoMongoDB implements GenericDao<String, JsonNode> {

  @NonNull
  protected final MongoCollection<Document> entityCollection;
  private final @NonNull JsonUtils jsonUtils;

  private String linkedDataIdBasePath;

  public GenericLDDaoMongoDB(@NonNull String dbName, @NonNull String collectionName, String linkedDataIdBasePath) {
    MongoClient mongoClient = MongoFactory.getClient();
    entityCollection = mongoClient.getDatabase(dbName).getCollection(collectionName);
    jsonUtils = new JsonUtils();
    this.linkedDataIdBasePath = linkedDataIdBasePath;
    // TODO: close mongoClient after using it
  }

  /* CRUD operations */

  /**
   * Create an element that contains a Linked Data identifier field (@id in JSON-LD). It is necessary to check that
   * there are not other elements into the DB with the same @id.
   *
   * @param element An element
   * @return The created element
   * @throws IOException If an occurs during creation
   */
  @Override
  @NonNull
  public JsonNode create(@NonNull JsonNode element) throws IOException {
    if ((element.get("@id") != null) && (!NULL.equals(element.get("@id").getNodeType()))) {
      throw new IllegalArgumentException("Specifying @id for new objects is not allowed");
    }
    String id = null;
    // Generate a non-existing uuid
    do {
      id = linkedDataIdBasePath + UUID.randomUUID().toString();
    } while (find(id) != null);
    ((ObjectNode) element).put("@id", id);

    // Adapts all keys not accepted by MongoDB
    JsonNode fixedElement = jsonUtils.fixMongoDB(element, FixMongoDirection.WRITE_TO_MONGO);
    ObjectMapper mapper = new ObjectMapper();
    Map elementMap = mapper.convertValue(fixedElement, Map.class);
    Document elementDoc = new Document(elementMap);
    entityCollection.insertOne(elementDoc);
    // Returns the document created (all keys adapted for MongoDB are restored)
    return jsonUtils.fixMongoDB(mapper.readTree(elementDoc.toJson()), FixMongoDirection.READ_FROM_MONGO);
  }

  /**
   * Find all elements
   *
   * @return A list of elements
   * @throws IOException If an error occurs during retrieval
   */
  @Override
  @NonNull
  public List<JsonNode> findAll() throws IOException {
    return findAll(null, null, null, FieldNameInEx.UNDEFINED);
  }

  @Override
  @NonNull
  public List<JsonNode> findAll(List<String> fieldNames, FieldNameInEx includeExclude) throws IOException {
    return findAll(null, null, fieldNames, includeExclude);
  }

  @Override
  @NonNull
  public List<JsonNode> findAll(Integer limit, Integer offset, List<String> fieldNames, FieldNameInEx includeExclude)
      throws IOException {
    FindIterable<Document> findIterable = entityCollection.find();
    if (limit != null) {
      findIterable.limit(limit);
    }
    if (offset != null) {
      findIterable.skip(offset);
    }
    if (fieldNames != null && fieldNames.size() > 0) {
      Bson fields = null;
      switch (includeExclude) {
        case INCLUDE:
          fields = Projections.fields(Projections.include(fieldNames), Projections.excludeId());
          break;
        case EXCLUDE:
          fields = Projections.exclude(fieldNames);
          break;
      }
      if (fields != null) {
        findIterable.projection(fields);
      }
    }
    MongoCursor<Document> cursor = findIterable.iterator();
    ObjectMapper mapper = new ObjectMapper();
    List<JsonNode> docs = new ArrayList<>();
    try {
      while (cursor.hasNext()) {
        JsonNode node = jsonUtils.fixMongoDB(mapper.readTree(cursor.next().toJson()), FixMongoDirection
            .READ_FROM_MONGO);
        docs.add(node);
      }
    } finally {
      cursor.close();
    }
    return docs;
  }

  /**
   * Find an element using its linked data ID  (@id in JSON-LD)
   *
   * @param id The linked data ID of the element
   * @return A JSON representation of the element or null if the element was not found
   * @throws IllegalArgumentException If the ID is not valid
   * @throws IOException              If an error occurs during retrieval
   */
  @Override
  public JsonNode find(@NonNull String id) throws IOException {
    if ((id == null) || (id.length() == 0)) {
      throw new IllegalArgumentException();
    }
    Document doc = entityCollection.find(eq("@id", id)).first();
    if (doc == null) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    return jsonUtils.fixMongoDB(mapper.readTree(doc.toJson()), FixMongoDirection.READ_FROM_MONGO);
  }

  /**
   * Update an element using its linked data ID  (@id in JSON-LD)
   *
   * @param id            The linked data ID of the element to update
   * @param modifications The update
   * @return The updated JSON representation of the element
   * @throws IllegalArgumentException  If the ID is not valid
   * @throws InstanceNotFoundException If the element is not found
   * @throws IOException               If an error occurs during update
   */
  @Override
  @NonNull
  public JsonNode update(@NonNull String id, @NonNull JsonNode modifications)
      throws InstanceNotFoundException, IOException {
    if ((id == null) || (id.length() == 0)) {
      throw new IllegalArgumentException();
    }
    if (!exists(id)) {
      throw new InstanceNotFoundException();
    }
    // Adapts all keys not accepted by MongoDB
    modifications = jsonUtils.fixMongoDB(modifications, FixMongoDirection.WRITE_TO_MONGO);
    ObjectMapper mapper = new ObjectMapper();
    Map modificationsMap = mapper.convertValue(modifications, Map.class);
    UpdateResult updateResult = entityCollection.updateOne(eq("@id", id), new Document("$set", modificationsMap));
    if (updateResult.getMatchedCount() == 1) {
      return find(id);
    } else {
      throw new InternalError();
    }
  }

  /**
   * Delete an element using its linked data ID  (@id in JSON-LD)
   *
   * @param id The linked data ID of the element to delete
   * @throws IllegalArgumentException  If the ID is not valid
   * @throws InstanceNotFoundException If the element is not found
   * @throws IOException               If an error occurs during deletion
   */
  @Override
  public void delete(@NonNull String id) throws InstanceNotFoundException, IOException {
    if ((id == null) || (id.length() == 0)) {
      throw new IllegalArgumentException();
    }
    if (!exists(id)) {
      throw new InstanceNotFoundException();
    }
    DeleteResult deleteResult = entityCollection.deleteOne(eq("@id", id));
    if (deleteResult.getDeletedCount() != 1) {
      throw new InternalError();
    }
  }

  /**
   * Check if an element exists using its linked data ID  (@id in JSON-LD)
   *
   * @param id The linked data ID of the element
   * @return True if an element with the supplied linked data ID  exists or False otherwise
   * @throws IOException If an error occurs during the existence check
   */
  @Override
  public boolean exists(@NonNull String id) throws IOException {
    return (find(id) != null);
  }

  /**
   * Delete all elements
   */
  @Override
  public void deleteAll() {
    entityCollection.drop();
  }

  @Override
  public long count() {
    return entityCollection.count();
  }

}
