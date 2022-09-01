package top.xiqiu.north.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 简单实现 从 Connection 获得 PreparedStatement
 */
public class SimplePreparedStatementCreator implements PreparedStatementCreator {
    private final PreparedStatementSetter setter;
    private final String sql;

    public SimplePreparedStatementCreator(PreparedStatementSetter setter, String sql) {
        this.setter = setter;
        this.sql    = sql;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        setter.setValues(preparedStatement);
        return preparedStatement;
    }

    public PreparedStatement createPreparedStatement(Connection conn, int autoGenKeyIndex) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(sql, autoGenKeyIndex);
        setter.setValues(preparedStatement);
        return preparedStatement;
    }
}
