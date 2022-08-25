package top.xiqiu.north.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 通过回调获取 Connection 的 PreparedStatement
 * 由用户使用该 Connection 创建相关的 PreparedStatement
 */
public interface PreparedStatementCreator {
    PreparedStatement createPreparedStatement(Connection conn) throws SQLException;
}
