package org.metadatacenter.server.service.mongodb;

import checkers.nullness.quals.NonNull;
import com.fasterxml.jackson.databind.JsonNode;
import org.metadatacenter.server.dao.mongodb.TemplateInstanceDaoMongoDB;
import org.metadatacenter.server.service.FieldNameInEx;
import org.metadatacenter.server.service.TemplateInstanceService;

import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.util.List;

public class TemplateInstanceServiceMongoDB extends GenericTemplateServiceMongoDB<String, JsonNode> implements
    TemplateInstanceService<String, JsonNode> {

  @NonNull
  private final TemplateInstanceDaoMongoDB templateInstanceDao;

  public TemplateInstanceServiceMongoDB(@NonNull String db, @NonNull String templateInstancesCollection, String
      linkedDataIdBasePath) {
    this.templateInstanceDao = new TemplateInstanceDaoMongoDB(db, templateInstancesCollection, linkedDataIdBasePath);
  }

  @Override
  @NonNull
  public JsonNode createTemplateInstance(@NonNull JsonNode templateInstance) throws IOException {
    return templateInstanceDao.create(templateInstance);
  }

  @Override
  @NonNull
  public List<JsonNode> findAllTemplateInstances() throws IOException {
    return templateInstanceDao.findAll();
  }

  @Override
  @NonNull
  public List<JsonNode> findAllTemplateInstances(List<String> fieldNames, FieldNameInEx includeExclude) throws
      IOException {
    return templateInstanceDao.findAll(fieldNames, includeExclude);
  }

  @Override
  @NonNull
  public List<JsonNode> findAllTemplateInstances(Integer limit, Integer offset, List<String> fieldNames,
                                                 FieldNameInEx includeExclude) throws IOException {
    return templateInstanceDao.findAll(limit, offset, fieldNames, includeExclude);
  }

  @Override
  public JsonNode findTemplateInstance(@NonNull String templateInstanceId)
      throws IOException {
    return templateInstanceDao.find(templateInstanceId);
  }

  @Override
  @NonNull
  public JsonNode updateTemplateInstance(@NonNull String templateInstanceId, @NonNull JsonNode modifications)
      throws InstanceNotFoundException, IOException {
    return templateInstanceDao.update(templateInstanceId, modifications);
  }

  @Override
  public void deleteTemplateInstance(@NonNull String templateInstanceId) throws InstanceNotFoundException, IOException {
    templateInstanceDao.delete(templateInstanceId);
  }

  @Override
  public long count() {
    return templateInstanceDao.count();
  }

}
