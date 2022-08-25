package top.xiqiu.north.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 批量更新 或 删除接口
 */
public interface BatchPreparedStatementSetter {
    void setValues(PreparedStatement preparedStatement, int index) throws SQLException;

    int size();
}
