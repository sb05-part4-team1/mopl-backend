package com.mopl.external.tsdb.client;

import com.mopl.external.tsdb.exception.TsdbImageDownloadException;
import com.mopl.external.tsdb.model.EventResponse;
import com.mopl.external.tsdb.model.LeagueResponse;
import com.mopl.external.tsdb.properties.TsdbProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class TsdbClient {

    private final WebClient tsdbWebClient;
    private final TsdbProperties props;

    public LeagueResponse fetchAllLeagues() {
        return tsdbWebClient.get()
            .uri("/all_leagues.php")
            .retrieve()
            .bodyToMono(LeagueResponse.class)
            .block();
    }

    public EventResponse fetchNextLeagueEvent(Long leagueId) {
        return tsdbWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/eventsnextleague.php")
                .queryParam("id", leagueId)
                .build()
            )
            .retrieve()
            .bodyToMono(EventResponse.class)
            .block();
    }

    public EventResponse fetchPastLeagueEvent(Long leagueId) {
        return tsdbWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/eventspastleague.php")
                .queryParam("id", leagueId)
                .build()
            )
            .retrieve()
            .bodyToMono(EventResponse.class)
            .block();
    }

    public Resource downloadImage(String fullImageUrl) {
        if (fullImageUrl == null || fullImageUrl.isBlank()) {
            return null;
        }

        try {
            return tsdbWebClient.get()
                .uri(fullImageUrl + "/" + props.getImage().getDefaultSize())
                .retrieve()
                .bodyToMono(Resource.class)
                .block();

        } catch (RuntimeException e) {
            throw new TsdbImageDownloadException(fullImageUrl, e);
        }
    }
}
