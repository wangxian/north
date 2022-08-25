package top.xiqiu.north.db;


import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用于处理 ResultSet 的每一行结果，无返回值
 * 该回调方法中无需执行 rs.next()，用户只需按行获取数据然后处理即可
 */
public interface RowCallbackHandler {
    void processRow(ResultSet rs) throws SQLException;
}
