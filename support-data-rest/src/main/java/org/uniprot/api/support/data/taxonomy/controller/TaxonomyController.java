package org.uniprot.api.support.data.taxonomy.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.uniprot.api.rest.output.UniProtMediaType.LIST_MEDIA_TYPE_VALUE;
import static org.uniprot.api.rest.output.UniProtMediaType.RDF_MEDIA_TYPE;
import static org.uniprot.api.rest.output.UniProtMediaType.RDF_MEDIA_TYPE_VALUE;
import static org.uniprot.api.rest.output.UniProtMediaType.TSV_MEDIA_TYPE_VALUE;
import static org.uniprot.api.rest.output.UniProtMediaType.XLS_MEDIA_TYPE_VALUE;
import static org.uniprot.api.rest.output.context.MessageConverterContextFactory.Resource.TAXONOMY;

import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.uniprot.api.common.repository.search.QueryResult;
import org.uniprot.api.rest.controller.BasicSearchController;
import org.uniprot.api.rest.output.context.MessageConverterContext;
import org.uniprot.api.rest.output.context.MessageConverterContextFactory;
import org.uniprot.api.rest.validation.ValidReturnFields;
import org.uniprot.api.support.data.taxonomy.request.GetByTaxonIdsRequest;
import org.uniprot.api.support.data.taxonomy.request.TaxonomySearchRequest;
import org.uniprot.api.support.data.taxonomy.request.TaxonomyStreamRequest;
import org.uniprot.api.support.data.taxonomy.service.TaxonomyService;
import org.uniprot.core.taxonomy.TaxonomyEntry;
import org.uniprot.core.taxonomy.TaxonomyInactiveReasonType;
import org.uniprot.store.config.UniProtDataType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Taxonomy",
        description =
                "UniProtKB taxonomy data is manually curated: next to manually verified organism names, we provide a selection of external links, organism strains and viral host information.")
@RestController
@RequestMapping("/taxonomy")
@Validated
public class TaxonomyController extends BasicSearchController<TaxonomyEntry> {

    private final TaxonomyService taxonomyService;
    private static final String TAXONOMY_ID_REGEX = "^[0-9]+$";

    public TaxonomyController(
            ApplicationEventPublisher eventPublisher,
            TaxonomyService taxonomyService,
            @Qualifier("taxonomyMessageConverterContextFactory")
                    MessageConverterContextFactory<TaxonomyEntry>
                            taxonomyMessageConverterContextFactory,
            ThreadPoolTaskExecutor downloadTaskExecutor) {
        super(
                eventPublisher,
                taxonomyMessageConverterContextFactory,
                downloadTaskExecutor,
                TAXONOMY);
        this.taxonomyService = taxonomyService;
    }

    @Operation(
            summary = "Get taxonomy by id.",
            responses = {
                @ApiResponse(
                        content = {
                            @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TaxonomyEntry.class)),
                            @Content(mediaType = TSV_MEDIA_TYPE_VALUE),
                            @Content(mediaType = LIST_MEDIA_TYPE_VALUE),
                            @Content(mediaType = XLS_MEDIA_TYPE_VALUE),
                            @Content(mediaType = RDF_MEDIA_TYPE_VALUE)
                        })
            })
    @GetMapping(
            value = "/{taxonId}",
            produces = {
                TSV_MEDIA_TYPE_VALUE,
                LIST_MEDIA_TYPE_VALUE,
                APPLICATION_JSON_VALUE,
                XLS_MEDIA_TYPE_VALUE,
                RDF_MEDIA_TYPE_VALUE
            })
    public ResponseEntity<MessageConverterContext<TaxonomyEntry>> getById(
            @Parameter(description = "Taxon id to find")
                    @PathVariable("taxonId")
                    @Pattern(
                            regexp = TAXONOMY_ID_REGEX,
                            flags = {Pattern.Flag.CASE_INSENSITIVE},
                            message = "{search.taxonomy.invalid.id}")
                    String taxonId,
            @Parameter(description = "Comma separated list of fields to be returned in response")
                    @ValidReturnFields(uniProtDataType = UniProtDataType.TAXONOMY)
                    @RequestParam(value = "fields", required = false)
                    String fields,
            HttpServletRequest request) {

        if (isRDFAccept(request)) {
            String result = this.taxonomyService.getRDFXml(taxonId);
            return super.getEntityResponseRDF(result, getAcceptHeader(request), request);
        }
        TaxonomyEntry taxonomyEntry = this.taxonomyService.findById(Long.parseLong(taxonId));
        return super.getEntityResponse(taxonomyEntry, fields, request);
    }

    @Operation(
            summary = "Get taxonomy by comma separated taxon ids.",
            responses = {
                @ApiResponse(
                        content = {
                            @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            TaxonomyEntry.class))),
                            @Content(mediaType = TSV_MEDIA_TYPE_VALUE),
                            @Content(mediaType = LIST_MEDIA_TYPE_VALUE),
                            @Content(mediaType = XLS_MEDIA_TYPE_VALUE)
                        })
            })
    @GetMapping(
            value = "/taxonIds/{taxonIds}",
            produces = {
                TSV_MEDIA_TYPE_VALUE,
                LIST_MEDIA_TYPE_VALUE,
                APPLICATION_JSON_VALUE,
                XLS_MEDIA_TYPE_VALUE
            })
    public ResponseEntity<MessageConverterContext<TaxonomyEntry>> getByIds(
            @Valid @ModelAttribute GetByTaxonIdsRequest getByTaxonIdsRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        QueryResult<TaxonomyEntry> result = this.taxonomyService.search(getByTaxonIdsRequest);
        return super.getSearchResponse(
                result,
                getByTaxonIdsRequest.getFields(),
                getByTaxonIdsRequest.isDownload(),
                request,
                response);
    }

    @Operation(
            summary = "Search taxonomies by given Lucene search query.",
            responses = {
                @ApiResponse(
                        content = {
                            @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            TaxonomyEntry.class))),
                            @Content(mediaType = TSV_MEDIA_TYPE_VALUE),
                            @Content(mediaType = LIST_MEDIA_TYPE_VALUE),
                            @Content(mediaType = XLS_MEDIA_TYPE_VALUE)
                        })
            })
    @GetMapping(
            value = "/search",
            produces = {
                TSV_MEDIA_TYPE_VALUE,
                LIST_MEDIA_TYPE_VALUE,
                APPLICATION_JSON_VALUE,
                XLS_MEDIA_TYPE_VALUE
            })
    public ResponseEntity<MessageConverterContext<TaxonomyEntry>> search(
            @Valid @ModelAttribute TaxonomySearchRequest searchRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        QueryResult<TaxonomyEntry> results = taxonomyService.search(searchRequest);
        return super.getSearchResponse(results, searchRequest.getFields(), request, response);
    }

    @Operation(
            summary = "Download taxonomies by given Lucene search query.",
            responses = {
                @ApiResponse(
                        content = {
                            @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            TaxonomyEntry.class))),
                            @Content(mediaType = TSV_MEDIA_TYPE_VALUE),
                            @Content(mediaType = LIST_MEDIA_TYPE_VALUE),
                            @Content(mediaType = XLS_MEDIA_TYPE_VALUE),
                            @Content(mediaType = RDF_MEDIA_TYPE_VALUE)
                        })
            })
    @GetMapping(
            value = "/stream",
            produces = {
                TSV_MEDIA_TYPE_VALUE,
                LIST_MEDIA_TYPE_VALUE,
                APPLICATION_JSON_VALUE,
                XLS_MEDIA_TYPE_VALUE,
                RDF_MEDIA_TYPE_VALUE
            })
    public DeferredResult<ResponseEntity<MessageConverterContext<TaxonomyEntry>>> stream(
            @Valid @ModelAttribute TaxonomyStreamRequest streamRequest,
            @RequestHeader(value = "Accept", defaultValue = APPLICATION_JSON_VALUE)
                    MediaType contentType,
            HttpServletRequest request) {
        if (contentType.equals(RDF_MEDIA_TYPE)) {
            Stream<String> result = taxonomyService.streamRDF(streamRequest);
            return super.streamRDF(result, streamRequest, contentType, request);
        } else {
            Stream<TaxonomyEntry> result = taxonomyService.stream(streamRequest);
            return super.stream(result, streamRequest, contentType, request);
        }
    }

    @Override
    protected String getEntityId(TaxonomyEntry entity) {
        return String.valueOf(entity.getTaxonId());
    }

    @Override
    protected Optional<String> getEntityRedirectId(TaxonomyEntry entity) {
        if (isInactiveAndMergedEntity(entity)) {
            return Optional.of(String.valueOf(entity.getInactiveReason().getMergedTo()));
        } else {
            return Optional.empty();
        }
    }

    private boolean isInactiveAndMergedEntity(TaxonomyEntry entity) {
        return !entity.isActive()
                && entity.hasInactiveReason()
                && TaxonomyInactiveReasonType.MERGED.equals(
                        entity.getInactiveReason().getInactiveReasonType());
    }
}
