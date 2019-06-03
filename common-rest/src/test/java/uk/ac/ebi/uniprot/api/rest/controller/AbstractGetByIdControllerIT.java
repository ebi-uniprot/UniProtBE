package uk.ac.ebi.uniprot.api.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.ac.ebi.uniprot.api.rest.controller.param.ContentTypeParam;
import uk.ac.ebi.uniprot.api.rest.controller.param.GetIdContentTypeParam;
import uk.ac.ebi.uniprot.api.rest.controller.param.GetIdParameter;
import uk.ac.ebi.uniprot.common.Utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author lgonzales
 */
@Slf4j
public abstract class AbstractGetByIdControllerIT {

    @Test
    protected void validIdReturnSuccess(GetIdParameter idParameter) throws Exception {
        assertThat(idParameter,notNullValue());
        assertThat(idParameter.getId(),notNullValue());
        assertThat(idParameter.getId(),not(isEmptyOrNullString()));
        assertThat(idParameter.getResultMatchers(),notNullValue());
        assertThat(idParameter.getResultMatchers(),not(emptyIterable()));
        // given
        saveEntry();

        // when
        MockHttpServletRequestBuilder requestBuilder = get(getIdRequestPath() + idParameter.getId())
                .header(ACCEPT, MediaType.APPLICATION_JSON);

        ResultActions response = getMockMvc().perform(requestBuilder);

        // then
        ResultActions resultActions = response.andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        for (ResultMatcher resultMatcher : idParameter.getResultMatchers()) {
            resultActions.andExpect(resultMatcher);
        }

    }

    @Test
    protected void invalidIdReturnBadRequest(GetIdParameter idParameter) throws Exception {
        assertThat(idParameter,notNullValue());
        assertThat(idParameter.getId(),notNullValue());
        assertThat(idParameter.getId(),not(isEmptyOrNullString()));
        assertThat(idParameter.getResultMatchers(),notNullValue());
        assertThat(idParameter.getResultMatchers(),not(emptyIterable()));
        // when
        MockHttpServletRequestBuilder requestBuilder = get(getIdRequestPath() + idParameter.getId())
                .header(ACCEPT, MediaType.APPLICATION_JSON);

        ResultActions response = getMockMvc().perform(requestBuilder);

        // then
        ResultActions resultActions = response.andDo(print())
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE));

        for (ResultMatcher resultMatcher : idParameter.getResultMatchers()) {
            resultActions.andExpect(resultMatcher);
        }

    }

    @Test
    protected void nonExistentIdReturnFoundRequest(GetIdParameter idParameter) throws Exception {
        assertThat(idParameter,notNullValue());
        assertThat(idParameter.getId(),notNullValue());
        assertThat(idParameter.getId(),not(isEmptyOrNullString()));
        assertThat(idParameter.getResultMatchers(),notNullValue());
        assertThat(idParameter.getResultMatchers(),not(emptyIterable()));
        // when
        MockHttpServletRequestBuilder requestBuilder = get(getIdRequestPath() + idParameter.getId())
                .header(ACCEPT, MediaType.APPLICATION_JSON);

        ResultActions response = getMockMvc().perform(requestBuilder);

        // then
        ResultActions resultActions = response.andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        for (ResultMatcher resultMatcher : idParameter.getResultMatchers()) {
            resultActions.andExpect(resultMatcher);
        }
    }

    @Test
    protected void withFilterFieldsReturnSuccess(GetIdParameter idParameter) throws Exception {
        assertThat(idParameter,notNullValue());
        if (Utils.notEmpty(idParameter.getFields())) {

            assertThat(idParameter.getId(),notNullValue());
            assertThat(idParameter.getId(),not(isEmptyOrNullString()));
            assertThat(idParameter.getResultMatchers(),notNullValue());
            assertThat(idParameter.getResultMatchers(),not(emptyIterable()));
            //when
            saveEntry();

            MockHttpServletRequestBuilder requestBuilder = get(getIdRequestPath() + idParameter.getId())
                    .header(ACCEPT, MediaType.APPLICATION_JSON)
                    .param("fields",idParameter.getFields());

            ResultActions response = getMockMvc().perform(requestBuilder);

            // then
            ResultActions resultActions = response.andDo(print())
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            for (ResultMatcher resultMatcher : idParameter.getResultMatchers()) {
                resultActions.andExpect(resultMatcher);
            }
        } else {
            log.info("Filter fields are not being tested, I am assuming that this is not a supported feature for this endpoint");
        }
    }

    @Test
    protected void withInvalidFilterFieldsReturnBadRequest(GetIdParameter idParameter) throws Exception {
        if (Utils.notEmpty(idParameter.getFields())) {
            //when
            MockHttpServletRequestBuilder requestBuilder = get(getIdRequestPath() + idParameter.getId())
                    .header(ACCEPT, MediaType.APPLICATION_JSON)
                    .param("fields",idParameter.getFields());

            ResultActions response = getMockMvc().perform(requestBuilder);

            // then
            ResultActions resultActions = response.andDo(print())
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            for (ResultMatcher resultMatcher : idParameter.getResultMatchers()) {
                resultActions.andExpect(resultMatcher);
            }
        } else {
            log.info("Filter fields are not being tested, I am assuming that this is not a supported feature for this endpoint");
        }
    }

    @Test
    protected void idSuccessContentTypes(GetIdContentTypeParam contentTypeParam) throws Exception {
        // given
        saveEntry();

        assertThat(contentTypeParam, notNullValue());
        assertThat(contentTypeParam.getContentTypeParams(), notNullValue());
        assertThat(contentTypeParam.getContentTypeParams(), not(empty()));

        for(ContentTypeParam contentType: contentTypeParam.getContentTypeParams()) {
            // when
            MockHttpServletRequestBuilder requestBuilder = get(getIdRequestPath() + contentTypeParam.getId())
                    .header(ACCEPT, contentType.getContentType());

            ResultActions response = getMockMvc().perform(requestBuilder);

            // then
            ResultActions resultActions = response.andDo(print())
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, contentType.getContentType().toString()));

            for (ResultMatcher resultMatcher : contentType.getResultMatchers()) {
                resultActions.andExpect(resultMatcher);
            }
        }
    }

    @Test
    protected void idBadRequestContentTypes(GetIdContentTypeParam contentTypeParam) throws Exception {
        assertThat(contentTypeParam, notNullValue());
        assertThat(contentTypeParam.getContentTypeParams(), notNullValue());
        assertThat(contentTypeParam.getContentTypeParams(), not(empty()));

        // when
        for(ContentTypeParam contentType: contentTypeParam.getContentTypeParams()) {
            // when
            MockHttpServletRequestBuilder requestBuilder = get(getIdRequestPath() + contentTypeParam.getId())
                    .header(ACCEPT, contentType.getContentType());

            ResultActions response = getMockMvc().perform(requestBuilder);

            // then
            ResultActions resultActions = response.andDo(print())
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, contentType.getContentType().toString()));

            for (ResultMatcher resultMatcher : contentType.getResultMatchers()) {
                resultActions.andExpect(resultMatcher);
            }
        }
    }

    protected abstract void saveEntry();

    protected abstract MockMvc getMockMvc();

    protected abstract String getIdRequestPath();

}
