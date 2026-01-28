package com.mopl.api.interfaces.api.storage;

import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@ConditionalOnProperty(name = "mopl.storage.type", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class FileController implements FileApiSpec {

    private final StorageProvider storageProvider;

    @GetMapping("/display")
    public ResponseEntity<Resource> display(@RequestParam String path) {
        Resource resource = storageProvider.download(path);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
            .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(resource);
    }
}
