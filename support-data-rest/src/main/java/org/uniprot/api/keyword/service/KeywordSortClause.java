package org.uniprot.api.keyword.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.uniprot.api.rest.search.AbstractSolrSortClause;
import org.uniprot.store.config.searchfield.common.SearchFieldConfig;
import org.uniprot.store.config.searchfield.factory.SearchFieldConfigFactory;
import org.uniprot.store.config.searchfield.factory.UniProtDataType;

/** @author lgonzales */
@Component
public class KeywordSortClause extends AbstractSolrSortClause {
    private SearchFieldConfig searchFieldConfig =
            SearchFieldConfigFactory.getSearchFieldConfig(UniProtDataType.keyword);

    @Override
    protected Sort createDefaultSort(boolean hasScore) {
        return new Sort(Sort.Direction.DESC, "score")
                .and(
                        new Sort(
                                Sort.Direction.ASC,
                                searchFieldConfig
                                        .getSearchFieldItemByName("keyword_id")
                                        .getFieldName()))
                .and(
                        new Sort(
                                Sort.Direction.ASC,
                                searchFieldConfig.getSearchFieldItemByName("id").getFieldName()));
    }

    @Override
    protected String getSolrDocumentIdFieldName() {
        return searchFieldConfig.getSearchFieldItemByName("id").getFieldName();
    }

    @Override
    protected String getSolrSortFieldName(String name) {
        return searchFieldConfig.getCorrespondingSortField(name).getFieldName();
    }
}
