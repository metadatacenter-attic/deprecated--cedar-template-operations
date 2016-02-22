package org.metadatacenter.server.service;

import checkers.nullness.quals.NonNull;

public interface DiagnosticsService<T> {

  @NonNull
  public T heartbeat();
}
