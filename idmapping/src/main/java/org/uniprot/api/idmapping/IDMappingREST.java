package org.uniprot.api.idmapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.uniprot.api.rest.output.header.HttpCommonHeaderConfig;

/**
 * Starts the ID Mapping service.
 *
 * <p>Created 15/02/2021
 *
 * @author Edd
 */
@SpringBootApplication
@Import({HttpCommonHeaderConfig.class})
@ComponentScan(basePackages = {"org.uniprot.api.idmapping", "org.uniprot.api.rest"})
public class IDMappingREST {
    public static void main(String[] args) {
        SpringApplication.run(IDMappingREST.class, args);
    }
}
