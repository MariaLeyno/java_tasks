package org.tasks.config;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) throws IOException {
        YamlPropertiesFactoryBean yamlBean = new YamlPropertiesFactoryBean();
        yamlBean.setResources(resource.getResource());
        Properties properties = yamlBean.getObject();
        return new PropertiesPropertySource(resource.getResource().getFilename(), properties);
    }
}
