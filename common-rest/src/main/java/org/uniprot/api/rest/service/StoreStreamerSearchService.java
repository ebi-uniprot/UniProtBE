package org.uniprot.api.rest.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.uniprot.api.common.repository.search.QueryResult;
import org.uniprot.api.common.repository.search.SolrQueryConfig;
import org.uniprot.api.common.repository.search.SolrQueryRepository;
import org.uniprot.api.common.repository.search.SolrRequest;
import org.uniprot.api.common.repository.search.facet.Facet;
import org.uniprot.api.common.repository.search.facet.FacetConfig;
import org.uniprot.api.common.repository.search.facet.FacetTupleStreamConverter;
import org.uniprot.api.common.repository.search.facet.SolrStreamFacetResponse;
import org.uniprot.api.common.repository.search.page.impl.CursorPage;
import org.uniprot.api.common.repository.solrstream.FacetTupleStreamTemplate;
import org.uniprot.api.common.repository.solrstream.SolrStreamFacetRequest;
import org.uniprot.api.common.repository.stream.store.StoreStreamer;
import org.uniprot.api.rest.request.IdsSearchRequest;
import org.uniprot.api.rest.request.StreamRequest;
import org.uniprot.api.rest.search.AbstractSolrSortClause;
import org.uniprot.core.util.Utils;
import org.uniprot.store.config.UniProtDataType;
import org.uniprot.store.search.document.Document;

public abstract class StoreStreamerSearchService<D extends Document, R>
        extends BasicSearchService<D, R> {
    private final StoreStreamer<R> storeStreamer;
    private final FacetTupleStreamTemplate tupleStreamTemplate;
    private final FacetTupleStreamConverter tupleStreamConverter;
    private final SolrQueryConfig solrQueryConfig;

    public StoreStreamerSearchService(
            SolrQueryRepository<D> repository,
            FacetConfig facetConfig,
            AbstractSolrSortClause solrSortClause,
            StoreStreamer<R> storeStreamer,
            SolrQueryConfig solrQueryConfig,
            FacetTupleStreamTemplate tupleStreamTemplate) {

        this(
                repository,
                null,
                solrSortClause,
                facetConfig,
                storeStreamer,
                solrQueryConfig,
                tupleStreamTemplate);
    }

    public StoreStreamerSearchService(
            SolrQueryRepository<D> repository,
            Function<D, R> entryConverter,
            AbstractSolrSortClause solrSortClause,
            FacetConfig facetConfig,
            StoreStreamer<R> storeStreamer,
            SolrQueryConfig solrQueryConfig,
            FacetTupleStreamTemplate tupleStreamTemplate) {

        super(repository, entryConverter, solrSortClause, solrQueryConfig, facetConfig);
        this.storeStreamer = storeStreamer;
        this.solrQueryConfig = solrQueryConfig;
        this.tupleStreamTemplate = tupleStreamTemplate;
        this.tupleStreamConverter = new FacetTupleStreamConverter(getSolrIdField(), facetConfig);
    }

    public abstract R findByUniqueId(final String uniqueId, final String filters);

    @Override
    public Stream<R> stream(StreamRequest request) {
        SolrRequest query = createDownloadSolrRequest(request);
        return this.storeStreamer.idsToStoreStream(query);
    }

    public Stream<String> streamIds(StreamRequest request) {
        SolrRequest solrRequest = createDownloadSolrRequest(request);
        return this.storeStreamer.idsStream(solrRequest);
    }

    public QueryResult<R> getByIds(IdsSearchRequest idsRequest) {
        SolrStreamFacetResponse solrStreamResponse =
                solrStreamNeeded(idsRequest)
                        ? searchBySolrStream(idsRequest)
                        : new SolrStreamFacetResponse();

        // use the ids returned by solr stream if facetFilter is passed
        // otherwise use the passed ids
        List<String> ids =
                Utils.notNullNotEmpty(idsRequest.getFacetFilter())
                        ? solrStreamResponse.getIds()
                        : idsRequest.getIdList();
        // default page size to number of ids passed
        int pageSize = Objects.isNull(idsRequest.getSize()) ? ids.size() : idsRequest.getSize();

        // compute the cursor and get subset of ids as per cursor
        CursorPage cursorPage = CursorPage.of(idsRequest.getCursor(), pageSize, ids.size());

        List<String> idsInPage =
                ids.subList(
                        cursorPage.getOffset().intValue(), CursorPage.getNextOffset(cursorPage));

        // get n entries from store
        Stream<R> entries = this.storeStreamer.streamEntries(idsInPage);

        // facets may be set when facetList is passed but that should not be returned with cursor
        List<Facet> facets = solrStreamResponse.getFacets();
        if (Objects.nonNull(idsRequest.getCursor())) {
            facets = null; // do not return facet in case of next page and facetFilter
        }

        return QueryResult.of(entries, cursorPage, facets, null);
    }

    protected SolrRequest createDownloadSolrRequest(StreamRequest request) {
        return createSolrRequestBuilder(request, solrSortClause, queryBoosts).build();
    }

    protected abstract UniProtDataType getUniProtDataType();

    protected abstract String getSolrIdField();

    private SolrStreamFacetResponse searchBySolrStream(IdsSearchRequest idsRequest) {
        SolrStreamFacetRequest solrStreamRequest = createSolrStreamRequest(idsRequest);
        TupleStream tupleStream = this.tupleStreamTemplate.create(solrStreamRequest, facetConfig);
        return this.tupleStreamConverter.convert(tupleStream);
    }

    private SolrStreamFacetRequest createSolrStreamRequest(IdsSearchRequest idsRequest) {
        SolrStreamFacetRequest.SolrStreamFacetRequestBuilder solrRequestBuilder =
                SolrStreamFacetRequest.builder();

        // construct the query for tuple stream
        List<String> ids = idsRequest.getIdList();
        StringBuilder qb = new StringBuilder();
        qb.append("({!terms f=")
                .append(getSolrIdField())
                .append("}")
                .append(String.join(",", ids))
                .append(")");
        String termQuery = qb.toString();

        // append the facet filter query in the accession query
        if (Utils.notNullNotEmpty(idsRequest.getFacetFilter())) {
            solrRequestBuilder.query(idsRequest.getFacetFilter());
            solrRequestBuilder.filteredQuery(termQuery);
            solrRequestBuilder.searchAccession(Boolean.TRUE);
            solrRequestBuilder.searchSort(getSolrIdField() + " asc");
            solrRequestBuilder.searchFieldList(getSolrIdField());
        } else {
            solrRequestBuilder.query(termQuery);
        }

        List<String> facets = idsRequest.getFacetList();

        return solrRequestBuilder.queryConfig(this.solrQueryConfig).facets(facets).build();
    }

    private boolean solrStreamNeeded(IdsSearchRequest idsRequest) {
        return (Utils.nullOrEmpty(idsRequest.getCursor())
                        && Utils.notNullNotEmpty(idsRequest.getFacetList())
                        && !idsRequest.isDownload())
                || Utils.notNullNotEmpty(idsRequest.getFacetFilter());
    }
}
