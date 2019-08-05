package org.uniprot.api.taxonomy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

import org.uniprot.core.json.parser.taxonomy.TaxonomyJsonConfig;
import org.uniprot.core.taxonomy.TaxonomyEntry;
import org.uniprot.store.search.document.taxonomy.TaxonomyDocument;

@Slf4j
public class TaxonomyEntryConverter implements Function<TaxonomyDocument, TaxonomyEntry> {

    private final ObjectMapper objectMapper;

    public TaxonomyEntryConverter(){
        objectMapper = TaxonomyJsonConfig.getInstance().getFullObjectMapper();
    }

    @Override
    public TaxonomyEntry apply(TaxonomyDocument taxonomyDocument) {
        try {
            return objectMapper.readValue(taxonomyDocument.getTaxonomyObj().array(), TaxonomyEntry.class);
        } catch (Exception e) {
            log.info("Error converting solr binary to TaxonomyEntry: ", e);
        }
        return null;
    }

}