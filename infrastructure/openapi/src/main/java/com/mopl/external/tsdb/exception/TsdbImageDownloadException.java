package com.mopl.external.tsdb.exception;

import lombok.Getter;

@Getter
public class TsdbImageDownloadException extends RuntimeException {

    private final String imageUrl;

    public TsdbImageDownloadException(String imageUrl, Throwable cause) {
        super("TSDB image download failed: imageUrl=" + imageUrl, cause);
        this.imageUrl = imageUrl;
    }
}
