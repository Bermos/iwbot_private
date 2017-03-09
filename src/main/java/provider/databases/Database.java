package provider.databases;

import iw_bot.LogUtil;
import provider.DataProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private String dbName;
    private Connection connection;

    /**
     * Connects the Database object to the specified database using info from the data.json
     *
     * @param dbName the name as specified in the data.json file
     * @return true if the connection was successful
     */
    public boolean connect(String dbName) {
        this.dbName = dbName;
        DataProvider.ConData conInfo = DataProvider.getConData(dbName);

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + conInfo.IP +
                        "/" + conInfo.DB,
                    conInfo.US,
                    conInfo.PW);

            return connection.isValid(1000);
        } catch (SQLException e) {
            LogUtil.logErr(e);
            return false;
        }
    }

    /**
     * Create a preparedStatement to query/update info in the database
     *
     * @param statement The statement string with '?' as placeholders
     * @return PreparedStatement for use in the program
     */
    public PreparedStatement prepareStatement(String statement) {
        try {
            if (!connection.isValid(1000))
                connect(dbName);
        } catch (SQLException ignored) {}

        return new PreparedStatement(statement, this);
    }

    /**
     * Creates a PreparedStatement for my PS class to use internally
     *
     * @param statement The statement string with '?' as placeholders
     * @return PreparedStatement for use in my PreparedStatement class only!
     * @throws SQLException if a database access error occurs
     */
    java.sql.PreparedStatement getPS(String statement) throws SQLException {
        return connection.prepareStatement(statement);
    }
}
