package org.uniprot.api.rest.controller.param;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

@Data
@Builder
public class ContentTypeParam {

    private MediaType contentType;

    @Singular private List<ResultMatcher> resultMatchers;
}
