package org.uniprot.api.literature.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

import org.uniprot.api.literature.repository.LiteratureFacetConfig;
import org.uniprot.api.rest.request.SearchRequest;
import org.uniprot.api.rest.validation.*;
import org.uniprot.store.config.UniProtDataType;

/**
 * @author lgonzales
 * @since 2019-07-04
 */
@Data
public class LiteratureRequest implements SearchRequest {

    @NotNull(message = "{search.required}")
    @ValidSolrQuerySyntax(message = "{search.invalid.query}")
    @ValidSolrQueryFields(
            uniProtDataType = UniProtDataType.LITERATURE,
            messagePrefix = "search.literature")
    private String query;

    @ValidSolrSortFields(uniProtDataType = UniProtDataType.LITERATURE)
    private String sort;

    private String cursor;

    @ValidReturnFields(uniProtDataType = UniProtDataType.LITERATURE)
    private String fields;

    @Positive(message = "{search.positive}")
    private Integer size;

    @ValidFacets(facetConfig = LiteratureFacetConfig.class)
    private String facets;
}