package org.uniprot.api.idmapping.service;

import org.uniprot.api.common.exception.ResourceNotFoundException;
import org.uniprot.api.idmapping.model.IdMappingJob;

import static org.uniprot.api.idmapping.controller.response.JobStatus.FINISHED;

/**
 * @author sahmad
 * @created 23/02/2021
 */
public interface IdMappingJobCacheService {
    void put(String key, IdMappingJob value);

    IdMappingJob get(String key);

    boolean exists(String key);

    void delete(String key);

    default IdMappingJob getJobAsResource(String jobId) {
        if (exists(jobId)) {
            return get(jobId);
        } else {
            throw new ResourceNotFoundException("{search.not.found}");
        }
    }

    default IdMappingJob getCompletedJobAsResource(String jobId) {
        IdMappingJob job = getJobAsResource(jobId);
        if (job.getJobStatus() == FINISHED) {
            return job;
        } else {
            throw new ResourceNotFoundException("{search.not.found}");
        }
    }
}
