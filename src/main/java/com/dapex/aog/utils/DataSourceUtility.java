package com.dapex.aog.utils;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * Created by mmacpher on 9/12/18.
 */
public class DataSourceUtility {

    public DataSource getNewDataSource() {
        PoolProperties p = new PoolProperties();
        p.setTestOnBorrow(true);
        p.setMaxActive(50);
        p.setMaxIdle(50);

        DataSource d = new DataSource();
        d.setPoolProperties(p);
        return d;
    }

}
