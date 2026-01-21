package com.mopl.api.interfaces.api.storage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "File API", description = "파일 API")
public interface FileApiSpec {

    @Operation(
        summary = "파일 불러오기"
    )
    public ResponseEntity<Resource> display(@RequestParam String path);

}
