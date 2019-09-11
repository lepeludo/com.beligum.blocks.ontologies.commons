package com.beligum.blocks.ontologies.commons.persistance;

public class SQLLiteConnector
{

    private static SQLiteJDBCDriverConnection connection;
    /**
     * Do not use the constructor.
     */
    private SQLLiteConnector(){
        throw new RuntimeException("not implemented");
    }

    private static class LazyHolder
    {
        private static final SQLLiteConnector INSTANCE = new SQLLiteConnector();
    }

    public static SQLLiteConnector getInstance()
    {
        if(connection == null){
            connection = new SQLiteJDBCDriverConnection();
        }
        return LazyHolder.INSTANCE;
    }


}
