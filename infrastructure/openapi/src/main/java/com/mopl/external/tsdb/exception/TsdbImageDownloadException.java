package com.mopl.external.tsdb.exception;

public class TsdbImageDownloadException extends RuntimeException {

    private final String imageUrl;

    public TsdbImageDownloadException(String imageUrl, Throwable cause) {
        super("TSDB image download failed: imageUrl=" + imageUrl, cause);
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
