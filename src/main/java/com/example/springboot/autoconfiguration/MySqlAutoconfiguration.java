package com.example.springboot.autoconfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.util.ClassUtils;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Properties;

@Configuration
@ConditionalOnClass(DataSource.class)
@PropertySource("classpath:mysql.properties")
public class MySqlAutoconfiguration {

    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnBean(name = "dataSource")
    @ConditionalOnMissingBean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.example.springboot.autoconfiguration");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        if (additionalProperties() != null) {
            em.setJpaProperties(additionalProperties());
        }
        return em;
    }

    @Bean
    @ConditionalOnProperty(
            name = "usemysql",
            havingValue = "local")
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/myDb?createDatabaseIfNotExist=true");
        dataSource.setUsername("mysqluser");
        dataSource.setPassword("mysqlpass");

        return dataSource;
    }

    @Bean(name = "dataSource")
    @ConditionalOnProperty(
            name = "usemysql",
            havingValue = "custom")
    @ConditionalOnMissingBean
    public DataSource dataSource2() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(env.getProperty("mysql.url"));
        dataSource.setUsername(env.getProperty("mysql.user") != null
                ? env.getProperty("mysql.user") : "");
        dataSource.setPassword(env.getProperty("mysql.pass") != null
                ? env.getProperty("mysql.pass") : "");

        return dataSource;
    }

    @Bean
    @ConditionalOnMissingBean(type = "JpaTransactionManager")
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @ConditionalOnResource(
            resources = "classpath:mysql.properties")
    @Conditional(HibernateCondition.class)
    Properties additionalProperties() {
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.hbm2ddl.auto",
                env.getProperty("mysql-hibernate.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect",
                env.getProperty("mysql-hibernate.dialect"));
        hibernateProperties.setProperty("hibernate.show_sql",
                env.getProperty("mysql-hibernate.show_sql") != null
                        ? env.getProperty("mysql-hibernate.show_sql") : "false");
        return hibernateProperties;
    }

    static class HibernateCondition extends SpringBootCondition {

        private static String[] CLASS_NAMES
                = { "org.hibernate.ejb.HibernateEntityManager",
                "org.hibernate.jpa.HibernateEntityManager" };

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context,
                                                AnnotatedTypeMetadata metadata) {

            ConditionMessage.Builder message
                    = ConditionMessage.forCondition("Hibernate");
            return Arrays.stream(CLASS_NAMES)
                    .filter(className -> ClassUtils.isPresent(className, context.getClassLoader()))
                    .map(className -> ConditionOutcome
                            .match(message.found("class")
                                    .items(ConditionMessage.Style.NORMAL, className)))
                    .findAny()
                    .orElseGet(() -> ConditionOutcome
                            .noMatch(message.didNotFind("class", "classes")
                                    .items(ConditionMessage.Style.NORMAL, Arrays.asList(CLASS_NAMES))));
        }
    }
}

