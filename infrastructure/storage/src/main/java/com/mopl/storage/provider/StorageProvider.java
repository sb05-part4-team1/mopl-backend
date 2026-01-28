package com.mopl.storage.provider;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;

public interface StorageProvider {

    void upload(InputStream inputStream, long contentLength, String path);

    String getUrl(String path);

    Resource download(String relativePath);

    void delete(String path);

    boolean exists(String path);

    List<String> listObjects(String prefix, int maxKeys);
}
