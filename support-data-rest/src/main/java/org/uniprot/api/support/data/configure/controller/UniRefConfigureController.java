package org.uniprot.api.support.data.configure.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uniprot.api.support.data.configure.response.AdvancedSearchTerm;
import org.uniprot.api.support.data.configure.response.UniProtReturnField;
import org.uniprot.api.support.data.configure.service.UniRefConfigureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author jluo
 * @date: 20 Aug 2019
 */
@Tag(
        name = "Configuration",
        description = "These services provide configuration data used in the UniProt website")
@RestController
@RequestMapping("/configure/uniref")
public class UniRefConfigureController {
    private final UniRefConfigureService service;
    private final ServletContext servletContext;

    public UniRefConfigureController(
            UniRefConfigureService service, ServletContext servletContext) {
        this.service = service;
        this.servletContext = servletContext;
    }

    @Operation(
            summary = "List of return fields available in the UniRef services.",
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
                                                                            UniProtReturnField
                                                                                    .class)))
                        })
            })
    @GetMapping("/result-fields")
    public List<UniProtReturnField> getResultFields() {
        return service.getResultFields();
    }

    @Operation(
            summary = "List of search fields available in the UniRef services.",
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
                                                                            AdvancedSearchTerm
                                                                                    .class)))
                        })
            })
    @GetMapping("/search-fields")
    public List<AdvancedSearchTerm> getSearchFields() {
        return service.getSearchItems(servletContext.getContextPath());
    }
}
