package com.mopl.api.interfaces.api.user;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.api.application.user.FollowFacade;
import com.mopl.domain.model.user.FollowModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController implements FollowApiSpec {

    private final FollowFacade followFacade;
    private final FollowResponseMapper followResponseMapper;

    @Override
    @PostMapping
    public ResponseEntity<FollowResponse> follow(
        /*@AuthenticationPrincipal*/ UUID followerId,
        @Valid @RequestBody FollowRequest request
    ) {
        FollowModel follow = followFacade.follow(followerId, request.followeeId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(followResponseMapper.toResponse(follow));
    }

    @Override
    @DeleteMapping("/{followId}")
    public ResponseEntity<Void> unFollow(
        /*@AuthenticationPrincipal*/ UUID userId,
        @PathVariable UUID followId
    ) {
        followFacade.unFollow(userId, followId);
        return ResponseEntity.noContent().build();
    }
}
