package org.uniprot.api.idmapping.output;

import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.SerializationUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.uniprot.api.common.concurrency.TaskExecutorProperties;
import org.uniprot.api.idmapping.model.IdMappingStringPair;
import org.uniprot.api.idmapping.model.StringUniProtKBEntryPair;
import org.uniprot.api.rest.output.context.MessageConverterContext;
import org.uniprot.api.rest.output.context.MessageConverterContextFactory;
import org.uniprot.api.rest.output.converter.ErrorMessageConverter;
import org.uniprot.api.rest.output.converter.ErrorMessageXMLConverter;
import org.uniprot.api.rest.output.converter.JsonMessageConverter;
import org.uniprot.core.json.parser.uniprot.UniprotKBJsonConfig;
import org.uniprot.store.config.UniProtDataType;
import org.uniprot.store.config.returnfield.config.ReturnFieldConfig;
import org.uniprot.store.config.returnfield.factory.ReturnFieldConfigFactory;
import org.uniprot.store.config.returnfield.model.ReturnField;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created 21/08/18
 *
 * @author Edd
 */
@Configuration
@ConfigurationProperties(prefix = "download")
@Getter
@Setter
public class MessageConverterConfig {
    private TaskExecutorProperties taskExecutor = new TaskExecutorProperties();

    @Bean
    public ThreadPoolTaskExecutor downloadTaskExecutor(
            ThreadPoolTaskExecutor configurableTaskExecutor) {
        configurableTaskExecutor.setCorePoolSize(taskExecutor.getCorePoolSize());
        configurableTaskExecutor.setMaxPoolSize(taskExecutor.getMaxPoolSize());
        configurableTaskExecutor.setQueueCapacity(taskExecutor.getQueueCapacity());
        configurableTaskExecutor.setKeepAliveSeconds(taskExecutor.getKeepAliveSeconds());
        configurableTaskExecutor.setAllowCoreThreadTimeOut(taskExecutor.isAllowCoreThreadTimeout());
        configurableTaskExecutor.setWaitForTasksToCompleteOnShutdown(
                taskExecutor.isWaitForTasksToCompleteOnShutdown());
        return configurableTaskExecutor;
    }

    @Bean
    public ThreadPoolTaskExecutor configurableTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    public WebMvcConfigurer extendedMessageConverters() {
        return new WebMvcConfigurer() {
            @Override
            public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                converters.add(new ErrorMessageConverter());
                converters.add(new ErrorMessageXMLConverter()); // to handle xml error messages

                ReturnFieldConfig returnFieldConfig = getUniProtKBIdMappingReturnFieldConfig();

                JsonMessageConverter<StringUniProtKBEntryPair> kbMappingPairJsonMessageConverter =
                        new JsonMessageConverter<>(
                                UniprotKBJsonConfig.getInstance().getSimpleObjectMapper(),
                                StringUniProtKBEntryPair.class,
                                returnFieldConfig);
                converters.add(0, kbMappingPairJsonMessageConverter);

                JsonMessageConverter<IdMappingStringPair> idMappingPairJsonMessageConverter =
                        new JsonMessageConverter<>(
                                new ObjectMapper(), IdMappingStringPair.class, null);
                converters.add(1, idMappingPairJsonMessageConverter);
            }
        };
    }

    @Bean("stringPairMessageConverterContextFactory")
    public MessageConverterContextFactory<IdMappingStringPair>
            stringPairMessageConverterContextFactory() {
        MessageConverterContextFactory<IdMappingStringPair> contextFactory =
                new MessageConverterContextFactory<>();

        asList(idMappingContext(APPLICATION_JSON))
                .forEach(contextFactory::addMessageConverterContext);

        return contextFactory;
    }

    @Bean("stringUniProtKBEntryPairMessageConverterContextFactory")
    public MessageConverterContextFactory<StringUniProtKBEntryPair>
            stringUniProtKBEntryPairMessageConverterContextFactory() {
        MessageConverterContextFactory<StringUniProtKBEntryPair> contextFactory =
                new MessageConverterContextFactory<>();

        asList(kbContext(APPLICATION_JSON)).forEach(contextFactory::addMessageConverterContext);

        return contextFactory;
    }

    private MessageConverterContext<IdMappingStringPair> idMappingContext(MediaType contentType) {
        return MessageConverterContext.<IdMappingStringPair>builder()
                .resource(MessageConverterContextFactory.Resource.IDMAPPING_PIR)
                .contentType(contentType)
                .build();
    }

    private MessageConverterContext<StringUniProtKBEntryPair> kbContext(MediaType contentType) {
        return MessageConverterContext.<StringUniProtKBEntryPair>builder()
                .resource(MessageConverterContextFactory.Resource.UNIPROTKB)
                .contentType(contentType)
                .build();
    }

    private ReturnFieldConfig getUniProtKBIdMappingReturnFieldConfig() {
        ReturnFieldConfig uniProtKBReturnField =
                ReturnFieldConfigFactory.getReturnFieldConfig(UniProtDataType.UNIPROTKB);
        // clone it to avoid messing with the global constant
        ReturnFieldConfig idMappingReturnConfig =
                (ReturnFieldConfig) SerializationUtils.clone(uniProtKBReturnField);
        List<ReturnField> returnFields =
                idMappingReturnConfig.getReturnFields().stream()
                        .map(this::updatePath)
                        .collect(Collectors.toList());
        idMappingReturnConfig.getReturnFields().clear();
        ReturnField fromField = getFromReturnField();
        idMappingReturnConfig.getReturnFields().add(fromField);// add required from field on the fly
        idMappingReturnConfig.getReturnFields().addAll(returnFields);
        return idMappingReturnConfig;
    }
    // prefix to. in the return field path
    private ReturnField updatePath(ReturnField returnField) {
        ReturnField updatedReturnField = returnField;
        List<String> oldPaths =
                returnField.getPaths().stream()
                        .map(path -> "to." + path)
                        .collect(Collectors.toList());
        updatedReturnField.setPaths(oldPaths);
        return updatedReturnField;
    }

    // add a required field from to be returned in the response all the time
    private ReturnField getFromReturnField() {
        ReturnField fromReturnField = new ReturnField();
        fromReturnField.setName("from");
        fromReturnField.setId("from");
        fromReturnField.addPath("from");
        fromReturnField.setIsRequiredForJson(true);
        return fromReturnField;
    }
}
