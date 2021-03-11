package org.uniprot.api.support.data.configure.service;

import org.junit.jupiter.api.Test;
import org.uniprot.api.support.data.configure.response.AdvancedSearchTerm;
import org.uniprot.api.support.data.configure.response.UniProtReturnField;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author lgonzales
 * @since 11/03/2021
 */
class TaxonomyConfigureServiceTest {

    @Test
    void getResultFields() {
        TaxonomyConfigureService service = new TaxonomyConfigureService();
        List<UniProtReturnField> resultGroups = service.getResultFields();

        assertNotNull(resultGroups);
        assertEquals(1, resultGroups.size());

        assertEquals(14, resultGroups.get(0).getFields().size());
    }

    @Test
    void getSearchItems() {
        TaxonomyConfigureService service = new TaxonomyConfigureService();
        List<AdvancedSearchTerm> result = service.getSearchItems();
        assertNotNull(result);
        assertEquals(8, result.size());
    }
}