package top.xiqiu.north.db;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * 数据库简单操作类
 */
@SuppressWarnings("unchecked")
public class DbTemplate {
    private final DataSource dataSource;
    private Connection connection;

    private Statement statement;
    private PreparedStatement preparedStatement;

    private ResultSet resultSet;

    public DbTemplate() {
        this.dataSource = DataSourceInitializer.initDataSource();
    }

    // public DbTemplate(DataSource dataSource) {
    //     this.dataSource = dataSource;
    // }
    //
    // /**
    //  * 设置数据源
    //  */
    // public void setDataSource(DataSource dataSource) {
    //     this.dataSource = dataSource;
    // }

    /**
     * 获取默认 PreparedStatement
     */
    private void getDefaultPreparedStatement(String sql, Object[] args, int[] argTypes) throws SQLException {
        connection = this.dataSource.getConnection();

        final ArgsTypePreparedStatementSetter setter = new ArgsTypePreparedStatementSetter(args, argTypes);
        preparedStatement = new SimplePreparedStatementCreator(setter, sql).createPreparedStatement(connection);
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
                // System.out.println("-------- close connection");
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
     * db::base
     */
    public int update(String sql, Object[] args, int[] argTypes) {
        int affectedRows = 0;

        try {
            // 如果没有预处理参数，则直接调用 statement 即可
            if (args == null || args.length == 0) {
                connection   = dataSource.getConnection();
                statement    = connection.createStatement();
                affectedRows = statement.executeUpdate(sql);
            } else {
                getDefaultPreparedStatement(sql, args, argTypes);
                affectedRows = preparedStatement.executeUpdate();
            }
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
     * db::base
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
     * db::base
     * <p>
     * **用法：**
     * keyHolder = new GeneratedKeyHolder();
     * this.update(preparedStatementCreator, keyHolder)
     * int id = keyHolder.getKey().intValue()
     */
    public int update(PreparedStatementCreator preparedStatementCreator, KeyHolder keyHolder) {
        int affectedRows = 0;

        try {
            connection        = dataSource.getConnection();
            preparedStatement = preparedStatementCreator.createPreparedStatement(connection, Statement.RETURN_GENERATED_KEYS);
            affectedRows      = preparedStatement.executeUpdate();

            resultSet = preparedStatement.getGeneratedKeys();
            int id = 0;
            if (resultSet.next()) {
                id = resultSet.getInt(1);
            }

            // 回写新插入的主键ID
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
     * db::base
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
     * db:base
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
     * db:base
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
     * @param batchArgs 参数列表，注意：Object[] 为一次SQL需要的参数，故外层还有一个 List &lt;&gt; 结构
     */
    public int[] batchUpdate(final String sql, List<Object[]> batchArgs, int[] argTypes) {
        return this.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int index) throws SQLException {
                Object[] args = batchArgs.get(index);
                ArgsTypePreparedStatementSetter setter = new ArgsTypePreparedStatementSetter(args, argTypes);
                setter.setValues(preparedStatement);
            }

            @Override
            public int size() {
                return batchArgs.size();
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
     * db::base
     */
    public void execute(String sql) {
        System.out.printf("数据库操作，执行SQL = %s\n", sql);
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
     * 查询记录集
     * db:base
     */
    private ResultSet query(String sql, Object[] args, int[] argTypes) throws SQLException {
        System.out.printf("数据库操作，执行SQL = %s, 参数 = %s\n", sql, Arrays.toString(args));
        // 如果没有预处理参数，则直接调用 statement 即可
        if (args == null || args.length == 0) {
            connection = dataSource.getConnection();
            statement  = connection.createStatement();
            resultSet  = statement.executeQuery(sql);
        } else {
            getDefaultPreparedStatement(sql, args, argTypes);
            resultSet = preparedStatement.executeQuery();
        }

        return resultSet;
    }

    /**
     * 查询记录集
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return this.query(new SimplePreparedStatementCreator(new ArgsTypePreparedStatementSetter(null, null), sql), rowMapper);
    }

    /**
     * 查询记录集
     */
    public void query(String sql, RowCallbackHandler rowCallbackHandler) {
        this.query(new SimplePreparedStatementCreator(new ArgsTypePreparedStatementSetter(null, null), sql), rowCallbackHandler);
    }

    /**
     * 查询记录集
     */
    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor) {
        return this.query(sql, null, null, resultSetExtractor);
    }

    /**
     * 查询记录集
     */
    public <T> T query(String sql, PreparedStatementSetter setter, ResultSetExtractor<T> resultSetExtractor) {
        return this.query(new SimplePreparedStatementCreator(setter, sql), resultSetExtractor);
    }

    /**
     * 查询记录集
     */
    public <T> T query(String sql, Object[] args, ResultSetExtractor<T> resultSetExtractor) {
        return this.query(sql, args, null, resultSetExtractor);
    }

    /**
     * 查询记录集
     */
    public <T> T query(String sql, ResultSetExtractor<T> resultSetExtractor, Object... args) {
        return this.query(sql, args, resultSetExtractor);
    }

    /**
     * 查询记录集
     */
    public void query(String sql, Object[] args, RowCallbackHandler rowCallbackHandler) {
        this.query(sql, args, null, rowCallbackHandler);
    }

    /**
     * 查询记录集
     */
    public void query(String sql, RowCallbackHandler rowCallbackHandler, Object... args) {
        this.query(sql, args, rowCallbackHandler);
    }

    /**
     * 查询记录集
     */
    public void query(String sql, PreparedStatementSetter setter, RowCallbackHandler rowCallbackHandler) {
        this.query(new SimplePreparedStatementCreator(setter, sql), rowCallbackHandler);
    }

    /**
     * 查询记录集
     */
    public <T> List<T> query(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return this.query(new SimplePreparedStatementCreator(preparedStatementSetter, sql), rowMapper);
    }

    /**
     * 查询记录集
     */
    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) {
        return this.query(sql, args, null, rowMapper);
    }

    /**
     * 查询记录集
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return this.query(sql, args, rowMapper);
    }

    /**
     * 查询记录集 - RowMapper
     * db:base
     */
    public <T> List<T> query(PreparedStatementCreator preparedStatementCreator, RowMapper<T> rowMapper) {
        ArrayList<T> arrayList = new ArrayList<>();

        try {
            connection        = dataSource.getConnection();
            preparedStatement = preparedStatementCreator.createPreparedStatement(connection);
            resultSet         = preparedStatement.executeQuery();

            int rowNum = 0;
            while (resultSet.next()) {
                final T t = rowMapper.mapRow(resultSet, ++rowNum);
                arrayList.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }

        return arrayList;
    }

    /**
     * 查询记录集 - List&lt;T&gt; - RowMapper - args + argTypes
     */
    public <T> List<T> query(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) {
        ArrayList<T> arrayList = new ArrayList<>();

        try {
            resultSet = this.query(sql, args, argTypes);

            int rowNum = 0;
            while (resultSet.next()) {
                final T t = rowMapper.mapRow(resultSet, ++rowNum);
                arrayList.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }

        return arrayList;
    }

    /**
     * 查询记录集 - RowCallbackHandler
     * db:base
     */
    public void query(PreparedStatementCreator preparedStatementCreator, RowCallbackHandler rowCallbackHandler) {
        try {
            connection        = dataSource.getConnection();
            preparedStatement = preparedStatementCreator.createPreparedStatement(connection);
            resultSet         = preparedStatement.executeQuery();

            while (resultSet.next()) {
                rowCallbackHandler.processRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }
    }

    /**
     * 查询记录集 - ResultSetExtractor
     * db:base
     */
    public <T> T query(PreparedStatementCreator preparedStatementCreator, ResultSetExtractor<T> resultSetExtractor) {
        T result = null;

        try {
            connection        = dataSource.getConnection();
            preparedStatement = preparedStatementCreator.createPreparedStatement(connection);
            resultSet         = preparedStatement.executeQuery();

            result = resultSetExtractor.extractData(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }

        return result;
    }

    /**
     * 查询记录集 - ResultSetExtractor - args + argTypes
     */
    public <T> T query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> resultSetExtractor) {
        T result = null;

        try {
            resultSet = this.query(sql, args, argTypes);
            result    = resultSetExtractor.extractData(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }

        return result;
    }

    /**
     * 查询记录集 - RowCallbackHandler - args + argTypes
     */
    public void query(String sql, Object[] args, int[] argTypes, RowCallbackHandler rowCallbackHandler) {
        try {
            resultSet = this.query(sql, args, argTypes);
            while (resultSet.next()) {
                rowCallbackHandler.processRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanUp();
        }
    }

    /**
     * 对象查询 - Bean
     */
    public <T> T queryForObject(String sql, Class<T> requiredType) {
        return queryForObject(sql, null, requiredType);
    }

    /**
     * 对象查询 - Bean
     */
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
        return queryForObject(sql, args, requiredType);
    }

    /**
     * 对象查询 - Bean - 有参数
     */
    public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) {
        // 提高查询性能，只查询一条即可
        if (sql != null && !sql.toLowerCase().contains(" limit ")) {
            sql = sql + " LIMIT 1";
        }

        return this.query(sql, args, new ResultSetExtractor<T>() {
            @Override
            public T extractData(ResultSet rs) throws SQLException {
                T result = null;

                if (rs.next()) {
                    // 兼容基本类型的查询需求
                    if (requiredType == Integer.class) {
                        result = (T) Integer.valueOf(rs.getInt(1));
                    } else if (requiredType == Long.class) {
                        result = (T) Long.valueOf(rs.getLong(1));
                    } else {
                        // bean 类型
                        result = ResultRowToBean.process(rs, requiredType);
                    }
                }

                return result;
            }
        });
    }

    /**
     * 对象查询 - Bean - 无参数
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        return this.queryForObject(sql, null, rowMapper);
    }

    /**
     * 对象查询 - Bean - 有参数 - RowMapper
     */
    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        return this.queryForObject(sql, args, null, rowMapper);
    }

    /**
     * 对象查询 - Bean - args - RowMapper
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return this.queryForObject(sql, args, rowMapper);
    }

    /**
     * 对象查询 - Bean - args + argTypes - RowMapper
     */
    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) {
        // 提高查询性能，只查询一条即可
        if (sql != null && !sql.toLowerCase().contains(" limit ")) {
            sql = sql + " LIMIT 1";
        }

        List<T> result = this.query(sql, args, argTypes, rowMapper);
        if (result == null || result.size() == 0) {
            throw new RuntimeException("queryForObject resultSet is empty");
        }

        return result.get(0);
    }

    /**
     * 列表查询
     */
    public List<Map<String, Object>> queryForList(String sql) {
        return this.queryForList(sql, null, new int[]{});
    }

    /**
     * 列表查询
     */
    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        return this.queryForList(sql, args, new int[]{});
    }

    /**
     * 列表查询
     */
    public List<Map<String, Object>> queryForList(String sql, Object[] args, int[] argTypes) {
        return this.query(sql, args, argTypes, new ResultSetExtractor<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException {
                final List<Map<String, Object>> result = new ArrayList<>();

                final ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    final HashMap<String, Object> map = new HashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        map.put(metaData.getColumnName(i), rs.getObject(i));
                    }

                    result.add(map);
                }

                return result;
            }
        });
    }

    /**
     * 列表查询
     */
    public <T> List<T> queryForList(String sql, Class<T> requiredType) {
        return this.queryForList(sql, null, requiredType);
    }

    /**
     * 列表查询
     */
    public <T> List<T> queryForList(String sql, Object[] args, Class<T> requiredType) {
        return this.queryForList(sql, args, null, requiredType);
    }

    /**
     * 列表查询
     */
    public <T> List<T> queryForList(String sql, Class<T> requiredType, Object... args) {
        return this.queryForList(sql, args, requiredType);
    }

    /**
     * 列表查询
     */
    public <T> List<T> queryForList(String sql, Object[] args, int[] argTypes, Class<T> requiredType) {
        return this.query(sql, args, argTypes, new RowMapper<T>() {
            @Override
            public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                return ResultRowToBean.process(rs, requiredType);
            }
        });
    }

    /**
     * 字典查询 (1条）
     */
    public Map<String, Object> queryForMap(String sql, Object... args) {
        return this.queryForMap(sql, args, null);
    }

    /**
     * 字典查询 (1条）
     */
    public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) {
        // 提高查询性能，只查询一条即可
        if (sql != null && !sql.toLowerCase().contains(" limit ")) {
            sql = sql + " LIMIT 1";
        }

        final List<Map<String, Object>> data = this.queryForList(sql, args, argTypes);
        if (data == null || data.size() == 0) {
            return Map.of();
        }

        return data.get(0);
    }

    /**
     * 返回一个结果集之后调用 getString 或 getInt 等去取值
     */
    public ResultSet queryForRowSet(String sql, Object[] args, int[] argTypes) throws SQLException {
        return this.query(sql, args, argTypes);
    }

}
