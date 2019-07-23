package uk.ac.ebi.uniprot.api.proteome.repository;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.uniprot.api.common.repository.search.facet.FacetConfig;
import uk.ac.ebi.uniprot.api.common.repository.search.facet.FacetProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jluo
 * @date: 24 Apr 2019
 *
*/
@Component
@Getter @Setter
@PropertySource({"classpath:proteome.facet.properties"})
@ConfigurationProperties(prefix = "facet")
public class ProteomeFacetConfig extends FacetConfig {

	 private Map<String, FacetProperty> proteome = new HashMap<>();
	
	@Override
	public Map<String, FacetProperty> getFacetPropertyMap() {
		return proteome;
	}

    @Override
    public Collection<String> getFacetNames() {
        return proteome.keySet();
    }
}

