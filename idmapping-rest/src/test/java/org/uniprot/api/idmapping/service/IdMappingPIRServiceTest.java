package org.uniprot.api.idmapping.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.uniprot.api.common.repository.search.QueryResult;
import org.uniprot.api.idmapping.controller.request.IdMappingJobRequest;
import org.uniprot.api.idmapping.controller.request.IdMappingPageRequest;
import org.uniprot.api.idmapping.model.IdMappingResult;
import org.uniprot.api.idmapping.model.IdMappingStringPair;

class IdMappingPIRServiceTest {
    private static FakeIdMappingPIRService pirService;

    @BeforeAll
    static void setUp() {
        pirService = new FakeIdMappingPIRService(5);
    }

    @Test
    void queryPageSuccessfully() {
        // given
        IdMappingPageRequest pageRequest = new IdMappingPageRequest();
        int pageSize = 3;
        pageRequest.setSize(pageSize);

        List<IdMappingStringPair> mappingPairs = createMappingPairs(10);
        List<String> unmappedIds = createUnmappedIds(5);
        IdMappingResult mappingResult =
                IdMappingResult.builder().mappedIds(mappingPairs).unmappedIds(unmappedIds).build();

        // when
        QueryResult<IdMappingStringPair> queryResult =
                pirService.queryResultPage(pageRequest, mappingResult);

        // then
        assertThat(
                queryResult.getContent().collect(Collectors.toList()),
                is(mappingPairs.subList(0, pageSize)));
        assertThat(queryResult.getFailedIds(), is(unmappedIds));
    }

    @Test
    void queryAllResultsSuccessfully() { // given
        List<IdMappingStringPair> mappingPairs = createMappingPairs(10);
        List<String> unmappedIds = createUnmappedIds(5);
        IdMappingResult mappingResult =
                IdMappingResult.builder().mappedIds(mappingPairs).unmappedIds(unmappedIds).build();

        // when
        QueryResult<IdMappingStringPair> queryResult = pirService.queryResultAll(mappingResult);

        // then
        assertThat(queryResult.getContent().collect(Collectors.toList()), is(mappingPairs));
        assertThat(queryResult.getFailedIds(), is(unmappedIds));
    }

    private List<IdMappingStringPair> createMappingPairs(int count) {
        return IntStream.range(1, count)
                .mapToObj(i -> new IdMappingStringPair("from " + i, "to " + i))
                .collect(Collectors.toList());
    }

    private List<String> createUnmappedIds(int count) {
        return IntStream.range(1, count)
                .mapToObj(i -> "unmapped " + i)
                .collect(Collectors.toList());
    }

    static class FakeIdMappingPIRService extends IdMappingPIRService {
        public FakeIdMappingPIRService(int defaultPageSize) {
            super(defaultPageSize);
        }

        @Override
        public IdMappingResult mapIds(IdMappingJobRequest request) {
            return null;
        }
    }
}
