package org.uniprot.api.common.repository.solrstream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpressionNamedParameter;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
import org.uniprot.core.util.Utils;

/**
 * This class is responsible for simplifying the creation of {@link TupleStream} instances for facet
 * functions and/or a search function of solr streaming, which enable us to get facets from Solr.
 * This template class should be initialised with correct configuration details, e.g., zookeeper
 * address and collection. This template instance can then be used to create specific {@link
 * TupleStream}s for a given query, using the original configuration details specified in the
 * template.
 *
 * <p>Created 21/08/18
 *
 * @author Edd
 * @author sahmad
 */
@Builder
@Slf4j
public class FacetTupleStreamTemplate extends AbstractTupleStreamTemplate {
    private final String zookeeperHost;
    private final String collection;
    private final HttpClient httpClient;

    public TupleStream create(SolrStreamFacetRequest request) {
        try {
            List<StreamExpression> expressions = new ArrayList<>();
            // add search function if needed
            if (request.isSearchAccession()) {
                StreamExpression searchExpression =
                        new SearchStreamExpression(this.collection, request);

                if (queryFilteredQuerySet(request)) {
                    searchExpression.addParameter(
                            new StreamExpressionNamedParameter("defType", "edismax"));
                    searchExpression.addParameter(
                            new StreamExpressionNamedParameter(
                                    "qf", request.getQueryConfig().getQueryFields()));
                    searchExpression.addParameter(
                            new StreamExpressionNamedParameter("fq", request.getFilteredQuery()));
                }
                expressions.add(searchExpression);
            }
            // create a solr streaming facet function call for each `facet`
            List<StreamExpression> facetExpressions =
                    request.getFacets().stream()
                            .map(
                                    facet ->
                                            new FacetStreamExpression.FacetStreamExpressionBuilder(
                                                            this.collection, facet, request)
                                                    .build())
                            .collect(Collectors.toList());

            expressions.addAll(facetExpressions);
            // we can replace list with plist function when solr >= 7.5
            ListStreamExpression listStreamExpression = new ListStreamExpression(expressions);
            StreamFactory streamFactory = getStreamFactory(this.zookeeperHost, this.collection);
            TupleStream tupleStream = streamFactory.constructStream(listStreamExpression);
            StreamContext clientContext = getStreamContext(this.collection, this.httpClient);
            tupleStream.setStreamContext(clientContext);
            return tupleStream;
        } catch (IOException e) {
            log.error("Could not create TupleStream", e);
            throw new IllegalStateException();
        }
    }

    private boolean queryFilteredQuerySet(SolrStreamFacetRequest request) {
        return Utils.notNullNotEmpty(request.getFilteredQuery())
                && Utils.notNullNotEmpty(request.getQuery());
    }
}
