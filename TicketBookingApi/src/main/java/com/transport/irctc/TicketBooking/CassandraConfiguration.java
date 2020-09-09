package com.transport.irctc.TicketBooking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableCassandraRepositories
public class CassandraConfiguration extends AbstractCassandraConfiguration{

    @Autowired
    private Environment env;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    protected String getKeyspaceName() {
        return env.getProperty("cassandra.keyspace");
    }

    @Override
    protected String getContactPoints() {
        return env.getProperty("cassandra.contactpoints");
    }

    @Override
    protected int getPort() {
        return Integer.parseInt(env.getProperty("cassandra.port"));
    }

    @Override
    protected String getLocalDataCenter() {
        return "datacenter1";
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {

        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
                .ifNotExists()
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withSimpleReplication(1);
        return Arrays.asList(specification);
    }

    //    @Override
//    protected List<String> getStartupScripts() {
//        return super.getStartupScripts();
//    }

//    @SneakyThrows
//    @Override
//    protected SessionBuilderConfigurer getSessionBuilderConfigurer() {
//
//        Resource resource = resourceLoader.getResource(env.getProperty("cassandra.CloudSecureConnectBundle"));
//        InputStream cloudConfigInputStream = resource.getInputStream();
//        return (cqlSessionBuilder) -> {
//            cqlSessionBuilder.withLocalDatacenter("datacenter1");
////                    .withCloudSecureConnectBundle(cloudConfigInputStream)
////                    .withAuthCredentials(env.getProperty("cassandra.username"), env.getProperty("cassandra.password"))
////                    .withKeyspace(getKeyspaceName());
//            return cqlSessionBuilder;
//        };
//    }


//    @Bean
//    @Primary
//    public CqlSession cassandraSession() throws IOException {
//        CqlSessionFactoryBean cqlSessionFactoryBean =new CqlSessionFactoryBean();
//        Resource resource = resourceLoader.getResource(env.getProperty("cassandra.CloudSecureConnectBundle"));
//        InputStream cloudConfigInputStream = resource.getInputStream();
//        return new CqlSessionBuilder()
//                .withCloudSecureConnectBundle(cloudConfigInputStream)
//                .withAuthCredentials(env.getProperty("cassandra.username"), env.getProperty("cassandra.password"))
//                .withKeyspace(getKeyspaceName())
//                .build();
//    }
}
