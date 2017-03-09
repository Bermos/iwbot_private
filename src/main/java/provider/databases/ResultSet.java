package provider.databases;

import java.sql.SQLException;

public class ResultSet {
    java.sql.ResultSet rs;

    public ResultSet(java.sql.ResultSet rs) {
        this.rs = rs;
    }

    /**
     * Moves to the next row in the ResultSet
     *
     * @return true if there is a next row, false if there isn't
     * @throws SQLException if a database access error occurs or this method is called on a closed result set
     */
    public boolean next() throws SQLException {
        return rs.next();
    }

    /**
     * Get the value of the specified column
     *
     * @param columnLabel the name of the column as it is in the database
     * @return value for that row/column
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public int getInt(String columnLabel) throws SQLException {
        return rs.getInt(columnLabel);
    }

    /**
     * Get the value of the specified column
     *
     * @param columnLabel the name of the column as it is in the database
     * @return value for that row/column
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public String getString(String columnLabel) throws SQLException {
        return rs.getString(columnLabel);
    }

    /**
     * Get the value of the specified column
     *
     * @param columnLabel the name of the column as it is in the database
     * @return value for that row/column
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public double getDouble(String columnLabel) throws SQLException {
        return rs.getDouble(columnLabel);
    }

    /**
     * Get the MetaData from the ResultSet
     *
     * @return MetaData object
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        return new ResultSetMetaData(rs.getMetaData());
    }

    /**
     * Closes the ResultSet, probably not necessary
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public void close() throws SQLException {
        rs.close();
    }
}
