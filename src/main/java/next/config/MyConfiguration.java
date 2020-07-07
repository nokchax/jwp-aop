package next.config;

import core.annotation.Bean;
import core.annotation.ComponentScan;
import core.annotation.Configuration;
import core.jdbc.ConnectionHolder;
import core.jdbc.JdbcTemplate;
import core.mvc.tobe.HandlerConverter;
import core.mvc.tobe.support.*;
import next.security.LoginUserArgumentResolver;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

import static java.util.Arrays.asList;

@Configuration
@ComponentScan({ "next", "core" })
public class MyConfiguration {
    @Bean
    public DataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:~/jwp-framework;DB_CLOSE_DELAY=-1");
        ds.setUsername("sa");
        ds.setPassword("");

        ConnectionHolder.setDataSource(ds);
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate();
    }


    @Bean
    public HandlerConverter handlerConverter(ArgumentResolvers argumentResolvers) {
        HandlerConverter handlerConverter = new HandlerConverter();
        handlerConverter.setArgumentResolvers(argumentResolvers.getResolvers());
        handlerConverter.addArgumentResolver(loginUserArgumentResolver());
        return handlerConverter;
    }

    @Bean
    public ArgumentResolvers argumentResolvers() {
        return () -> asList(
                new HttpRequestArgumentResolver(),
                new HttpResponseArgumentResolver(),
                new RequestParamArgumentResolver(),
                new PathVariableArgumentResolver(),
                new ModelArgumentResolver()
        );
    }

    LoginUserArgumentResolver loginUserArgumentResolver() {
        return new LoginUserArgumentResolver();
    }
}
