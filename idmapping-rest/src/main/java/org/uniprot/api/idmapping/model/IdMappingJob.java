package org.uniprot.api.idmapping.model;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

import org.uniprot.api.idmapping.controller.request.IdMappingJobRequest;
import org.uniprot.api.idmapping.controller.response.JobStatus;

/**
 * @author sahmad
 * @created 23/02/2021
 */
@Data
@Builder(toBuilder = true)
public class IdMappingJob {
    private String jobId;
    private JobStatus jobStatus;
    private IdMappingJobRequest idMappingRequest;
    private IdMappingResult idMappingResult;
    private String errorMessage;
    @Builder.Default private Date created = new Date();
    @Builder.Default private Date updated = new Date();
}
