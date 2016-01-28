package org.metadatacenter.server.service;

import checkers.nullness.quals.NonNull;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.util.List;

public interface TemplateService<K, T> {

  @NonNull
  public T createTemplate(@NonNull T template) throws IOException;

  @NonNull
  public List<T> findAllTemplates() throws IOException;

  @NonNull
  public List<T> findAllTemplates(List<String> fieldNames, FieldNameInEx includeExclude) throws IOException;

  @NonNull
  public List<T> findAllTemplates(Integer limit, Integer offset, List<String> fieldNames, FieldNameInEx
      includeExclude) throws IOException;

  public T findTemplate(@NonNull K templateId) throws IOException, ProcessingException;

  @NonNull
  public T updateTemplate(@NonNull K templateId, @NonNull T modifications) throws InstanceNotFoundException,
      IOException;

  public void deleteTemplate(@NonNull K templateId) throws InstanceNotFoundException, IOException;

  @NonNull
  public boolean existsTemplate(@NonNull K templateId) throws IOException;

  public void deleteAllTemplates();

  public long count();
}
