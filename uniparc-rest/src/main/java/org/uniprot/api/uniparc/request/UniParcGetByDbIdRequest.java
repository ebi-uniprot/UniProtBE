package org.uniprot.api.uniparc.request;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * @author sahmad
 * @created 13/08/2020
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UniParcGetByDbIdRequest extends UniParcGetByIdRequest {
    @Parameter(
            description =
                    "All UniParc cross reference accessions, eg. AAC02967 (EMBL) or XP_006524055 (RefSeq)")
    @NotNull(message = "{search.required}")
    private String dbId;
}
