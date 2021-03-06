package org.uniprot.api.common.repository.search;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.*;

/**
 * Created 04/09/19
 *
 * @author Edd
 */
@Builder
@Getter
@ToString
public class SolrQueryConfig {
    @Singular private final List<String> defaultSearchBoosts;
    private final String defaultSearchBoostFunctions;
    @Singular private final List<String> advancedSearchBoosts;
    private final String advancedSearchBoostFunctions;

    @Setter(AccessLevel.NONE)
    private String queryFields;

    @Setter(AccessLevel.NONE)
    private Set<String> stopWords;

    @Setter(AccessLevel.NONE)
    private String highlightFields;

    public static class SolrQueryConfigBuilder {
        public SolrQueryConfigBuilder queryFields(String queryFields) {
            this.queryFields =
                    Arrays.stream(queryFields.split(","))
                            .map(String::trim)
                            .collect(Collectors.joining(" "));
            return this;
        }

        public SolrQueryConfigBuilder stopWords(String stopWords) {
            this.stopWords =
                    Arrays.stream(stopWords.split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet());
            return this;
        }
    }
}
