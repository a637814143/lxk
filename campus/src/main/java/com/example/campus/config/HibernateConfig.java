package com.example.campus.config;

import java.util.Map;
import org.hibernate.cfg.Environment;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig implements HibernatePropertiesCustomizer {

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(Environment.HBM2DDL_AUTO, "none");
        hibernateProperties.put("jakarta.persistence.schema-generation.database.action", "none");
    }
}
