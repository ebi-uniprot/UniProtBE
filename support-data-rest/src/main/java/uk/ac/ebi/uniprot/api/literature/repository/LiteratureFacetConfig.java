package uk.ac.ebi.uniprot.api.literature.repository;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.uniprot.api.common.repository.search.facet.FacetConfigConverter;
import uk.ac.ebi.uniprot.api.common.repository.search.facet.FacetProperty;
import uk.ac.ebi.uniprot.api.common.repository.search.facet.GenericFacetConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lgonzales
 * @since 2019-07-08
 */
@Component
@Getter
@Setter
@PropertySource("classpath:facet.properties")
@ConfigurationProperties(prefix = "facet")
public class LiteratureFacetConfig extends GenericFacetConfig implements FacetConfigConverter {

    private Map<String, FacetProperty> literature = new HashMap<>();

    @Override
    public Map<String, FacetProperty> getFacetPropertyMap() {
        return literature;
    }

    public Collection<String> getFacetNames() {
        return literature.keySet();
    }

}