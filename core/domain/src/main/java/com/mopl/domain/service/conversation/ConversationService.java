package com.mopl.domain.service.conversation;

import com.mopl.domain.exception.conversation.ConversationAccessDeniedException;
import com.mopl.domain.exception.conversation.ConversationNotFoundException;
import com.mopl.domain.exception.conversation.DirectMessageNotFoundException;
import com.mopl.domain.exception.conversation.ReadStatusNotFoundException;
import com.mopl.domain.exception.user.UserNotFoundException;
import com.mopl.domain.model.conversation.ConversationModel;
import com.mopl.domain.model.conversation.DirectMessageModel;
import com.mopl.domain.model.conversation.ReadStatusModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.conversation.ConversationQueryRepository;
import com.mopl.domain.repository.conversation.ConversationQueryRequest;
import com.mopl.domain.repository.conversation.ConversationRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRepository;
import com.mopl.domain.repository.conversation.DirectMessageQueryRequest;
import com.mopl.domain.repository.conversation.DirectMessageRepository;
import com.mopl.domain.repository.conversation.ReadStatusRepository;
import com.mopl.domain.repository.user.UserRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ReadStatusRepository readStatusRepository;
    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final ConversationQueryRepository conversationQueryRepository;
    private final DirectMessageQueryRepository directMessageQueryRepository;

    public ConversationModel create(
        ConversationModel conversationModel,
        UserModel userModel,
        UserModel withUserModel
    ) {
        ConversationModel model = conversationRepository.save(conversationModel);
        model.withUser(userModel);  // message/hasUnread에 null/false 넣어둠.

        // ReadStatus 생성 후 저장 , 위는 상대/아래는 본인
        ReadStatusModel withReadStatusModel = readStatusRepository.save(ReadStatusModel.create(
            model, model.getWithUser()));
        ReadStatusModel userReadStatusModel = readStatusRepository.save(ReadStatusModel.create(
            model, userModel));

        return model;
    }

    public CursorResponse<DirectMessageModel> getAllDirectMessage(
        UUID conversationId,
        DirectMessageQueryRequest request,
        UUID userId
    ) {
        CursorResponse<DirectMessageModel> directMessageModels = directMessageQueryRepository
            .findAllByConversationId(
                conversationId, request, userId
            );

        if (!conversationQueryRepository.existsParticipant(conversationId, userId)) {
            throw new ConversationAccessDeniedException(conversationId, userId);
        }


        ReadStatusModel otherReadStatusModel = readStatusRepository
            .findOtherReadStatus(conversationId, userId);
        UserModel userModel = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.withId(userId));
        for (DirectMessageModel directMessageModel : directMessageModels.data()) {
            if (directMessageModel.getSender().getId().equals(userModel.getId())) {
                directMessageModel.setReceiver(otherReadStatusModel.getUser()); // 예외 발생 지점
            } else {
                directMessageModel.setReceiver(userModel);
            }
        }

        return directMessageModels;

    }

    public CursorResponse<ConversationModel> getAllConversation(
        ConversationQueryRequest request,
        UUID userId
    ) {

        CursorResponse<ConversationModel> conversationModels = conversationQueryRepository
            .findAllConversation(request, userId);
        if (conversationModels.data().isEmpty()) {
            return conversationModels;
        }
        // 2. conversationIds 추출
        List<UUID> conversationIds = conversationModels.data().stream()
            .map(ConversationModel::getId)
            .toList();

        // 3. lastMessage 조회 (conversationId → DirectMessage)
        Map<UUID, DirectMessageModel> lastMessages = directMessageRepository
            .findLastMessagesByConversationIds(conversationIds);

        // 4. 상대방 ReadStatus 조회 (conversationId → ReadStatus)
        Map<UUID, ReadStatusModel> otherReadStatuses = readStatusRepository
            .findOthersByConversationIds(conversationIds, userId);

        // 5. 현재 사용자
        UserModel me = userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.withId(userId));

        // 6. 현재 사용자의 readStatus
        Map<UUID, ReadStatusModel> meReadStatuses = readStatusRepository
            .findMineByConversationIds(conversationIds, userId);

        // 6. ConversationModel 조립
        for (ConversationModel conversation : conversationModels.data()) {
            UUID conversationId = conversation.getId();

            ReadStatusModel otherReadStatus = otherReadStatuses.get(conversationId);
            DirectMessageModel lastMessage = lastMessages.get(conversationId);
            ReadStatusModel meReadStatus = meReadStatuses.get(conversationId);

            if (otherReadStatus != null) {
                conversation.withUser(otherReadStatus.getUser());
            }

            if (lastMessage != null) {
                // sender / receiver 보정
                if (lastMessage.getSender().getId().equals(me.getId())) {
                    if (otherReadStatus != null) {
                        lastMessage.setReceiver(otherReadStatus.getUser());
                    }
                } else {
                    lastMessage.setReceiver(me);
                }

                conversation.lastMessage(lastMessage);

                // hasUnread 계산
                if (!lastMessage.getSender().getId().equals(userId)) {
                    conversation.hasUnread(
                        meReadStatus != null &&
                            lastMessage.getCreatedAt().isAfter(meReadStatus.getLastRead())
                    );
                } else {
                    conversation.hasUnread(false);
                }

            } else {
                conversation.hasUnread(false);
            }
        }

        return conversationModels;

    }

    public void directMessageRead(
        DirectMessageModel directMessageModel,
        ReadStatusModel readStatusModel
    ) {
        if (directMessageModel != null) {
            readStatusModel.updateLastRead(Instant.now());
            readStatusRepository.save(readStatusModel);
        }

    }

    public DirectMessageModel getOtherDirectMessage(
        UUID conversationId,
        UUID directMessageId,
        UUID userId
    ) {

        return directMessageRepository.findOtherDirectMessage(conversationId, directMessageId,
            userId)
            .orElseThrow(() -> new DirectMessageNotFoundException(conversationId, directMessageId,
                userId));
    }

    public ConversationModel getConversationByWith(UUID userId, UUID withId) {

        UserModel withModel = userRepository.findById(withId)
            .orElseThrow(() -> UserNotFoundException.withId(withId));
        // message 포함
        ConversationModel conversationModel = conversationRepository.findByParticipants(userId,
            withId)
            .orElseThrow(() -> new ConversationNotFoundException(userId, withId));

        DirectMessageModel lastMessage = conversationModel.getLastMessage();

        ReadStatusModel readStatusModel = null;
        if (lastMessage != null) {
            readStatusModel = lastMessage.getSender().getId().equals(userId)
                ? readStatusRepository.findByConversationIdAndParticipantId(conversationModel
                    .getId(), withId)
                : readStatusRepository.findByConversationIdAndParticipantId(conversationModel
                    .getId(), userId);
        }

        if (lastMessage != null && !lastMessage.getSender().getId().equals(userId)) {
            conversationModel.hasUnread(
                lastMessage.getCreatedAt().isAfter(readStatusModel.getLastRead())
            );
        } else {
            conversationModel.hasUnread(false);
        }

        if (lastMessage != null) {
            DirectMessageModel directMessageModel = lastMessage.getSender().getId().equals(userId)
                ? conversationModel.getLastMessage().setReceiver(withModel)
                : conversationModel.getLastMessage()
                    .setReceiver(readStatusModel.getUser());
        }

        conversationModel.withUser(withModel);

        return conversationModel;
    }

    public ConversationModel getConversation(UUID conversationId, UUID userId) {
        if (!conversationQueryRepository.existsParticipant(conversationId, userId)) {
            throw new ConversationAccessDeniedException(conversationId, userId);
        }
        //lastmessage랑 같이 옴
        ConversationModel conversationModel = conversationRepository.find(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));
        DirectMessageModel lastMessage = conversationModel.getLastMessage();

        // 상대방 ReadStatus
        ReadStatusModel otherReadStatus = readStatusRepository.findOtherReadStatus(conversationId,
            userId);

        // 상대방 유저 세팅
        conversationModel.withUser(otherReadStatus.getUser());

        //receiver
        if (lastMessage != null) {
            DirectMessageModel directMessageModel = lastMessage.getSender().getId().equals(userId)
                ? conversationModel.getLastMessage()
                    .setReceiver(otherReadStatus.getUser())
                : conversationModel.getLastMessage().setReceiver(
                    userRepository.findById(userId)
                        .orElseThrow(() -> UserNotFoundException.withId(userId))
                );
        }
        // unread 계산
        if (lastMessage != null && !lastMessage.getSender().getId().equals(userId)) {

            ReadStatusModel myReadStatus = readStatusRepository
                .findByConversationIdAndParticipantId(conversationId, userId);

            conversationModel.hasUnread(
                lastMessage.getCreatedAt().isAfter(myReadStatus.getLastRead())

            );
        } else {
            conversationModel.hasUnread(false);
        }

        return conversationModel;
    }

    public DirectMessageModel getDirectMessageById(UUID directMessageId) {

        return directMessageRepository.findById(directMessageId)
            .orElseThrow(() -> new DirectMessageNotFoundException(directMessageId));
    }

    public List<ReadStatusModel> getReadStatusByConversationId(UUID conversationId) {
        return readStatusRepository.findByConversationId(conversationId);
    }

    public ReadStatusModel getReadStatusByConversationIdAndUserId(UUID conversationId,
        UUID userId) {
        return readStatusRepository.findByConversationIdAndUserId(conversationId, userId);
    }

    public ReadStatusModel getReadStatusById(UUID readStatusId) {
        return readStatusRepository.findById(readStatusId)
            .orElseThrow(() -> new ReadStatusNotFoundException(readStatusId));
    }

    public ReadStatusModel getReadStatusByConversationIdAndParticipantId(
        UUID conversationId,
        UUID participantId
    ) {
        return readStatusRepository
            .findByConversationIdAndParticipantId(conversationId, participantId);
    }

    public DirectMessageModel getLastDirectMessage(UUID conversationId, UUID senderId) {

        return directMessageRepository.findByConversationIdAndSenderId(conversationId, senderId);
    }
}
