package org.metadatacenter.server.service.mongodb;

import checkers.nullness.quals.NonNull;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import org.metadatacenter.server.dao.mongodb.TemplateDaoMongoDB;
import org.metadatacenter.server.service.FieldNameInEx;
import org.metadatacenter.server.service.TemplateElementService;
import org.metadatacenter.server.service.TemplateService;

import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.util.List;

public class TemplateServiceMongoDB extends GenericTemplateServiceMongoDB<String, JsonNode> implements
    TemplateService<String, JsonNode> {

  @NonNull
  private final TemplateDaoMongoDB templateDao;

  @NonNull
  private final TemplateElementService templateElementService;


  public TemplateServiceMongoDB(@NonNull String db, @NonNull String templatesCollection, String linkedDataIdBasePath,
                                TemplateElementService templateElementService) {
    this.templateDao = new TemplateDaoMongoDB(db, templatesCollection, linkedDataIdBasePath);
    this.templateElementService = templateElementService;
  }

  @Override
  @NonNull
  public JsonNode createTemplate(@NonNull JsonNode template) throws IOException {
    return templateDao.create(template);
  }

  @Override
  @NonNull
  public List<JsonNode> findAllTemplates() throws IOException {
    return templateDao.findAll();
  }

  @Override
  @NonNull
  public List<JsonNode> findAllTemplates(List<String> fieldNames, FieldNameInEx includeExclude) throws IOException {
    return templateDao.findAll(fieldNames, includeExclude);
  }

  @Override
  @NonNull
  public List<JsonNode> findAllTemplates(Integer limit, Integer offset, List<String> fieldNames, FieldNameInEx
      includeExclude) throws IOException {
    return templateDao.findAll(limit, offset, fieldNames, includeExclude);
  }

  @Override
  public JsonNode findTemplate(@NonNull String templateId)
      throws IOException, ProcessingException {
    return templateDao.find(templateId);
  }

  @Override
  @NonNull
  public JsonNode updateTemplate(@NonNull String templateId, @NonNull JsonNode modifications)
      throws InstanceNotFoundException, IOException {
    return templateDao.update(templateId, modifications);
  }

  @Override
  public void deleteTemplate(@NonNull String templateId) throws InstanceNotFoundException, IOException {
    templateDao.delete(templateId);
  }

  @Override
  public boolean existsTemplate(@NonNull String templateId) throws IOException {
    return templateDao.exists(templateId);
  }

  @Override
  public void deleteAllTemplates() {
    templateDao.deleteAll();
  }

  @Override
  public long count() {
    return templateDao.count();
  }


}
