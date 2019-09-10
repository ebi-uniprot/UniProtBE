package org.uniprot.api.uniref.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.uniprot.api.rest.controller.AbstractSearchControllerIT;
import org.uniprot.api.rest.controller.SaveScenario;
import org.uniprot.api.rest.controller.param.ContentTypeParam;
import org.uniprot.api.rest.controller.param.SearchContentTypeParam;
import org.uniprot.api.rest.controller.param.SearchParameter;
import org.uniprot.api.rest.controller.param.resolver.AbstractSearchContentTypeParamResolver;
import org.uniprot.api.rest.controller.param.resolver.AbstractSearchParameterResolver;
import org.uniprot.api.rest.output.UniProtMediaType;
import org.uniprot.api.rest.validation.error.ErrorHandlerConfig;
import org.uniprot.api.uniref.UniRefRestApplication;
import org.uniprot.api.uniref.repository.DataStoreTestConfig;
import org.uniprot.api.uniref.repository.UniRefFacetConfig;
import org.uniprot.core.Sequence;
import org.uniprot.core.builder.SequenceBuilder;
import org.uniprot.core.uniparc.builder.UniParcIdBuilder;
import org.uniprot.core.uniprot.builder.UniProtAccessionBuilder;
import org.uniprot.core.uniref.GoTermType;
import org.uniprot.core.uniref.RepresentativeMember;
import org.uniprot.core.uniref.UniRefEntry;
import org.uniprot.core.uniref.UniRefEntryId;
import org.uniprot.core.uniref.UniRefMember;
import org.uniprot.core.uniref.UniRefMemberIdType;
import org.uniprot.core.uniref.UniRefType;
import org.uniprot.core.uniref.builder.GoTermBuilder;
import org.uniprot.core.uniref.builder.RepresentativeMemberBuilder;
import org.uniprot.core.uniref.builder.UniRefEntryBuilder;
import org.uniprot.core.uniref.builder.UniRefEntryIdBuilder;
import org.uniprot.core.uniref.builder.UniRefMemberBuilder;
import org.uniprot.core.xml.jaxb.uniref.Entry;
import org.uniprot.core.xml.uniref.UniRefEntryConverter;
import org.uniprot.store.indexer.DataStoreManager;
import org.uniprot.store.search.field.SearchField;
import org.uniprot.store.search.field.UniRefField;
import org.uniprot.store.search.field.UniRefResultFields;

import com.beust.jcommander.internal.Lists;

/**
 *
 * @author jluo
 * @date: 27 Aug 2019
 *
*/
@ContextConfiguration(classes= {DataStoreTestConfig.class, UniRefRestApplication.class, ErrorHandlerConfig.class})

@ActiveProfiles(profiles = "offline")
@WebMvcTest(UniRefController.class)
@ExtendWith(value = {SpringExtension.class, UniRefSearchControllerIT.UniRefSearchContentTypeParamResolver.class,
		UniRefSearchControllerIT.UniRefSearchParameterResolver.class})

public class UniRefSearchControllerIT extends AbstractSearchControllerIT {
	private static final String ID_PREF = "UniRef50_P039";
	 private static final String NAME_PREF= "Cluster: MoeK5 ";
	 private static final String ACC_PREF ="P123";
	 private static final String ACC_2_PREF ="P123";
	 private static final String UPI_PREF = "UPI0000083A";
	    @Autowired
	    private MockMvc mockMvc;
	    @Autowired
	    SolrClient solrClient;
	    @Autowired
	    private DataStoreManager storeManager;
	    
	    @Autowired
	    private UniRefFacetConfig facetConfig;
	@Override
	protected void cleanEntries() {
		 storeManager.cleanSolr(DataStoreManager.StoreType.UNIREF);

	}

	@Override
	protected MockMvc getMockMvc() {
		return mockMvc;
	}

	@Override
	protected String getSearchRequestPath() {
		 return "/uniref/search";
	}

	@Override
	protected int getDefaultPageSize() {
		  return 25;
	}

	@Override
	protected List<SearchField> getAllSearchFields() {
		 return Arrays.asList(UniRefField.Search.values());
	}

	
	@Override
	protected String getFieldValueForValidatedField(SearchField searchField) {
		 String value = "*";
	        switch (searchField.getName()) {
	            case "id":
	            	value =ID_PREF +11;
	            	break;
	            case "taxonomy_id":
	            	value ="9606";
	            	break;
	            case "length":
	            	value ="[10 TO 500]";
	            			
	            	break;
	            case "count":
	            	value ="[2 TO 2]";
	            	break;
	            case "uniprot_id":
	            	value=ACC_PREF +11;
	            	break;
	            case "created":
	            	value = "[* TO *]";
	            	break;
	            case "upi":
	            	value = "UPI0000083A11";
	            	break;
	        }
	        return value;
	}

	@Override
	protected List<String> getAllSortFields() {
		 return Arrays.stream(UniRefField.Sort.values())
	                .map(UniRefField.Sort::getSolrFieldName)
	                .collect(Collectors.toList());
	}

	@Override
	protected List<String> getAllFacetFields() {
		 return new ArrayList<>(facetConfig.getFacetNames());
	}

	@Override
	protected List<String> getAllReturnedFields() {
		return Lists.newArrayList(
		UniRefResultFields.INSTANCE.getAllFields()
		.keySet());
	}

	@Override
	protected void saveEntry(SaveScenario saveContext) {
		saveEntry(11);
		saveEntry(20);

	}

	@Override
	protected void saveEntries(int numberOfEntries) {
		IntStream.rangeClosed(1, numberOfEntries)
		.forEach(i-> saveEntry(i));
	}
	
	
	private void saveEntry(int i) {
		UniRefEntry unirefEntry = createEntry(i);
		
		UniRefEntryConverter converter = new UniRefEntryConverter();
		Entry entry =converter.toXml(unirefEntry);
		storeManager.saveToStore(DataStoreManager.StoreType.UNIREF, unirefEntry);
		storeManager.saveEntriesInSolr	(DataStoreManager.StoreType.UNIREF, entry);

	}
	

	 private String getName(String prefix, int i) {
			if(i<10) {
				return prefix + "0" +i;
			}else
				return prefix  + i;
			
		}
	 
	 
	 private UniRefEntry createEntry(int i) {
			
			UniRefType type = UniRefType.UniRef100;
		
			UniRefEntryId entryId = new UniRefEntryIdBuilder(getName(ID_PREF, i)).build();
			
			UniRefEntry entry = new UniRefEntryBuilder()
					.id(entryId)
					.name(getName(NAME_PREF, i))
					.updated(LocalDate.now())
					.entryType(type)
					.commonTaxonId(9606l)
					.commonTaxon("Homo sapiens")
					.representativeMember(createReprestativeMember(i))
					.addMember(createMember(i))
					.addGoTerm(new GoTermBuilder().type(GoTermType.COMPONENT).id("GO:0044444").build())
					.addGoTerm(new GoTermBuilder().type(GoTermType.FUNCTION).id("GO:0044459").build())
					.addGoTerm(new GoTermBuilder().type(GoTermType.PROCESS).id("GO:0032459").build())
					.memberCount(2)
					.build();
			return entry;
		}
		private UniRefMember createMember(int i) {
			String memberId = getName(ACC_2_PREF, i)  +"_HUMAN";;
			int length=312;
			String pName ="some protein name"; 
			String upi = getName(UPI_PREF, i);
			
			UniRefMemberIdType type =UniRefMemberIdType.UNIPROTKB;
			UniRefMember member = new UniRefMemberBuilder()
					.memberIdType(type).memberId(memberId)
					.organismName("Homo sapiens")
					.organismTaxId(9606)
					.sequenceLength(length)
					.proteinName(pName)
					.uniparcId(new UniParcIdBuilder(upi).build())
					.accession(new UniProtAccessionBuilder(getName(ACC_2_PREF, i)).build())
					.uniref100Id(new UniRefEntryIdBuilder("UniRef100_P03923").build())
					.uniref90Id(new UniRefEntryIdBuilder("UniRef90_P03943").build())
					.uniref50Id(new UniRefEntryIdBuilder("UniRef50_P03973").build())
					.build();
			return member;
		}
		private RepresentativeMember createReprestativeMember(int i) {
			String seq = "MVSWGRFICLVVVTMATLSLARPSFSLVEDDFSAGSADFAFWERDGDSDGFDSHSDJHETRHJREH";
			Sequence sequence = new SequenceBuilder(seq).build();
			String memberId = getName(ACC_PREF, i)  +"_HUMAN";
			int length=312;
			String pName ="some protein name"; 
			String upi = getName(UPI_PREF, i);
			
			UniRefMemberIdType type =UniRefMemberIdType.UNIPROTKB;
			
			RepresentativeMember member = new RepresentativeMemberBuilder()
					.memberIdType(type).memberId(memberId)
					.organismName("Homo sapiens")
					.organismTaxId(9606)
					.sequenceLength(length)
					.proteinName(pName)
					.uniparcId(new UniParcIdBuilder(upi).build())
					.accession(new UniProtAccessionBuilder(getName(ACC_PREF, i)).build())
					.uniref100Id(new UniRefEntryIdBuilder("UniRef100_P03923").build())
					.uniref90Id(new UniRefEntryIdBuilder("UniRef90_P03943").build())
					.uniref50Id(new UniRefEntryIdBuilder("UniRef50_P03973").build())
					.isSeed(true)
					.sequence(sequence)
					.build();
			return member;
		}
	
	  static class UniRefSearchParameterResolver extends AbstractSearchParameterResolver {

	        @Override
	        protected SearchParameter searchCanReturnSuccessParameter() {
	            return SearchParameter.builder()
	                    .queryParam("query", Collections.singletonList("id:UniRef50_P03911"))
	                    .resultMatcher(jsonPath("$.results.*.id",contains("UniRef50_P03911")))
	                    .build();
	        }

	        @Override
	        protected SearchParameter searchCanReturnNotFoundParameter() {
	            return SearchParameter.builder()
	                    .queryParam("query", Collections.singletonList("id:UniRef50_P03931"))
	                    .resultMatcher(jsonPath("$.results.size()",is(0)))
	                    .build();
	        }

	        @Override
	        protected SearchParameter searchAllowWildcardQueryAllDocumentsParameter() {
	            return SearchParameter.builder()
	                    .queryParam("query", Collections.singletonList("id:*"))
	                    .resultMatcher(jsonPath("$.results.*.id",contains("UniRef50_P03911", "UniRef50_P03920")))
	                    .build();
	        }

	        @Override
	        protected SearchParameter searchQueryWithInvalidTypeQueryReturnBadRequestParameter() {
	            return SearchParameter.builder()
	                    .queryParam("query", Collections.singletonList("taxonomy_name:[1 TO 10]"))
	                    .resultMatcher(jsonPath("$.url",not(isEmptyOrNullString())))
	                    .resultMatcher(jsonPath("$.messages.*",
	                    		contains("'taxonomy_name' filter type 'range' is invalid. Expected 'term' filter type")))
	                    .build();
	        }

	        @Override
	        protected SearchParameter searchQueryWithInvalidValueQueryReturnBadRequestParameter() {
	            return SearchParameter.builder()
	                    .queryParam("query", Collections.singletonList("id:INVALID OR taxonomy_id:INVALID " +
	                            "OR length:INVALID OR count:INVALID  OR upi:INVALID"))
	                    .resultMatcher(jsonPath("$.url",not(isEmptyOrNullString())))
	                    .resultMatcher(jsonPath("$.messages.*",containsInAnyOrder(
	                    		"The 'id' value has invalid format. It should be a valid UniRef Cluster id",
	                    		 "The taxonomy id filter value should be a number",
	                            "'length' filter type 'term' is invalid. Expected 'range' filter type",	                           
	                            "'count' filter type 'term' is invalid. Expected 'range' filter type",
	                            "The 'upi' value has invalid format. It should be a valid UniParc UPI"
	                      )))
	                    .build();
	        }

	        @Override
	        protected SearchParameter searchSortWithCorrectValuesReturnSuccessParameter() {
	            return SearchParameter.builder()
	                    .queryParam("query", Collections.singletonList("*:*"))
	                    .queryParam("sort", Collections.singletonList("id desc"))
	                    .resultMatcher(jsonPath("$.results.*.id",contains("UniRef50_P03920", "UniRef50_P03911")))
	                    .build();
	        }

	        @Override
	        protected SearchParameter searchFieldsWithCorrectValuesReturnSuccessParameter() {
	            return SearchParameter.builder()
	                    .queryParam("query", Collections.singletonList("*:*"))
	                    .queryParam("fields", Collections.singletonList("name"))
	                    .resultMatcher(jsonPath("$.results.*.id",contains( "UniRef50_P03911", "UniRef50_P03920")))
	                  
	                
	                    .build();
	        }

	        @Override
	        protected SearchParameter searchFacetsWithCorrectValuesReturnSuccessParameter() {
	            return SearchParameter.builder()
	                    .queryParam("query", Collections.singletonList("*:*"))
	                //    .queryParam("facets", Collections.singletonList("reference"))
	                    .resultMatcher(jsonPath("$.results.*.id",contains("UniRef50_P03911", "UniRef50_P03920")))
	                    .build();
	        }
	    }


	    static class UniRefSearchContentTypeParamResolver extends AbstractSearchContentTypeParamResolver{

	        @Override
	        protected SearchContentTypeParam searchSuccessContentTypesParam() {
	            return SearchContentTypeParam.builder()
	                    .query("taxonomy_id:9606")
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(MediaType.APPLICATION_JSON)
	                            .resultMatcher(jsonPath("$.results.*.id",contains("UniRef50_P03911", "UniRef50_P03920")))
	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(MediaType.APPLICATION_XML)
	                            .resultMatcher(content().string(containsString("UniRef50_P03911")))
	                            .resultMatcher(content().string(containsString("UniRef50_P03920")))
	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(UniProtMediaType.LIST_MEDIA_TYPE)
	                            .resultMatcher(content().string(containsString("UniRef50_P03911")))
	                            .resultMatcher(content().string(containsString("UniRef50_P03920")))
	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(UniProtMediaType.TSV_MEDIA_TYPE)
	                            .resultMatcher(content().string(containsString("Cluster ID\tCluster Name\tCommon taxon\tSize\tDate of creation")))
	                            .resultMatcher(content().string(containsString("UniRef50_P03911	Cluster: MoeK5 11	Homo sapiens	2	2019-08-27")))
	                            .resultMatcher(content().string(containsString("UniRef50_P03920	Cluster: MoeK5 20	Homo sapiens	2	2019-08-27")))

	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(UniProtMediaType.XLS_MEDIA_TYPE)
	                            .resultMatcher(content().contentType(UniProtMediaType.XLS_MEDIA_TYPE))
	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(UniProtMediaType.FASTA_MEDIA_TYPE)
	                            .resultMatcher(content().contentType(UniProtMediaType.FASTA_MEDIA_TYPE))
	                            .build())
	                    .build();
	        }

	        @Override
	        protected SearchContentTypeParam searchBadRequestContentTypesParam() {
	            return SearchContentTypeParam.builder()
	                    .query("upi:invalid")
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(MediaType.APPLICATION_JSON)
	                            .resultMatcher(jsonPath("$.url",not(isEmptyOrNullString())))
	                            .resultMatcher(jsonPath("$.messages.*",contains("The 'upi' value has invalid format. It should be a valid UniParc UPI")))
	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(MediaType.APPLICATION_XML)
	                            .resultMatcher(content().string(containsString("The 'upi' value has invalid format. It should be a valid UniParc UPI")))
	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(UniProtMediaType.LIST_MEDIA_TYPE)
	                            .resultMatcher(content().string(isEmptyString()))
	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(UniProtMediaType.TSV_MEDIA_TYPE)
	                            .resultMatcher(content().string(isEmptyString()))
	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(UniProtMediaType.XLS_MEDIA_TYPE)
	                            .resultMatcher(content().string(isEmptyString()))
	                            .build())
	                    .contentTypeParam(ContentTypeParam.builder()
	                            .contentType(UniProtMediaType.FASTA_MEDIA_TYPE)
	                            .resultMatcher(content().contentType(UniProtMediaType.FASTA_MEDIA_TYPE))
	                            .build())
	                    .build();
	        }
	    }

}
