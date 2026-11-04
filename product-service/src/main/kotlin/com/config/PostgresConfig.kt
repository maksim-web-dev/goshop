package com.config

import com.config.props.PostgresProps
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.lang.NonNull
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
@EnableJpaRepositories(
    basePackages = ["com.data.entity"],
    entityManagerFactoryRef = "pgEntityManagerFactory",
    transactionManagerRef = "pgTransactionManager"
)
class PostgresConfig @Autowired constructor(
    private val pgProps: PostgresProps
) {

    @Primary
    @Bean(name = ["pgDataSource"])
    fun pgDataSource(): DataSource {

        val url = "jdbc:postgresql://${pgProps.host}:${pgProps.port}/${pgProps.db}" +
                "?sslmode=${pgProps.ssl}" +
                "&use_server_time_zone=true"

        return HikariDataSource(HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = url
            username = pgProps.user
            password = pgProps.pass
            maximumPoolSize = pgProps.poolMax
            minimumIdle = pgProps.poolMin
            connectionTimeout = pgProps.poolTimeoutMillis
            poolName = "HikariPG"
        })
    }


    @Primary
    @Bean(name = ["pgEntityManagerFactory"])
    fun pgEntityManagerFactory(
        @Qualifier("pgDataSource") dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val vendor = HibernateJpaVendorAdapter().apply {
            setGenerateDdl(false)
            setShowSql(pgProps.showSql)
        }
        val props = hashMapOf<String, Any?>(
            "hibernate.hbm2ddl.auto" to pgProps.ddlAuto,
            "hibernate.dialect" to "org.hibernate.dialect.PostgreSQLDialect",
        )
        return LocalContainerEntityManagerFactoryBean().apply {
            setPackagesToScan("zero44.crm.core.data.postgres")
            setJpaPropertyMap(props)
            setDataSource(dataSource)
            jpaVendorAdapter = vendor
            persistenceUnitName = "pg"
        }
    }

    @Primary
    @Bean(name = ["pgTransactionManager"])
    fun pgTransactionManager(
        @Qualifier("pgEntityManagerFactory") @NonNull emf: EntityManagerFactory
    ): PlatformTransactionManager = JpaTransactionManager(emf).apply {
        isRollbackOnCommitFailure = true
    }

    @Primary
    @Bean(name = ["pgJdbc"])
    fun pgJdbc(
        @Qualifier("pgDataSource") ds: DataSource
    ): JdbcTemplate = JdbcTemplate(ds)

    @Bean
    fun exceptionTranslator() = PersistenceExceptionTranslationPostProcessor()

}