package org.uniprot.api.idmapping.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.uniprot.api.idmapping.model.IdMappingJob;
import org.uniprot.api.idmapping.service.IdMappingJobCacheService;
import org.uniprot.store.indexer.uniref.mockers.UniRefEntryMocker;

/**
 * @author sahmad
 * @created 03/03/2021
 */
public class UniRefIdMappingResultsJobOperation extends AbstractJobOperation {
    public UniRefIdMappingResultsJobOperation(IdMappingJobCacheService cacheService) {
        super(cacheService);
    }

    @Override
    public IdMappingJob createAndPutJobInCache() throws Exception {
        Map<String, String> ids = new LinkedHashMap<>();
        for (int i = 1; i <= 20; i++) {
            String fromId = String.format("Q%05d", i);
            String toId = String.format(UniRefEntryMocker.ID_PREF_50 + "%02d", i);
            ids.put(fromId, toId);
        }
        return createAndPutJobInCache("ACC", "NF50", ids);
    }
}