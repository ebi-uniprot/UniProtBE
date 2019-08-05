package org.uniprot.api.uniparc.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.uniprot.api.configure.uniparc.UniParcResultFields;
import org.uniprot.api.rest.request.SearchRequest;
import org.uniprot.api.rest.validation.ValidFacets;
import org.uniprot.api.rest.validation.ValidReturnFields;
import org.uniprot.api.rest.validation.ValidSolrQueryFields;
import org.uniprot.api.rest.validation.ValidSolrQuerySyntax;
import org.uniprot.api.rest.validation.ValidSolrSortFields;
import org.uniprot.api.uniparc.repository.UniParcFacetConfig;
import org.uniprot.store.search.field.UniParcField;

import com.google.common.base.Strings;

import lombok.Data;

/**
 *
 * @author jluo
 * @date: 20 Jun 2019
 *
*/
@Data
public class UniParcRequest implements SearchRequest {
	 private static final int DEFAULT_RESULTS_SIZE = 25;

	    @NotNull(message = "{search.required}")
	    @ValidSolrQuerySyntax(message = "{search.invalid.query}")
     @ValidSolrQueryFields(fieldValidatorClazz = UniParcField.Search.class, messagePrefix = "search.uniparc")
	    private String query;

	    @ValidSolrSortFields(sortFieldEnumClazz = UniParcField.Sort.class)
	    private String sort;

	    private String cursor;
	    
	    @ValidReturnFields(fieldValidatorClazz = UniParcResultFields.class)
	    private String fields;

	    @ValidFacets(facetConfig = UniParcFacetConfig.class)
	    private String facets;

	    @Positive(message = "{search.positive}")
	    private int size = DEFAULT_RESULTS_SIZE;

	    public static final String DEFAULT_FIELDS="upi,organism,accession,first_seen,last_seen,length";
	    @Override
	    public String getFields() {
	    	if(Strings.isNullOrEmpty(fields)) {
	    		fields =DEFAULT_FIELDS;
	    	}else if(!fields.contains(UniParcField.Return.upi.name())) {
	    		String temp = "upi,"+fields;
	    		this.fields= temp;
	    	}
	    	return fields;
	    }
}
