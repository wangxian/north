package top.xiqiu.north.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用于将 ResultSet 每行数据转换为需要的类型
 */
public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
