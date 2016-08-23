package com.transficc.tools.feedback.dao;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public abstract class DaoTest
{
    private final DataSource dataSource;
    private final DataSourceTransactionManager transactionManager;
    private final File dataSourceFile;
    private TransactionStatus transactionStatus;

    DaoTest()
    {
        try
        {
            this.dataSourceFile = File.createTempFile("data/", "feedback");
        }
        catch (final IOException e)
        {
            throw new UncheckedIOException(e);
        }
        this.dataSource = createDataSource();
        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.migrate();
        this.transactionManager = new DataSourceTransactionManager(dataSource);
    }

    DataSource getDataSource()
    {
        return dataSource;
    }

    @Before
    public void beginning()
    {
        final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionStatus = transactionManager.getTransaction(transactionDefinition);
    }

    @After
    public void cleanup()
    {
        transactionManager.rollback(transactionStatus);
        dataSourceFile.delete();
    }

    private DataSource createDataSource()
    {
        return JdbcConnectionPool.create("jdbc:h2:" + dataSourceFile.toString(), "sa", "sa");
    }
}
