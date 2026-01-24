package com.mopl.websocket.interfaces.api.user.mapper;

import com.mopl.domain.model.user.UserModel;
import com.mopl.storage.provider.StorageProvider;
import com.mopl.websocket.interfaces.api.user.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSummaryMapper {

    private final StorageProvider storageProvider;

    public UserSummary toSummary(UserModel userModel) {
        if (userModel == null) {
            return null;
        }

        return new UserSummary(
            userModel.getId(),
            userModel.getName(),
            storageProvider.getUrl(userModel.getProfileImagePath())
        );
    }
}
