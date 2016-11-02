package com.transficc.tools.feedback.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class IterationDao
{
    private static final int ITERATION_KEY = 1;
    private final JdbcTemplate jdbcTemplate;

    public IterationDao(final DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public String updateIteration(final String iteration)
    {
        jdbcTemplate.update("UPDATE iteration SET name = ? WHERE id = ?", iteration, ITERATION_KEY);
        return getIteration();
    }

    public String getIteration()
    {
        return jdbcTemplate.queryForObject("SELECT name FROM iteration WHERE id = ?", String.class, ITERATION_KEY);
    }
}
