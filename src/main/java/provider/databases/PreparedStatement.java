package provider.databases;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class PreparedStatement {
    private Database db;
    private String statement;
    private Map<Integer, Object> args = new LinkedHashMap<>();

    public PreparedStatement(String statement, Database db) {
        this.statement = statement;
        this.db = db;
    }

    public void setInteger(int pos, Integer integer) {
        args.put(pos, integer);
    }

    public void setString(int pos, String string) {
        args.put(pos, string);
    }

    public void setLong(int pos, Long lo) {
        args.put(pos, lo);
    }

    public void setFloat(int pos, Float flt) {
        args.put(pos, flt);
    }

    public void setDate(int pos, Date date) {
        args.put(pos, date);
    }

    public int executeUpdate() throws SQLException {
        return prepare().executeUpdate();
    }

    public ResultSet executeQuery() throws SQLException {
        return new ResultSet(prepare().executeQuery());
    }

    private java.sql.PreparedStatement prepare() throws SQLException {
        java.sql.PreparedStatement ps = db.getPS(statement);

        for (Map.Entry<Integer, Object> entry : args.entrySet()) {
            if (entry.getValue() instanceof Integer)
                ps.setInt(entry.getKey(), (int) entry.getValue());

            if (entry.getValue() instanceof String)
                ps.setString(entry.getKey(), (String) entry.getValue());

            if (entry.getValue() instanceof Long)
                ps.setLong(entry.getKey(), (Long) entry.getValue());

            if (entry.getValue() instanceof Float)
                ps.setFloat(entry.getKey(), (Float) entry.getValue());

            if (entry.getValue() instanceof Date)
                ps.setDate(entry.getKey(), new java.sql.Date(((Date) entry.getValue()).getTime()));
        }

        return ps;
    }



}
