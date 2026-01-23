package com.mopl.storage.provider;

import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface StorageProvider {

    void upload(InputStream inputStream, String path);

    String getUrl(String path);

    Resource download(String relativePath);

    void delete(String path);

    boolean exists(String path);
}
