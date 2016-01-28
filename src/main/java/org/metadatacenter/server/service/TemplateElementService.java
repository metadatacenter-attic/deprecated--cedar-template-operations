package org.metadatacenter.server.service;

import checkers.nullness.quals.NonNull;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.util.List;

public interface TemplateElementService<K, T> {

  @NonNull
  public T createTemplateElement(@NonNull T templateElement) throws IOException;

  @NonNull
  public List<T> findAllTemplateElements() throws IOException;

  @NonNull
  public List<T> findAllTemplateElements(List<String> fieldName, FieldNameInEx includeExclude) throws IOException;

  @NonNull
  public List<T> findAllTemplateElements(Integer limit, Integer offset, List<String> fieldName, FieldNameInEx
      includeExclude) throws IOException;

  public T findTemplateElement(@NonNull K templateElementId) throws IOException, ProcessingException;

  @NonNull
  public T updateTemplateElement(@NonNull K templateElementId, @NonNull T modifications) throws
      InstanceNotFoundException, IOException;

  public void deleteTemplateElement(@NonNull K templateElementId) throws InstanceNotFoundException, IOException;

  @NonNull
  public boolean existsTemplateElement(@NonNull K templateElementId) throws IOException;

  public void deleteAllTemplateElements();

  public long count();
}