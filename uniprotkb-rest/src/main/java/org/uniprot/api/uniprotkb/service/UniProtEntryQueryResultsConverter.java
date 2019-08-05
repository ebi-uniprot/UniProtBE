package org.uniprot.api.uniprotkb.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniprot.api.common.repository.search.QueryResult;
import org.uniprot.api.uniprotkb.controller.request.FieldsParser;
import org.uniprot.api.uniprotkb.repository.store.UniProtKBStoreClient;
import org.uniprot.core.builder.SequenceBuilder;
import org.uniprot.core.json.parser.uniprot.UniprotJsonConfig;
import org.uniprot.core.uniprot.EntryInactiveReason;
import org.uniprot.core.uniprot.InactiveReasonType;
import org.uniprot.core.uniprot.UniProtAccession;
import org.uniprot.core.uniprot.UniProtEntry;
import org.uniprot.core.uniprot.UniProtId;
import org.uniprot.core.uniprot.builder.EntryInactiveReasonBuilder;
import org.uniprot.core.uniprot.builder.UniProtAccessionBuilder;
import org.uniprot.core.uniprot.builder.UniProtEntryBuilder;
import org.uniprot.core.uniprot.builder.UniProtIdBuilder;
import org.uniprot.store.search.document.uniprot.UniProtDocument;
import org.uniprot.store.search.field.UniProtField;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.jodah.failsafe.RetryPolicy;

/**
 * The purpose of this class is to simplify conversion of {@code QueryResult<UniProtDocument>} instances to
 * {@code QueryResult<UniProtEntry>} or {@code Optional<UniProtEntry>}. It is used in {@link UniProtEntryService}.
 *
 * Note, this class will be replaced once we finalise a core domain model, which is the single entity returned
 * from by requests -- and on which all message converters will operate.
 *
 * Created 18/10/18
 *
 * @author Edd
 */
class UniProtEntryQueryResultsConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniProtEntryQueryResultsConverter.class);

    private final UniProtKBStoreClient entryStore;
    private final RetryPolicy retryPolicy = new RetryPolicy()
            .retryOn(IOException.class)
            .withDelay(500,TimeUnit.MILLISECONDS)
            .withMaxRetries(5);

    UniProtEntryQueryResultsConverter(UniProtKBStoreClient entryStore) {
        this.entryStore = entryStore;
    }

    QueryResult<UniProtEntry> convertQueryResult(QueryResult<UniProtDocument> results, Map<String, List<String>> filters) {
        List<UniProtEntry> upEntries = results.getContent()
                .stream().map(doc -> convertDoc(doc, filters))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return QueryResult.of(upEntries, results.getPage(), results.getFacets(), results.getMatchedFields());
    }

    Optional<UniProtEntry> convertDoc(UniProtDocument doc, Map<String, List<String>> filters) {
        if (doc.active) {
            return getEntryFromStore(doc, filters);
        } else {
            return getInactiveUniProtEntry(doc);
        }
    }

    private Optional<UniProtEntry> getInactiveUniProtEntry(UniProtDocument doc) {
        UniProtAccession accession = new UniProtAccessionBuilder(doc.accession).build();
        List<String> mergeDemergeList = new ArrayList<>();

        String[] reasonItems =  doc.inactiveReason.split(":");
        InactiveReasonType type = InactiveReasonType.valueOf(reasonItems[0].toUpperCase());
        if(reasonItems.length > 1){
            mergeDemergeList.addAll(Arrays.asList(reasonItems[1].split(",")));
        }

        UniProtId uniProtId = new UniProtIdBuilder(doc.id).build();
        EntryInactiveReason inactiveReason = new EntryInactiveReasonBuilder()
                .type(type)
                .mergeDemergeTo(mergeDemergeList)
                .build();

        UniProtEntryBuilder.InactiveEntryBuilder entryBuilder = new UniProtEntryBuilder()
                .primaryAccession(accession)
                .uniProtId(uniProtId)
                .inactive()
                .inactiveReason(inactiveReason);
        return Optional.of(entryBuilder.build());
    }

    private Optional<UniProtEntry> getEntryFromStore(UniProtDocument doc, Map<String, List<String>> filters) {
        if (FieldsParser.isDefaultFilters(filters) && (doc.avro_binary != null)) {
            UniProtEntry uniProtEntry = null;

            try {
                byte[] decodeEntry = Base64.getDecoder().decode(doc.avro_binary);
                ObjectMapper jsonMapper = UniprotJsonConfig.getInstance().getFullObjectMapper();
                uniProtEntry = jsonMapper.readValue(decodeEntry, UniProtEntry.class);
            }catch (IOException e){
                LOGGER.info("Error converting solr avro_binary default UniProtEntry",e);
            }
            if (Objects.isNull(uniProtEntry)) {
                return Optional.empty();
            }
            if (filters.containsKey(UniProtField.ResultFields.mass.name())
                    || filters.containsKey(UniProtField.ResultFields.length.name())) {
                char[] fakeSeqArrayWithCorrectLength = new char[doc.seqLength];
                Arrays.fill(fakeSeqArrayWithCorrectLength, 'X');
                SequenceBuilder seq = new SequenceBuilder(new String(fakeSeqArrayWithCorrectLength));
                //seq.molWeight(doc.seqMass); //TODO: TRM-22339 assigned to Jie
                UniProtEntryBuilder.ActiveEntryBuilder entryBuilder = new UniProtEntryBuilder().from(uniProtEntry);
                entryBuilder.sequence(seq.build());
                uniProtEntry = entryBuilder.build();
            }
            return Optional.of(uniProtEntry);
        } else {
            return  entryStore.getEntry(doc.accession);                     
                          
        }
    }
}