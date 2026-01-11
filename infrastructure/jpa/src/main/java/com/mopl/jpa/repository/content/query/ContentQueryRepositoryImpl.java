package com.mopl.jpa.repository.content.query;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mopl.jpa.entity.content.QContentEntity.contentEntity;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class ContentQueryRepositoryImpl implements ContentQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ContentEntityMapper contentEntityMapper;
    private final ContentTagRepository contentTagRepository;

    @Override
    public CursorResponse<ContentModel> findAll(ContentQueryRequest request) {

        ContentSortFieldJpa sortFieldJpa = ContentSortFieldJpa.from(request.sortBy());

        // 1️⃣ 콘텐츠 엔티티 커서 페이지네이션 조회
        JPAQuery<ContentEntity> query = queryFactory
            .selectFrom(contentEntity)
            .where(
                typeEqual(request.typeEqual()),
                keywordLike(request.keywordLike())
            );

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            query,
            contentEntity.id
        );

        List<ContentEntity> entities = query.fetch();
        long totalCount = countTotal(request);

        if (entities.isEmpty()) {
            return CursorResponse.empty(
                sortFieldJpa.getFieldName(),
                request.sortDirection()
            );
        }

        // 2️⃣ Entity → Model 변환
        List<ContentModel> models = entities.stream()
            .map(contentEntityMapper::toModel)
            .collect(Collectors.toList());

        // 3️⃣ 태그 배치 조회 후 매핑
        attachTags(models);

        // 4️⃣ id → model 매핑 (CursorResponse용)
        Map<UUID, ContentModel> modelById = models.stream()
            .collect(Collectors.toMap(ContentModel::getId, Function.identity()));

        // 5️⃣ CursorResponse 생성
        return CursorPaginationHelper.buildResponse(
            entities,
            request,
            sortFieldJpa,
            totalCount,
            entity -> modelById.get(entity.getId()),
            sortFieldJpa::extractValue,
            ContentEntity::getId
        );
    }

    private long countTotal(ContentQueryRequest request) {
        Long total = queryFactory
            .select(contentEntity.count())
            .from(contentEntity)
            .where(
                typeEqual(request.typeEqual()),
                keywordLike(request.keywordLike())
            )
            .fetchOne();

        return total != null ? total : 0;
    }

    private BooleanExpression typeEqual(ContentModel.ContentType type) {
        return type != null ? contentEntity.type.eq(type) : null;
    }

    private BooleanExpression keywordLike(String keyword) {
        return hasText(keyword)
            ? contentEntity.title.containsIgnoreCase(keyword)
                .or(contentEntity.description.containsIgnoreCase(keyword))
            : null;
    }

    /**
     * 태그 배치 조회 후 ContentModel에 매핑
     */
    private void attachTags(List<ContentModel> contents) {

        List<UUID> contentIds = contents.stream()
            .map(ContentModel::getId)
            .toList();

        if (contentIds.isEmpty()) {
            return;
        }

        Map<UUID, List<TagModel>> tagsByContentId = contentTagRepository.findTagsByContentIds(
            contentIds);

        contents.replaceAll(content -> content.withTags(
            tagsByContentId
                .getOrDefault(content.getId(), List.of())
                .stream()
                .map(TagModel::getName)
                .toList()
        )
        );
    }
}
