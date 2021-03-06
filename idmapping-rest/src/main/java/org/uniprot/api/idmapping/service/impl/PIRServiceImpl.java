package org.uniprot.api.idmapping.service.impl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.uniprot.api.idmapping.controller.request.IdMappingJobRequest;
import org.uniprot.api.idmapping.model.IdMappingResult;
import org.uniprot.api.idmapping.service.IdMappingPIRService;
import org.uniprot.api.idmapping.service.PIRResponseConverter;
import org.uniprot.store.config.idmapping.IdMappingFieldConfig;

/**
 * Created 17/02/2021
 *
 * @author Edd
 */
@Profile("live")
@Service
public class PIRServiceImpl extends IdMappingPIRService {
    static final HttpHeaders HTTP_HEADERS = new HttpHeaders();

    static {
        HTTP_HEADERS.setContentType(APPLICATION_FORM_URLENCODED);
    }

    public final String pirIdMappingUrl;
    private final RestTemplate restTemplate;
    private final PIRResponseConverter pirResponseConverter;

    @Autowired
    public PIRServiceImpl(
            RestTemplate idMappingRestTemplate,
            @Value("${search.default.page.size:#{null}}") Integer defaultPageSize,
            @Value(
                            "${id.mapping.pir.url:https://idmapping.uniprot.org/cgi-bin/idmapping_http_client_async}")
                    String pirMappingUrl) {

        super(defaultPageSize);
        this.restTemplate = idMappingRestTemplate;
        this.pirResponseConverter = new PIRResponseConverter();
        this.pirIdMappingUrl = UriComponentsBuilder.fromHttpUrl(pirMappingUrl).toUriString();
    }

    public String getPirIdMappingUrl() {
        return pirIdMappingUrl;
    }

    @Override
    public IdMappingResult mapIds(IdMappingJobRequest request) {
        HttpEntity<MultiValueMap<String, String>> requestBody =
                new HttpEntity<>(createPostBody(request), HTTP_HEADERS);

        return pirResponseConverter.convertToIDMappings(
                request, restTemplate.postForEntity(pirIdMappingUrl, requestBody, String.class));
    }

    private MultiValueMap<String, String> createPostBody(IdMappingJobRequest request) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("ids", String.join(",", request.getIds()));

        map.add("from", IdMappingFieldConfig.convertDbNameToPIRDbName(request.getFrom()));
        map.add("to", IdMappingFieldConfig.convertDbNameToPIRDbName(request.getTo()));

        map.add("tax_off", "NO"); // we do not need PIR's header line, "Taxonomy ID:"
        map.add("taxid", request.getTaxId());
        map.add("async", "NO");

        return map;
    }
}
