package com.transficc.tools.feedback.dao;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IterationDaoTest extends DaoTest
{
    private IterationDao dao;

    @Before
    public void setUp() throws Exception
    {
        dao = new IterationDao(getDataSource());
    }

    @Test
    public void shouldUpdateIterationValue()
    {
        final String expectedIteration = "it12312";

        final String actualIteration = dao.updateIteration(expectedIteration);

        assertThat(actualIteration, is(expectedIteration));
    }
}