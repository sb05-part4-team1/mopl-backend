package com.mopl.external.tsdb.client;

import com.mopl.external.ExternalApiMetrics;
import com.mopl.external.tsdb.exception.TsdbImageDownloadException;
import com.mopl.external.tsdb.model.EventResponse;
import com.mopl.external.tsdb.model.LeagueResponse;
import com.mopl.external.tsdb.properties.TsdbProperties;
import com.mopl.logging.context.LogContext;
import io.micrometer.core.instrument.Timer;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TsdbClient {

    private static final String API_NAME = "tsdb";

    private final WebClient tsdbWebClient;
    private final TsdbProperties props;
    private final ExternalApiMetrics metrics;

    public TsdbClient(
        WebClient tsdbWebClient,
        TsdbProperties props,
        @Nullable ExternalApiMetrics metrics
    ) {
        this.tsdbWebClient = tsdbWebClient;
        this.props = props;
        this.metrics = metrics;
    }

    public LeagueResponse fetchAllLeagues() {
        String endpoint = "allLeagues";
        LogContext.with("api", API_NAME).and("endpoint", endpoint).debug("Fetching");
        Timer.Sample sample = startTimer();
        try {
            LeagueResponse response = tsdbWebClient.get()
                .uri("/all_leagues.php")
                .retrieve()
                .bodyToMono(LeagueResponse.class)
                .block();
            recordSuccess(sample, endpoint);
            return response;
        } catch (Exception e) {
            recordError(sample, endpoint, e.getClass().getSimpleName());
            throw e;
        }
    }

    public EventResponse fetchNextLeagueEvent(Long leagueId) {
        String endpoint = "nextLeagueEvent";
        LogContext.with("api", API_NAME).and("endpoint", endpoint).and("leagueId", leagueId).debug("Fetching");
        Timer.Sample sample = startTimer();
        try {
            EventResponse response = tsdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/eventsnextleague.php")
                    .queryParam("id", leagueId)
                    .build()
                )
                .retrieve()
                .bodyToMono(EventResponse.class)
                .block();
            recordSuccess(sample, endpoint);
            return response;
        } catch (Exception e) {
            recordError(sample, endpoint, e.getClass().getSimpleName());
            throw e;
        }
    }

    public EventResponse fetchPastLeagueEvent(Long leagueId) {
        String endpoint = "pastLeagueEvent";
        LogContext.with("api", API_NAME).and("endpoint", endpoint).and("leagueId", leagueId).debug("Fetching");
        Timer.Sample sample = startTimer();
        try {
            EventResponse response = tsdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/eventspastleague.php")
                    .queryParam("id", leagueId)
                    .build()
                )
                .retrieve()
                .bodyToMono(EventResponse.class)
                .block();
            recordSuccess(sample, endpoint);
            return response;
        } catch (Exception e) {
            recordError(sample, endpoint, e.getClass().getSimpleName());
            throw e;
        }
    }

    public Resource downloadImage(String fullImageUrl) {
        if (fullImageUrl == null || fullImageUrl.isBlank()) {
            return null;
        }

        Timer.Sample sample = startTimer();
        try {
            Resource resource = tsdbWebClient.get()
                .uri(fullImageUrl + "/" + props.getImage().getDefaultSize())
                .retrieve()
                .bodyToMono(Resource.class)
                .block();
            recordSuccess(sample, "downloadImage");
            return resource;
        } catch (RuntimeException e) {
            recordError(sample, "downloadImage", e.getClass().getSimpleName());
            throw new TsdbImageDownloadException(fullImageUrl, e);
        }
    }

    private Timer.Sample startTimer() {
        return metrics != null ? metrics.startTimer() : null;
    }

    private void recordSuccess(Timer.Sample sample, String endpoint) {
        if (metrics != null && sample != null) {
            metrics.recordSuccess(sample, API_NAME, endpoint);
        }
    }

    private void recordError(Timer.Sample sample, String endpoint, String errorType) {
        if (metrics != null && sample != null) {
            metrics.recordError(sample, API_NAME, endpoint, errorType);
        }
    }
}
