package org.uniprot.api.idmapping.controller.request;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * @author sahmad
 * @created 22/02/2021
 */
@Data
@EqualsAndHashCode
@Builder
public class IdMappingRequest {
    @NotNull(message = "{search.required}")
    @Parameter(description = "Name of the from type")
    private String from; // TODO add a from validator to verify supported from

    @NotNull(message = "{search.required}")
    @Parameter(description = "Name of the to type")
    private String to; // TODO add a to validator based on from

    @NotNull(message = "{search.required}")
    @Parameter(description = "Comma separated list of ids")
    private String ids; // TODO add validation like length, regex

    @Parameter(description = "Value of the taxon Id")
    private String taxId;
}
