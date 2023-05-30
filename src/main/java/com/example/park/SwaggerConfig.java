package com.example.park;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import static springfox.documentation.builders.PathSelectors.regex;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author manjunathr created at 01-06-2020
 * <p>
 * This is the Swagger confiuration Class used to generate the run time swagger
 */
@EnableSwagger2
@Configuration
public class SwaggerConfig {

    /**
     * This method will create the swagger group that contains all the end points in this application
     */
    @Bean
    public Docket all()
    {
        return new Docket(DocumentationType.SWAGGER_2).groupName("all").apiInfo(apiInfo()).select()
                .paths(regex("/rest/.*")).build();

    }

    /**
     * This is the private method which is used to fetch the API information of the swagger this will be
     * used to generate the swagger UI
     */
    private ApiInfo apiInfo()
    {
        return new ApiInfoBuilder().title("Park duration").version("1.0.0").build();
    }
}