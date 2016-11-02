/*
 * Copyright 2016 TransFICC Ltd.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.
 */
package com.transficc.tools.feedback.dao;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import javax.sql.DataSource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
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
