package com.axon.config;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.CommandGatewayFactory;
import org.axonframework.common.jdbc.ConnectionProvider;
import org.axonframework.common.jdbc.DataSourceConnectionProvider;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.TokenSchema;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.serialization.Serializer;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import javax.sql.DataSource;

@Slf4j
@Configuration
@AutoConfigureAfter({ AxonAutoConfiguration.class })
public class AxonConfig {


    @Bean
    public CommandGateway sCSmartPayCommandGateway(org.axonframework.config.Configuration configurer) {
        CommandBus commandBus = configurer.commandBus();
        commandBus.registerHandlerInterceptor(new LoggingInterceptor<>());
        CommandGatewayFactory gatewayFactory = CommandGatewayFactory.builder().commandBus(commandBus).build();
        log.info("++++++++++++++++ command gateway++++++++++++++++++");
        return gatewayFactory.createGateway(CommandGateway.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropagatingErrorHandler propagatingErrorHandler() {
        log.info("+++++++++++++++++Configuring PropagatingErrorHandler++++++++++++++++++++++");
        return PropagatingErrorHandler.instance();
    }

    @Autowired
    public void configureEventProcessing(Configurer configurer) {
        log.info("+++++++++++++++++Configuring LoggingInterceptor event HandlerInterceptor++++++++++++++++++++++");
        configurer.eventProcessing().registerDefaultHandlerInterceptor((cfg, s) -> new LoggingInterceptor<>());
    }

    @Bean
    @ConditionalOnProperty( name = {"token-store.jdbc.enable"}, havingValue = "false", matchIfMissing = true)
    public JpaTokenStore jpaTokenStore(@Autowired EntityManagerProvider entityManagerProvider, Serializer genericSerializer) {
        log.info("+++++++++++++++++Configuring JpaTokenStore++++++++++++++++++++++");
        return JpaTokenStore.builder().serializer(genericSerializer).entityManagerProvider(entityManagerProvider).build();
    }

    @Bean
    @ConditionalOnProperty(name = "token-store.jdbc.enable", havingValue = "true")
    public TokenStore tokenStore(DataSource dataSource, @Qualifier("serializer") Serializer serializer) {
        ConnectionProvider connectionProvider = new DataSourceConnectionProvider(dataSource);
        log.info("+++++++++++++++++Configuring JDBCTokenStore++++++++++++++++++++++");
        Class<?> contentTypeClass = byte[].class;

        return JdbcTokenStore.builder()
                             .connectionProvider(connectionProvider)
                             .serializer(serializer)
                             .claimTimeout(Duration.ofSeconds(10))
                             .schema(tokenSchema())
                             .contentType(contentTypeClass)
                             .nodeId(ManagementFactory.getRuntimeMXBean()
                                                      .getName())
                             .build();

    }

    private TokenSchema tokenSchema() {
        return TokenSchema.builder()
                          .setTokenTable("token_entry_jdbc")
                          .setProcessorNameColumn("processor_name")
                          .setSegmentColumn("segment")
                          .setTokenColumn("token")
                          .setTokenTypeColumn("token_type")
                          .setTimestampColumn("timestamp")
                          .setOwnerColumn("owner")
                          .build();
    }

}
