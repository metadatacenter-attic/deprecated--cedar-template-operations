package org.metadatacenter.server.service.mongodb;

import checkers.nullness.quals.NonNull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import org.metadatacenter.constant.CedarConstants;
import org.metadatacenter.server.dao.mongodb.TemplateFieldDaoMongoDB;
import org.metadatacenter.server.service.FieldNameInEx;
import org.metadatacenter.server.service.TemplateFieldService;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TemplateFieldServiceMongoDB extends GenericTemplateServiceMongoDB<String, JsonNode> implements
    TemplateFieldService<String, JsonNode> {

  @NonNull
  private final TemplateFieldDaoMongoDB templateFieldDao;

  public TemplateFieldServiceMongoDB(@NonNull String db, @NonNull String templateFieldsCollection, String
      linkedDataIdBasePath) {
    this.templateFieldDao = new TemplateFieldDaoMongoDB(db, templateFieldsCollection, linkedDataIdBasePath);
  }

  @Override
  @NonNull
  public List<JsonNode> findAllTemplateFields(Integer limit, Integer offset, List<String> fieldNames, FieldNameInEx
      includeExclude) throws IOException {
    return templateFieldDao.findAll(limit, offset, fieldNames, includeExclude);
  }

  @Override
  public JsonNode findTemplateField(@NonNull String templateFieldId) throws IOException,
      ProcessingException {
    return templateFieldDao.find(templateFieldId);
  }

  @Override
  public long count() {
    return templateFieldDao.count();
  }

  @Override
  public void saveNewFieldsAndReplaceIds(JsonNode genericInstance) throws IOException {

    JsonNode properties = genericInstance.get("properties");
    Iterator<Map.Entry<String, JsonNode>> it = properties.fields();
    while (it.hasNext()) {
      Map.Entry<String, JsonNode> entry = it.next();
      JsonNode fieldCandidate = entry.getValue();
      // If the entry is an object
      if (fieldCandidate.isObject()) {
        if (fieldCandidate.get("@id") != null) {
          String id = fieldCandidate.get("@id").asText();
          if (id != null && id.indexOf(CedarConstants.TEMP_ID_PREFIX) == 0) {
            JsonNode removeId = ((ObjectNode) fieldCandidate).remove("@id");
            templateFieldDao.create(fieldCandidate);
          }
        }
      }
    }
  }

}
