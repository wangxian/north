package top.xiqiu.north.db;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

/**
 * 数据库简单操作类
 */
public class DbTemplate {
    private DataSource dataSource;
    private Connection connection;

    private Statement statement;
    private PreparedStatement preparedStatement;

    private ResultSet resultSet;

    public DbTemplate() {
    }

    public DbTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 设置数据源
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取默认 PreparedStatement
     */
    private void getDefaultPreparedStatement(String sql, Object[] args, int[] argTypes) throws SQLException {
        connection = this.dataSource.getConnection();

        final ArgsTypePreparedStatementSetter setter = new ArgsTypePreparedStatementSetter(args, argTypes);
        preparedStatement = new SimplePreparedStatementCreator(setter, sql).createPreparedStatement(connection);
    }

    /**
     * 查询记录集
     */
    private ResultSet query(String sql, Object[] args, int[] argTypes) throws SQLException {
        getDefaultPreparedStatement(sql, args, argTypes);
        return preparedStatement.executeQuery(sql);
    }

    /**
     * 清理使用过的操作资源语柄
     */
    private void cleanUp() {
        try {
            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }

            if (preparedStatement != null) {
                preparedStatement.close();
                preparedStatement = null;
            }

            if (statement != null) {
                statement.close();
                statement = null;
            }

            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行更新、删除操作，返回影响行数
     *
     * @param sql SQL语句
     * @return 影响行数
     */
    public int update(String sql) {
        return this.update(sql, null, null);
    }

    /**
     * 执行更新、删除操作，返回影响行数
     *
     * @param sql  SQL语句
     * @param args 形参的展开列表
     * @return 影响行数
     */
    public int update(String sql, Object... args) {
        return this.update(sql, args, null);
    }

    /**
     * 更新、删除操作，返回受影响的行数
     */
    public int update(String sql, Object[] args, int[] argTypes) {
        int affectedRows = 0;

        try {
            getDefaultPreparedStatement(sql, args, argTypes);
            affectedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 已到调用终点，清理资源占用
            this.cleanUp();
        }

        return affectedRows;
    }

    /**
     * 更新、删除操作，返回受影响的行数
     */
    public int update(PreparedStatementCreator preparedStatementCreator) {
        int affectedRows = 0;

        try {
            connection        = dataSource.getConnection();
            preparedStatement = preparedStatementCreator.createPreparedStatement(connection);
            affectedRows      = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 已到调用终点，清理资源占用
            this.cleanUp();
        }

        return affectedRows;
    }

    /**
     * 更新、删除操作，返回受影响的行数，可获取插入的主键ID
     * <p>
     * 用法：
     * keyHolder = new GeneratedKeyHolder();
     * this.update(preparedStatementCreator, keyHolder)
     * int id = keyHolder.getKey().intValue()
     */
    public int update(PreparedStatementCreator preparedStatementCreator, KeyHolder keyHolder) {
        int affectedRows = 0;

        try {
            connection        = dataSource.getConnection();
            preparedStatement = preparedStatementCreator.createPreparedStatement(connection);
            affectedRows      = preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            int id = 0;
            if (resultSet.next()) {
                id = resultSet.getInt(1);
            }

            // 会写插入的主键ID
            keyHolder.setKey(id);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 已到调用终点，清理资源占用
            this.cleanUp();
        }

        return affectedRows;
    }

    /**
     * 更新、删除操作，返回受影响的行数
     */
    public int update(String sql, PreparedStatementSetter setter) {
        int affectedRows = 0;

        try {
            connection        = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            setter.setValues(preparedStatement);
            affectedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 已到调用终点，清理资源占用
            this.cleanUp();
        }

        return affectedRows;
    }

    /**
     * 执行批量更新，参数为 String[] 数组
     * 性能好
     */
    public int[] batchUpdate(final String[] sql) {
        int[] affectedRowsArray = null;

        try {
            connection = dataSource.getConnection();
            statement  = connection.createStatement();

            for (String s : sql) {
                statement.addBatch(s);
            }

            affectedRowsArray = statement.executeBatch();
            statement.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }

        return affectedRowsArray;
    }

    /**
     * 执行批量更新 - 批量更新 - 预处理SQL - 回调的方式
     */
    public int[] batchUpdate(final String sql, final BatchPreparedStatementSetter batchPreparedStatementSetter) {
        int[] affectedRowsArray = null;

        try {
            connection        = this.dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            for (int i = 0; i < batchPreparedStatementSetter.size(); i++) {
                batchPreparedStatementSetter.setValues(preparedStatement, i);
                preparedStatement.addBatch();
            }

            affectedRowsArray = preparedStatement.executeBatch();
            preparedStatement.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }

        return affectedRowsArray;
    }

    /**
     * 批量更新 - 批量更新（指定参数类型）- 预处理SQL
     *
     * @param args 参数列表，注意：Object[] 为一次SQL需要的参数，故外层还有一个 List<> 结构
     */
    public int[] batchUpdate(final String sql, List<Object[]> args, int[] argTypes) {
        return this.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int index) throws SQLException {
                Object[] argsItem = args.get(index);
                ArgsTypePreparedStatementSetter setter = new ArgsTypePreparedStatementSetter(argsItem, argTypes);
                setter.setValues(preparedStatement);
            }

            @Override
            public int size() {
                return args.size();
            }
        });
    }

    /**
     * 批量更新 - 不指定参数类型 - 预处理SQL
     */
    public int[] batchUpdate(final String sql, List<Object[]> args) {
        return this.batchUpdate(sql, args, null);
    }

    /**
     * 用于执行任何SQL语句（不确定 SQL 是 DDL/DCL/DML）
     * 一般用于执行DDL语句，无返回值
     */
    public void execute(String sql) {
        try {
            connection = dataSource.getConnection();
            statement  = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }
    }

    /**
     * 返回一个 JavaBean
     */
    public <T> T queryForObject(String sql, Class<T> requiredType) {
        return queryForObject(sql, null, requiredType);
    }

    public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) {
        return null;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        return null;
    }

    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        return null;
    }

    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) {
        return null;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return null;
    }

    /**
     * 返回一个 List<Map<String, Object>>
     */
    public void queryForList() {

    }

    public void queryForMap() {

    }

    /**
     * 返回一个结果集然后调用 getString 或 getInt 等去取值
     */
    public void queryForRowSet() {

    }

}
