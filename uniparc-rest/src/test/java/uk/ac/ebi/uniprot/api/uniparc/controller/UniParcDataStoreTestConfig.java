package uk.ac.ebi.uniprot.api.uniparc.controller;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.core.CoreContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import uk.ac.ebi.uniprot.api.common.repository.search.SolrRequest;
import uk.ac.ebi.uniprot.api.common.repository.search.SolrRequestConverter;
import uk.ac.ebi.uniprot.indexer.ClosableEmbeddedSolrClient;
import uk.ac.ebi.uniprot.indexer.DataStoreManager;
import uk.ac.ebi.uniprot.indexer.SolrDataStoreManager;
import uk.ac.ebi.uniprot.search.SolrCollection;

/**
 *
 * @author jluo
 * @date: 25 Jun 2019
 *
*/
@TestConfiguration
public class UniParcDataStoreTestConfig {
	@Bean(destroyMethod = "close")
    public DataStoreManager dataStoreManager() throws IOException,URISyntaxException{
        SolrDataStoreManager sdsm = new SolrDataStoreManager();
        DataStoreManager dataStoreManager=  new DataStoreManager(sdsm);
        return dataStoreManager;
    }

    @Bean
    @Profile("offline")
    public HttpClient httpClient() {
        return mock(HttpClient.class);
    }
    @Bean
    @Profile("offline")
    public SolrClient uniparcSolrClient(DataStoreManager dataStoreManager) throws URISyntaxException {
    	 CoreContainer container = new CoreContainer(new File(System.getProperty(ClosableEmbeddedSolrClient.SOLR_HOME)).getAbsolutePath());
         container.load();
     	
        ClosableEmbeddedSolrClient solrClient = new ClosableEmbeddedSolrClient(container, SolrCollection.uniparc);
        addStoreInfoProteome(dataStoreManager, solrClient, DataStoreManager.StoreType.UNIPARC );
        return solrClient;
    }
    
    @Bean
    @Profile("offline")
    public SolrRequestConverter requestConverter() {
        return new SolrRequestConverter() {
            @Override
            public SolrQuery toSolrQuery(SolrRequest request) {
                SolrQuery solrQuery = super.toSolrQuery(request);

                // required for tests, because EmbeddedSolrServer is not sharded
                solrQuery.setParam("distrib", "false");
                solrQuery.setParam("terms.mincount", "1");

                return solrQuery;
            }
        };
    }
    private void addStoreInfoProteome(DataStoreManager dsm, ClosableEmbeddedSolrClient solrClient, DataStoreManager.StoreType store ) throws URISyntaxException {
        dsm.addSolrClient(store, solrClient);
    }
}

