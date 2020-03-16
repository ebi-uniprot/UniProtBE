package org.uniprot.api.disease.download.IT;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.uniprot.api.DataStoreTestConfig;
import org.uniprot.api.disease.DiseaseController;
import org.uniprot.api.disease.download.resolver.DiseaseDownloadAllParamResolver;
import org.uniprot.api.rest.controller.param.DownloadParamAndResult;
import org.uniprot.api.support_data.SupportDataApplication;

/** Class to test download api without any filter.. kind of download everything */
@ContextConfiguration(classes = {DataStoreTestConfig.class, SupportDataApplication.class})
@ActiveProfiles(profiles = "offline")
@WebMvcTest(DiseaseController.class)
@ExtendWith(value = {SpringExtension.class})
public class DiseaseDownloadAllIT extends BaseDiseaseDownloadIT {
    @RegisterExtension
    static DiseaseDownloadAllParamResolver paramResolver = new DiseaseDownloadAllParamResolver();

    @Test
    protected void testDownloadAllJSON(DownloadParamAndResult paramAndResult) throws Exception {
        sendAndVerify(paramAndResult, HttpStatus.OK);
    }

    @ParameterizedTest(name = "[{index}]~/download?{0}")
    @MethodSource("provideRequestResponseByType")
    void testDownloadAll(DownloadParamAndResult paramAndResult) throws Exception {
        sendAndVerify(paramAndResult, HttpStatus.OK);
    }

    private static Stream<Arguments> provideRequestResponseByType() {
        return getNonJSONSupportedContentTypes().stream()
                .map(type -> paramResolver.getDownloadAllParamAndResult(type))
                .map(paramAndResult -> Arguments.of(paramAndResult));
    }
}
