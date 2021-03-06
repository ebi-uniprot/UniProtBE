package org.uniprot.api.idmapping.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.uniprot.api.rest.output.UniProtMediaType.*;
import static org.uniprot.api.rest.output.context.MessageConverterContextFactory.Resource.IDMAPPING_PIR;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.uniprot.api.common.repository.search.QueryResult;
import org.uniprot.api.idmapping.controller.request.IdMappingPageRequest;
import org.uniprot.api.idmapping.controller.request.IdMappingStreamRequest;
import org.uniprot.api.idmapping.model.IdMappingJob;
import org.uniprot.api.idmapping.model.IdMappingStringPair;
import org.uniprot.api.idmapping.service.IdMappingJobCacheService;
import org.uniprot.api.idmapping.service.IdMappingPIRService;
import org.uniprot.api.rest.controller.BasicSearchController;
import org.uniprot.api.rest.output.context.MessageConverterContext;
import org.uniprot.api.rest.output.context.MessageConverterContextFactory;

import com.google.common.base.Stopwatch;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Created 15/02/2021
 *
 * @author Edd
 */
@Tag(name = "results", description = "APIs to get result of the submitted job.")
@RestController
@Validated
@Slf4j
@RequestMapping(value = IdMappingJobController.IDMAPPING_PATH)
public class IdMappingResultsController extends BasicSearchController<IdMappingStringPair> {
    private final IdMappingPIRService idMappingService;
    private final IdMappingJobCacheService cacheService;

    @Autowired
    public IdMappingResultsController(
            ApplicationEventPublisher eventPublisher,
            IdMappingPIRService idMappingService,
            IdMappingJobCacheService cacheService,
            MessageConverterContextFactory<IdMappingStringPair> converterContextFactory,
            ThreadPoolTaskExecutor downloadTaskExecutor) {
        super(eventPublisher, converterContextFactory, downloadTaskExecutor, IDMAPPING_PIR);
        this.idMappingService = idMappingService;
        this.cacheService = cacheService;
    }

    @GetMapping(
            value = "/results/{jobId}",
            produces = {TSV_MEDIA_TYPE_VALUE, APPLICATION_JSON_VALUE, XLS_MEDIA_TYPE_VALUE})
    @Operation(
            summary = "Search result by a submitted job id.",
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
                                                                            IdMappingStringPair
                                                                                    .class))),
                            @Content(mediaType = TSV_MEDIA_TYPE_VALUE),
                            @Content(mediaType = LIST_MEDIA_TYPE_VALUE),
                            @Content(mediaType = XLS_MEDIA_TYPE_VALUE)
                        })
            })
    public ResponseEntity<MessageConverterContext<IdMappingStringPair>> results(
            @PathVariable String jobId,
            @Valid @ModelAttribute IdMappingPageRequest pageRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        IdMappingJob completedJob = cacheService.getCompletedJobAsResource(jobId);

        Stopwatch stopwatch = Stopwatch.createStarted();
        QueryResult<IdMappingStringPair> queryResult =
                idMappingService.queryResultPage(pageRequest, completedJob.getIdMappingResult());
        stopwatch.stop();

        log.debug(
                "[idmapping/results/{}] response took {} seconds (id count={})",
                jobId,
                stopwatch.elapsed(TimeUnit.SECONDS),
                completedJob.getIdMappingRequest().getIds().split(",").length);
        return super.getSearchResponse(queryResult, null, request, response);
    }

    @GetMapping(
            value = "/stream/{jobId}",
            produces = {
                TSV_MEDIA_TYPE_VALUE,
                APPLICATION_JSON_VALUE,
                XLS_MEDIA_TYPE_VALUE,
                LIST_MEDIA_TYPE_VALUE
            })
    @Operation(
            summary = "Stream result by a submitted job id.",
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
                                                                            IdMappingStringPair
                                                                                    .class))),
                            @Content(mediaType = TSV_MEDIA_TYPE_VALUE),
                            @Content(mediaType = LIST_MEDIA_TYPE_VALUE),
                            @Content(mediaType = XLS_MEDIA_TYPE_VALUE)
                        })
            })
    public DeferredResult<ResponseEntity<MessageConverterContext<IdMappingStringPair>>>
            streamResults(
                    @PathVariable String jobId,
                    @Valid @ModelAttribute IdMappingStreamRequest streamRequest,
                    HttpServletRequest request,
                    HttpServletResponse response) {

        IdMappingJob completedJob = cacheService.getCompletedJobAsResource(jobId);

        return super.stream(
                completedJob.getIdMappingResult().getMappedIds().stream(),
                streamRequest,
                getAcceptHeader(request),
                request);
    }

    @Override
    protected String getEntityId(IdMappingStringPair entity) {

        return entity.getTo();
    }

    @Override
    protected Optional<String> getEntityRedirectId(IdMappingStringPair entity) {
        return Optional.empty();
    }
}
