package provider.databases;

import java.sql.SQLException;

public class ResultSetMetaData {
    java.sql.ResultSetMetaData rsmt;

    /**
     * Create the custom ResultSetMetaData object from
     * @param rsmt the original RSMT from java.sql
     */
    ResultSetMetaData(java.sql.ResultSetMetaData rsmt) {
        this.rsmt = rsmt;
    }

    /**
     * Get the number of columns there are in this result set
     * @return number of columns
     * @throws SQLException something went wrong, dhu
     */
    public int getColumnCount() throws SQLException {
        return rsmt.getColumnCount();
    }

    /**
     * Get the name corresponding to the specified column index
     * @param column index, starting at 1
     * @return the name of the column
     * @throws SQLException something went wrong, doh
     */
    public String getColumnNames(int column) throws SQLException {
        return rsmt.getColumnName(column);
    }
}
