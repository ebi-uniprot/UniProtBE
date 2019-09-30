package org.uniprot.api.uniprotkb.repository.store;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Base64;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.uniprot.api.common.repository.store.StoreStreamer;
import org.uniprot.api.common.repository.store.TupleStreamTemplate;
import org.uniprot.api.rest.respository.RepositoryConfig;
import org.uniprot.api.rest.respository.RepositoryConfigProperties;
import org.uniprot.core.json.parser.uniprot.UniprotJsonConfig;
import org.uniprot.core.uniprot.UniProtEntry;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created 21/08/18
 *
 * @author Edd
 */
@Configuration
@Import(RepositoryConfig.class)
public class ResultsConfig {
    private static final Logger LOGGER = getLogger(ResultsConfig.class);

    @Bean
    public TupleStreamTemplate cloudSolrStreamTemplate(
            RepositoryConfigProperties configProperties, HttpClient httpClient) {
        return TupleStreamTemplate.builder()
                .collection("uniprot")
                .key("accession_id")
                .requestHandler("/export")
                .zookeeperHost(configProperties.getZkHost())
                .httpClient(httpClient)
                .build();
    }

    @Bean
    public StoreStreamer<UniProtEntry> uniProtEntryStoreStreamer(
            UniProtKBStoreClient uniProtClient, TupleStreamTemplate tupleStreamTemplate) {
        return StoreStreamer.<UniProtEntry>builder()
                .id(resultsConfigProperties().getUniprot().getValueId())
                .defaultsField(resultsConfigProperties().getUniprot().getDefaultsField())
                .streamerBatchSize(resultsConfigProperties().getUniprot().getBatchSize())
                .storeClient(uniProtClient)
                .defaultsConverter(this::convertDefaultAvroToUniProtEntry)
                .tupleStreamTemplate(tupleStreamTemplate)
                .build();
    }

    @Bean
    public StreamerConfigProperties resultsConfigProperties() {
        return new StreamerConfigProperties();
    }

    private UniProtEntry convertDefaultAvroToUniProtEntry(String s) {
        UniProtEntry result = null;
        try {
            ObjectMapper jsonMapper = UniprotJsonConfig.getInstance().getFullObjectMapper();
            result = jsonMapper.readValue(Base64.getDecoder().decode(s), UniProtEntry.class);
        } catch (IOException e) {
            LOGGER.error("Error converting DefaultAvro to UniProtEntry", e);
        }
        return result;
    }
}
