package org.uniprot.api.common.repository.store;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import org.uniprot.api.common.repository.search.SolrRequest;
import org.uniprot.api.rest.service.RDFService;

/**
 * @author sahmad
 * @created 26/01/2021
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractRDFStreamer {
    private final RDFService<String> rdfService;
    private final RetryPolicy<Object> rdfFetchRetryPolicy; // retry policy for RDF rest call
    private final String rdfProlog; // rdf prefix
    private final int rdfBatchSize; // number of accession in rdf rest request

    protected abstract Stream<String> fetchIds(SolrRequest solrRequest);

    public Stream<String> idsToRDFStoreStream(SolrRequest solrRequest) {
        Stream<String> idsStream = fetchIds(solrRequest);

        AbstractRDFStreamer.BatchRDFStoreIterable batchRDFStoreIterable =
                new AbstractRDFStreamer.BatchRDFStoreIterable(
                        idsStream::iterator, rdfService, rdfFetchRetryPolicy, rdfBatchSize);

        Stream<String> rdfStringStream =
                StreamSupport.stream(batchRDFStoreIterable.spliterator(), false)
                        .flatMap(Collection::stream)
                        .onClose(
                                () ->
                                        log.debug(
                                                "Finished streaming over search results and fetching from RDF server."));

        // prepend rdf prolog then rdf data and then append closing rdf tag
        return Stream.concat(
                Stream.of(rdfProlog),
                Stream.concat(rdfStringStream, Stream.of(RDFService.RDF_CLOSE_TAG)));
    }

    // iterable for RDF streaming
    private static class BatchRDFStoreIterable extends BatchIterable<String> {
        private final RDFService<String> rdfService;
        private final RetryPolicy<Object> retryPolicy;

        BatchRDFStoreIterable(
                Iterable<String> sourceIterable,
                RDFService<String> rdfService,
                RetryPolicy<Object> retryPolicy,
                int batchSize) {
            super(sourceIterable, batchSize);
            this.rdfService = rdfService;
            this.retryPolicy = retryPolicy;
        }

        @Override
        List<String> convertBatch(List<String> batch) {
            return Failsafe.with(retryPolicy)
                    .onFailure(
                            throwable ->
                                    log.error(
                                            "Call to RDF server failed for accessions {} with error {}",
                                            batch,
                                            throwable.getFailure().getMessage()))
                    .get(() -> rdfService.getEntries(batch));
        }
    }
}
