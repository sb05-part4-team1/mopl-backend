package com.mopl.api.interfaces.api.follow;

import java.util.UUID;

import com.mopl.api.interfaces.api.follow.dto.FollowRequest;
import com.mopl.dto.follow.FollowResponse;
import com.mopl.dto.follow.FollowResponseMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.api.application.follow.FollowFacade;
import com.mopl.domain.model.follow.FollowModel;
import com.mopl.security.userdetails.MoplUserDetails;

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
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @Valid @RequestBody FollowRequest request
    ) {
        FollowModel follow = followFacade.follow(userDetails.userId(), request.followeeId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(followResponseMapper.toResponse(follow));
    }

    @Override
    @DeleteMapping("/{followId}")
    public ResponseEntity<Void> unFollow(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID followId
    ) {
        followFacade.unFollow(userDetails.userId(), followId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/count")
    public ResponseEntity<Long> getFollowCount(@RequestParam UUID followeeId) {
        long count = followFacade.getFollowerCount(followeeId);
        return ResponseEntity.ok(count);
    }

    @Override
    @GetMapping("/followed-by-me")
    public ResponseEntity<Boolean> getFollowStatus(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestParam UUID followeeId
    ) {
        boolean follow = followFacade.isFollow(userDetails.userId(), followeeId);
        return ResponseEntity.ok(follow);
    }
}
