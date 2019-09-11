package com.beligum.blocks.ontologies.commons.persistance;

import com.beligum.base.server.R;
import com.mchange.v2.c3p0.DataSources;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import javax.ws.rs.core.UriBuilder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteJDBCDriverConnection {

    private final static DataSource pooled;

    static {
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(false);
            config.setPageSize(4096); //in bytes
            config.setCacheSize(2000); //number of pages
            config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
            config.setJournalMode(SQLiteConfig.JournalMode.OFF);
            SQLiteDataSource unpooled = new SQLiteDataSource(config);
            String dbPath = UriBuilder.fromPath(R.configuration().getString("wikidata-database-path")).path("wikidata.db").build().toString();
            unpooled.setUrl("jdbc:sqlite://"+dbPath);
            pooled = DataSources.pooledDataSource(unpooled);
            createResourceinfoTable();
            createWikidatamodelTable();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private static class LazyHolder
    {
        private static final SQLiteJDBCDriverConnection INSTANCE = new SQLiteJDBCDriverConnection();
    }

    public static SQLiteJDBCDriverConnection getInstance()
    {
        return LazyHolder.INSTANCE;
    }
    /**
     * Create a table that holds the wikidata Model
     */
    public static void createWikidatamodelTable()
    {

        // SQL statement for creating a new table
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE TABLE IF NOT EXISTS `wikidatamodel` (\n");
        stringBuilder.append("  `id` varchar(100) NOT NULL,\n");
        stringBuilder.append("   `numericid` int(11) DEFAULT NULL,\n");
        stringBuilder.append("   `subject` varchar(200) NOT NULL,\n");
        stringBuilder.append("   `objectlabel` varchar(200) NOT NULL,\n");
        stringBuilder.append("   `objectlanguage` varchar(200) DEFAULT NULL,\n");
        stringBuilder.append("  `predicate` varchar(200) NOT NULL\n");
        stringBuilder.append(" )");
        String sql = stringBuilder.toString();

        try (Connection conn = pooled.getConnection();
                        Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    /**
     * Create a table that holds the resource info for the wikidata object
     */
    public static void createResourceinfoTable()
    {
        // SQL statement for creating a new table
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE TABLE IF NOT EXISTS `wikiresourceinfo` (\n");
        stringBuilder.append("  `id` varchar(100) NOT NULL,\n");
        stringBuilder.append("   `lang` varchar(45) DEFAULT NULL,\n");
        stringBuilder.append("   `datalabel` varchar(200) DEFAULT NULL,\n");
        stringBuilder.append("   `pic` varchar(2000) DEFAULT NULL,\n");
        stringBuilder.append("  `resourcetype` varchar(150) NOT NULL,\n");
        stringBuilder.append("  `sitelink` varchar(2000) DEFAULT NULL\n");
        stringBuilder.append(" )");
        String sql = stringBuilder.toString();

        try (Connection conn = pooled.getConnection();
                        Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException
    {
        return this.pooled.getConnection();
    }

}
