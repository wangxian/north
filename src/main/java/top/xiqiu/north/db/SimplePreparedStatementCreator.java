package top.xiqiu.north.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 简单实现 从 Connection 获得 PreparedStatement
 */
public class SimplePreparedStatementCreator implements PreparedStatementCreator {
    private PreparedStatementSetter setter;
    private String sql;

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
}
