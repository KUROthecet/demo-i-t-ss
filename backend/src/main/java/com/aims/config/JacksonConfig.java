package com.aims.config;

import com.aims.media.MediaTypeDefinition;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer mediaSubtypeRegistrar(List<MediaTypeDefinition> definitions) {
        return builder -> builder.postConfigurer(mapper ->
            definitions.forEach(def ->
                mapper.registerSubtypes(new NamedType(def.getEntityClass(), def.getCategory()))
            )
        );
    }
}
