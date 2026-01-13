package com.mopl.domain.repository.conversation;

public record ConversationQueryRequest(




) {

}



//public record UserQueryRequest(
//        String emailLike,
//        UserModel.Role roleEqual,
//        Boolean isLocked,
//        String cursor,
//        UUID idAfter,
//        Integer limit,
//        SortDirection sortDirection,
//        UserSortField sortBy
//) implements CursorRequest<UserSortField> {
//
//    private static final int DEFAULT_LIMIT = 100;
//    private static final int MAX_LIMIT = 1000;
//
//    public UserQueryRequest {
//        limit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
//        sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
//        sortBy = sortBy != null ? sortBy : UserSortField.name;
//    }
//}
