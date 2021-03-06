package org.uniprot.api.support.data.taxonomy.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.uniprot.api.common.repository.search.SolrQueryConfig;
import org.uniprot.api.common.repository.search.SolrRequest;
import org.uniprot.api.common.repository.stream.rdf.RDFStreamer;
import org.uniprot.api.rest.request.BasicRequest;
import org.uniprot.api.rest.search.AbstractSolrSortClause;
import org.uniprot.api.rest.service.BasicSearchService;
import org.uniprot.api.rest.service.query.QueryProcessor;
import org.uniprot.api.support.data.taxonomy.repository.TaxonomyRepository;
import org.uniprot.api.support.data.taxonomy.request.GetByTaxonIdsRequest;
import org.uniprot.api.support.data.taxonomy.request.TaxonomyFacetConfig;
import org.uniprot.api.support.data.taxonomy.request.TaxonomySolrQueryConfig;
import org.uniprot.api.support.data.taxonomy.request.TaxonomySortClause;
import org.uniprot.api.support.data.taxonomy.response.TaxonomyEntryConverter;
import org.uniprot.core.taxonomy.TaxonomyEntry;
import org.uniprot.store.config.searchfield.common.SearchFieldConfig;
import org.uniprot.store.config.searchfield.model.SearchFieldItem;
import org.uniprot.store.search.document.taxonomy.TaxonomyDocument;

@Service
@Import(TaxonomySolrQueryConfig.class)
public class TaxonomyService extends BasicSearchService<TaxonomyDocument, TaxonomyEntry> {
    public static final String TAXONOMY_ID_FIELD = "id";
    private static final String ACTIVE_FIELD = "active";
    private final SearchFieldConfig searchFieldConfig;
    private final QueryProcessor queryProcessor;
    private final RDFStreamer rdfStreamer;

    public TaxonomyService(
            TaxonomyRepository repository,
            TaxonomyFacetConfig facetConfig,
            TaxonomyEntryConverter converter,
            TaxonomySortClause taxonomySortClause,
            SolrQueryConfig taxonomySolrQueryConf,
            QueryProcessor taxonomyQueryProcessor,
            SearchFieldConfig taxonomySearchFieldConfig,
            @Qualifier("taxonomyRDFStreamer") RDFStreamer rdfStreamer) {

        super(repository, converter, taxonomySortClause, taxonomySolrQueryConf, facetConfig);
        this.searchFieldConfig = taxonomySearchFieldConfig;
        this.queryProcessor = taxonomyQueryProcessor;
        this.rdfStreamer = rdfStreamer;
    }

    @Override
    protected SolrRequest.SolrRequestBuilder createSolrRequestBuilder(
            BasicRequest request,
            AbstractSolrSortClause solrSortClause,
            SolrQueryConfig queryBoosts) {
        SolrRequest.SolrRequestBuilder builder =
                super.createSolrRequestBuilder(request, solrSortClause, queryBoosts);

        if (!(request instanceof GetByTaxonIdsRequest)) {
            // We do not add active filter query for get by Taxon Ids
            // Because this endpoint need to return excluded taxon ids
            builder.filterQuery(getActiveTaxonomyFilterQuery());
        }
        return builder;
    }

    private String getActiveTaxonomyFilterQuery() {
        return ACTIVE_FIELD + ":true";
    }

    public TaxonomyEntry findById(final long taxId) {
        return findByUniqueId(String.valueOf(taxId));
    }

    @Override
    protected SearchFieldItem getIdField() {
        return this.searchFieldConfig.getSearchFieldItemByName(TAXONOMY_ID_FIELD);
    }

    @Override
    protected QueryProcessor getQueryProcessor() {
        return queryProcessor;
    }

    @Override
    protected RDFStreamer getRDFStreamer() {
        return this.rdfStreamer;
    }
}
