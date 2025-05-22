package com.example.academia.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module; // o hibernate5 si usas Hibernate 5
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernateModule() {
        Hibernate6Module module = new Hibernate6Module();
        // Opcional: configura para no serializar propiedades transitorias
        module.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);
        return module;
    }
}