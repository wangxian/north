package top.xiqiu.north.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用于结果集数据提取
 * 用户必须处理整个结果集
 */
public interface ResultSetExtractor<T> {
    T extractData(ResultSet rs) throws SQLException;
}
