package com.badfic.philbot.config;

import java.util.List;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories(basePackages = "com.badfic.philbot")
class JdbcConfiguration extends AbstractJdbcConfiguration {

    @Setter(onMethod_ = {@Autowired})
    private List<Converter<?, ?>> converters;

    @Override
    protected List<?> userConverters() {
        return converters;
    }
}
