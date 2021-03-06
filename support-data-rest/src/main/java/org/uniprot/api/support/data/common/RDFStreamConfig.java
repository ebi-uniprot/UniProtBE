package org.uniprot.api.support.data.common;

import java.io.IOException;
import java.time.temporal.ChronoUnit;

import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.RetryPolicy;

import org.uniprot.api.common.repository.stream.rdf.RDFStreamerConfigProperties;

/**
 * @author sahmad
 * @created 02/02/2021
 */
@Slf4j
public class RDFStreamConfig {
    private RDFStreamConfig() {}

    public static RetryPolicy<Object> rdfRetryPolicy(RDFStreamerConfigProperties rdfConfigProps) {
        int rdfRetryDelay = rdfConfigProps.getRetryDelayMillis();
        int maxRdfRetryDelay = rdfRetryDelay * 8;
        return new RetryPolicy<>()
                .handle(IOException.class)
                .withBackoff(rdfRetryDelay, maxRdfRetryDelay, ChronoUnit.MILLIS)
                .withMaxRetries(rdfConfigProps.getMaxRetries())
                .onRetry(
                        e ->
                                log.warn(
                                        "Call to RDF server failed. Failure #{}. Retrying...",
                                        e.getAttemptCount()));
    }
}
