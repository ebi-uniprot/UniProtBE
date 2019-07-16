package uk.ac.ebi.uniprot.api.literature.service;

import org.springframework.stereotype.Service;
import uk.ac.ebi.uniprot.api.common.repository.search.QueryResult;
import uk.ac.ebi.uniprot.api.common.repository.search.SolrRequest;
import uk.ac.ebi.uniprot.api.literature.repository.LiteratureFacetConfig;
import uk.ac.ebi.uniprot.api.literature.repository.LiteratureRepository;
import uk.ac.ebi.uniprot.api.literature.request.LiteratureMappedRequestDTO;
import uk.ac.ebi.uniprot.api.literature.request.LiteratureRequestDTO;
import uk.ac.ebi.uniprot.api.rest.service.BasicSearchService;
import uk.ac.ebi.uniprot.domain.literature.LiteratureEntry;
import uk.ac.ebi.uniprot.search.DefaultSearchHandler;
import uk.ac.ebi.uniprot.search.document.literature.LiteratureDocument;
import uk.ac.ebi.uniprot.search.field.LiteratureField;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lgonzales
 * @since 2019-07-04
 */
@Service
public class LiteratureService {

    private final BasicSearchService<LiteratureEntry, LiteratureDocument> basicService;
    private final DefaultSearchHandler defaultSearchHandler;
    private final LiteratureSortClause literatureSortClause;
    private final LiteratureFacetConfig facetConfig;
    private final LiteratureRepository repository;
    private final LiteratureEntryConverter entryConverter;

    public LiteratureService(LiteratureRepository repository, LiteratureFacetConfig facetConfig) {
        this.entryConverter = new LiteratureEntryConverter();
        this.basicService = new BasicSearchService<>(repository, entryConverter);
        this.defaultSearchHandler = new DefaultSearchHandler(LiteratureField.Search.content, LiteratureField.Search.id, LiteratureField.Search.getBoostFields());
        this.literatureSortClause = new LiteratureSortClause();
        this.facetConfig = facetConfig;
        this.repository = repository;
    }

    public LiteratureEntry findById(final String literatureId) {
        return basicService.getEntity(LiteratureField.Search.id.name(), literatureId);
    }

    public QueryResult<LiteratureEntry> search(LiteratureRequestDTO request) {
        SolrRequest solrRequest = basicService.createSolrRequest(request, facetConfig, literatureSortClause, defaultSearchHandler);
        return basicService.search(solrRequest, request.getCursor(), request.getSize());
    }

    public Stream<LiteratureEntry> download(LiteratureRequestDTO request) {
        SolrRequest solrRequest = basicService.createSolrRequest(request, facetConfig, literatureSortClause, defaultSearchHandler);
        return basicService.download(solrRequest);
    }

    public QueryResult<LiteratureEntry> getMappedLiteratureByUniprotAccession(final String accession, LiteratureMappedRequestDTO requestDTO) {
        SolrRequest solrRequest = SolrRequest.builder()
                .query("mapped_protein:" + accession)
                .addSort(literatureSortClause.getSort(requestDTO.getSort(), false))
                .build();
        QueryResult<LiteratureDocument> results = repository.searchPage(solrRequest, requestDTO.getCursor(), requestDTO.getSize());

        List<LiteratureEntry> converted = results.getContent().stream()
                .map(literatureDocument -> convertDocumentToEntryAndFilterMappedAccession(literatureDocument, accession))
                .collect(Collectors.toList());
        return QueryResult.of(converted, results.getPage(), results.getFacets());
    }

    private LiteratureEntry convertDocumentToEntryAndFilterMappedAccession(LiteratureDocument literatureDocument, String accession) {
        LiteratureEntry entry = entryConverter.apply(literatureDocument);
        entry.getLiteratureMappedReferences()
                .removeIf(mappedReference -> !mappedReference.getUniprotAccession().getValue().equalsIgnoreCase(accession));
        return entry;
    }

}
