package com.mopl.api.interfaces.api.storage;

import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final StorageProvider storageProvider;

    @GetMapping("/display")
    public ResponseEntity<Resource> display(@RequestParam String path) {
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(storageProvider.download(path));
    }
}
