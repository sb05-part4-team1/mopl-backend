package com.mopl.batch.collect.tmdb.service.content;

import com.mopl.batch.collect.tmdb.config.TmdbCollectPolicyResolver;
import com.mopl.batch.collect.tmdb.config.TmdbCollectProperties;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.external.tmdb.client.TmdbClient;
import com.mopl.external.tmdb.model.TmdbTvItem;
import com.mopl.external.tmdb.model.TmdbTvResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbPopularTvContentCollectService {

    private final TmdbClient tmdbClient;
    private final TmdbPopularContentUpsertTxService upsertService;
    private final TmdbCollectProperties collectProperties;
    private final TmdbCollectPolicyResolver policyResolver;

    private final AfterCommitExecutor afterCommitExecutor;
    private final ContentSearchSyncPort contentSearchSyncPort;

    public int collectPopularTvSeries() {
        int maxPage = policyResolver.maxPage(collectProperties.tvContent());
        List<ContentModel> inserted = new ArrayList<>();

        for (int page = 1; page <= maxPage; page++) {
            TmdbTvResponse response = tmdbClient.fetchPopularTvSeries(page);

            if (response == null || response.results() == null) {
                log.debug(
                    "TMDB tv results empty: page={}",
                    page
                );
                continue;
            }

            for (TmdbTvItem item : response.results()) {
                if (!isValid(item)) {
                    log.debug(
                        "TMDB invalid tv skipped: page={}, externalId={}",
                        page,
                        item == null ? null : item.id()
                    );
                    continue;
                }

                try {
                    ContentModel created = upsertService.upsertTv(item);
                    if (created != null) {
                        inserted.add(created);
                    }

                } catch (DataIntegrityViolationException e) {
                    log.debug(
                        "TMDB duplicate skipped: externalId={}",
                        item.id()
                    );

                } catch (RuntimeException e) {
                    log.warn(
                        "Failed to process TMDB tv: name={}, reason={}",
                        item.name(),
                        e.getMessage()
                    );
                }
            }
        }

        afterCommitExecutor.execute(() -> contentSearchSyncPort.upsertAll(inserted));

        log.info(
            "TMDB tv collect done. inserted={}",
            inserted.size()
        );

        return inserted.size();
    }

    private boolean isValid(TmdbTvItem item) {
        return item != null
            && item.name() != null && !item.name().isBlank()
            && item.poster_path() != null && !item.poster_path().isBlank()
            && item.overview() != null && !item.overview().isBlank();
    }
}
