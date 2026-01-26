package com.mopl.api.interfaces.api.follow;

import com.mopl.api.application.follow.FollowFacade;
import com.mopl.api.interfaces.api.follow.dto.FollowRequest;
import com.mopl.dto.follow.FollowResponse;
import com.mopl.dto.follow.FollowStatusResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController implements FollowApiSpec {

    private final FollowFacade followFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FollowResponse follow(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestBody @Valid FollowRequest request
    ) {
        return followFacade.follow(userDetails.userId(), request.followeeId());
    }

    @DeleteMapping("/{followId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unFollow(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID followId
    ) {
        followFacade.unFollow(userDetails.userId(), followId);
    }

    @GetMapping("/count")
    public long getFollowCount(@RequestParam UUID followeeId) {
        return followFacade.getFollowerCount(followeeId);
    }

    @GetMapping("/followed-by-me")
    public FollowStatusResponse getFollowStatus(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestParam UUID followeeId
    ) {
        return followFacade.getFollowStatus(userDetails.userId(), followeeId);
    }
}
