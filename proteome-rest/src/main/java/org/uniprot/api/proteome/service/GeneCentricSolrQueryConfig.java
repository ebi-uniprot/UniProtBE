package org.uniprot.api.proteome.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.uniprot.api.common.repository.search.SolrQueryConfig;
import org.uniprot.api.common.repository.search.SolrQueryConfigFileReader;
import org.uniprot.api.rest.service.query.QueryProcessor;
import org.uniprot.api.rest.service.query.UniProtQueryProcessor;
import org.uniprot.api.rest.service.query.processor.UniProtQueryProcessorConfig;
import org.uniprot.api.rest.validation.config.WhitelistFieldConfig;
import org.uniprot.store.config.UniProtDataType;
import org.uniprot.store.config.searchfield.common.SearchFieldConfig;
import org.uniprot.store.config.searchfield.factory.SearchFieldConfigFactory;
import org.uniprot.store.config.searchfield.model.SearchFieldItem;

@Configuration
public class GeneCentricSolrQueryConfig {
    private static final String RESOURCE_LOCATION = "/genecentric-query.config";

    @Bean
    public SolrQueryConfig geneCentricSolrQueryConf() {
        return new SolrQueryConfigFileReader(RESOURCE_LOCATION).getConfig();
    }

    @Bean
    public SearchFieldConfig geneCentricSearchFieldConfig() {
        return SearchFieldConfigFactory.getSearchFieldConfig(UniProtDataType.GENECENTRIC);
    }

    @Bean
    public QueryProcessor geneCentricQueryProcessor(
            WhitelistFieldConfig whiteListFieldConfig,
            SearchFieldConfig geneCentricSearchFieldConfig) {
        Map<String, String> geneCentricWhitelistFields =
                whiteListFieldConfig
                        .getField()
                        .getOrDefault(
                                UniProtDataType.GENECENTRIC.toString().toLowerCase(),
                                new HashMap<>());
        return UniProtQueryProcessor.newInstance(
                UniProtQueryProcessorConfig.builder()
                        .optimisableFields(
                                getDefaultSearchOptimisedFieldItems(geneCentricSearchFieldConfig))
                        .whiteListFields(geneCentricWhitelistFields)
                        .build());
    }

    private List<SearchFieldItem> getDefaultSearchOptimisedFieldItems(
            SearchFieldConfig geneCentricSearchFieldConfig) {
        return Collections.singletonList(
                geneCentricSearchFieldConfig.getSearchFieldItemByName(
                        GeneCentricService.GENECENTRIC_ID_FIELD));
    }
}
