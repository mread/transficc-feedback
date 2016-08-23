package com.transficc.tools.feedback.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class TestFailureDao
{
    private final JdbcTemplate jdbcTemplate;

    public TestFailureDao(final DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
