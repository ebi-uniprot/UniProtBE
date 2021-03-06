package org.uniprot.api.unirule.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.uniprot.api.rest.output.UniProtMediaType.LIST_MEDIA_TYPE_VALUE;
import static org.uniprot.api.rest.output.UniProtMediaType.TSV_MEDIA_TYPE_VALUE;
import static org.uniprot.api.rest.output.UniProtMediaType.XLS_MEDIA_TYPE_VALUE;
import static org.uniprot.api.rest.output.context.MessageConverterContextFactory.Resource.UNIRULE;

import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.uniprot.api.common.repository.search.QueryResult;
import org.uniprot.api.rest.controller.BasicSearchController;
import org.uniprot.api.rest.output.context.MessageConverterContext;
import org.uniprot.api.rest.output.context.MessageConverterContextFactory;
import org.uniprot.api.rest.validation.ValidReturnFields;
import org.uniprot.api.unirule.request.UniRuleSearchRequest;
import org.uniprot.api.unirule.request.UniRuleStreamRequest;
import org.uniprot.api.unirule.service.UniRuleService;
import org.uniprot.core.unirule.UniRuleEntry;
import org.uniprot.store.config.UniProtDataType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author sahmad
 * @created 11/11/2020
 */
@Tag(
        name = "unirule",
        description =
                "The unified rule(UniRule) resource for automatic annotation in the UniProt Knowledgebase ")
@RestController
@Validated
@RequestMapping("/unirule")
public class UniRuleController extends BasicSearchController<UniRuleEntry> {

    private static final int PREVIEW_SIZE = 10;
    private final UniRuleService uniRuleService;
    public static final String UNIRULE_ID_REGEX = "UR(\\d{9})";

    @Autowired
    public UniRuleController(
            ApplicationEventPublisher eventPublisher,
            MessageConverterContextFactory<UniRuleEntry> converterContextFactory,
            ThreadPoolTaskExecutor downloadTaskExecutor,
            UniRuleService uniRuleService) {
        super(eventPublisher, converterContextFactory, downloadTaskExecutor, UNIRULE);
        this.uniRuleService = uniRuleService;
    }

    @GetMapping(
            value = "/{uniruleid}",
            produces = {
                TSV_MEDIA_TYPE_VALUE,
                LIST_MEDIA_TYPE_VALUE,
                APPLICATION_JSON_VALUE,
                XLS_MEDIA_TYPE_VALUE
            })
    @Operation(
            summary = "Get a UniRule by id.",
            responses = {
                @ApiResponse(
                        content = {
                            @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UniRuleEntry.class)),
                            @Content(mediaType = TSV_MEDIA_TYPE_VALUE),
                            @Content(mediaType = LIST_MEDIA_TYPE_VALUE),
                            @Content(mediaType = XLS_MEDIA_TYPE_VALUE)
                        })
            })
    public ResponseEntity<MessageConverterContext<UniRuleEntry>> getByUniRuleId(
            @PathVariable("uniruleid")
                    @Pattern(
                            regexp = UNIRULE_ID_REGEX,
                            flags = {Pattern.Flag.CASE_INSENSITIVE},
                            message = "{search.unirule.invalid.id}")
                    String uniRuleId,
            @Parameter(description = "Comma separated list of fields to be returned in response")
                    @RequestParam(value = "fields", required = false)
                    @ValidReturnFields(uniProtDataType = UniProtDataType.UNIRULE)
                    String fields,
            HttpServletRequest request) {
        UniRuleEntry entryResult = this.uniRuleService.findByUniqueId(uniRuleId);
        return super.getEntityResponse(entryResult, fields, request);
    }

    @GetMapping(
            value = "/search",
            produces = {
                TSV_MEDIA_TYPE_VALUE,
                LIST_MEDIA_TYPE_VALUE,
                APPLICATION_JSON_VALUE,
                XLS_MEDIA_TYPE_VALUE
            })
    @Operation(
            summary = "Search for a UniRule entry (or entries) by a SOLR query.",
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
                                                                            UniRuleEntry.class))),
                            @Content(mediaType = TSV_MEDIA_TYPE_VALUE),
                            @Content(mediaType = LIST_MEDIA_TYPE_VALUE),
                            @Content(mediaType = XLS_MEDIA_TYPE_VALUE)
                        })
            })
    public ResponseEntity<MessageConverterContext<UniRuleEntry>> search(
            @Valid @ModelAttribute UniRuleSearchRequest searchRequest,
            @Parameter(hidden = true)
                    @RequestParam(value = "preview", required = false, defaultValue = "false")
                    boolean preview,
            HttpServletRequest request,
            HttpServletResponse response) {
        setPreviewInfo(searchRequest, preview);
        QueryResult<UniRuleEntry> results = uniRuleService.search(searchRequest);
        return super.getSearchResponse(results, searchRequest.getFields(), request, response);
    }

    @GetMapping(
            value = "/stream",
            produces = {
                TSV_MEDIA_TYPE_VALUE,
                LIST_MEDIA_TYPE_VALUE,
                APPLICATION_JSON_VALUE,
                XLS_MEDIA_TYPE_VALUE
            })
    @Operation(
            summary = "Stream a UniRule entry (or entries) by a SOLR query.",
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
                                                                            UniRuleEntry.class))),
                            @Content(mediaType = TSV_MEDIA_TYPE_VALUE),
                            @Content(mediaType = LIST_MEDIA_TYPE_VALUE),
                            @Content(mediaType = XLS_MEDIA_TYPE_VALUE)
                        })
            })
    public DeferredResult<ResponseEntity<MessageConverterContext<UniRuleEntry>>> stream(
            @Valid @ModelAttribute UniRuleStreamRequest streamRequest, HttpServletRequest request) {

        Stream<UniRuleEntry> result = uniRuleService.stream(streamRequest);
        return super.stream(result, streamRequest, getAcceptHeader(request), request);
    }

    @Override
    protected String getEntityId(UniRuleEntry entity) {
        return entity.getUniRuleId().getValue();
    }

    @Override
    protected Optional<String> getEntityRedirectId(UniRuleEntry entity) {
        return Optional.empty();
    }

    private void setPreviewInfo(UniRuleSearchRequest searchRequest, boolean preview) {
        if (preview) {
            searchRequest.setSize(PREVIEW_SIZE);
        }
    }
}
