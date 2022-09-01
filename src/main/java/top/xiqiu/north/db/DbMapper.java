package top.xiqiu.north.db;

import org.slf4j.Logger;
import top.xiqiu.north.annotation.TableName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 快速、轻量的 JDBC 操作工具类
 * base on DbTemplate
 */
public class DbMapper {
    /**
     * logger
     **/
    private final Logger logger = getLogger(DbMapper.class);

    private static DbMapper instance;

    /**
     * ORM 参数
     */
    private final ThreadLocal<DbOrmParam> dbOrmParam = new ThreadLocal<>();

    /**
     * SQL 执行达到终点后，清理临时变量
     */
    private void cleanUp() {
        if (dbOrmParam != null) {
            dbOrmParam.remove();
            // dbOrmParam = null;
        }
    }

    /**
     * 创建对象入口
     */
    public static DbMapper of() {
        return of(null);
    }

    /**
     * 创建对象入口
     * <p>
     * 1. 优先，使用 @TableName 注解
     * 2. 其次，类名首字母小写，转为下划线字符串作为表名, Person - person, UserInfo -> user_info
     * 3. 否则，请使用 table() 方法主动设置表名
     *
     * @param entity 查询后，映射到 entity，同时也可以根据此推测表名
     */
    public static <T> DbMapper of(Class<T> entity) {
        // 单例 DbMapper，只创建一次
        if (instance == null) {
            instance = new DbMapper();
        }

        // 初始化 DbOrmParam
        final DbOrmParam<T> dbOrmParam = new DbOrmParam<>();
        instance.dbOrmParam.set(dbOrmParam);

        // 设置表名
        if (entity != null) {
            String tableName;

            try {
                tableName = entity.getAnnotation(TableName.class).value();
            } catch (NullPointerException e) {
                tableName = entity.getSimpleName();

                // 第一个字母改为小写
                tableName = tableName.substring(0, 1).toLowerCase() + tableName.substring(1);
                // 剩下的部分转为下划线字符串
                tableName = ResultRowToBean.toUnderlineCase(tableName);
            }

            dbOrmParam.setEntity(entity);
            dbOrmParam.setTableName(tableName);
        }

        // 初始化，数据库操作对象
        dbOrmParam.setDbTemplate(new DbTemplate());

        return instance;
    }

    public DbMapper table(String tableName) {
        this.dbOrmParam.get().setTableName(tableName);
        return this;
    }

    public DbMapper field(String fields) {
        this.dbOrmParam.get().setFields(fields);
        return this;
    }

    public DbMapper where(String where, Object... args) {
        final DbOrmParam dbOrmParam = this.dbOrmParam.get();
        dbOrmParam.setWhere(where);
        dbOrmParam.setArgs(args);

        return this;
    }

    public DbMapper groupBy(String groupBy) {
        this.dbOrmParam.get().setGroupBy(" GROUP BY " + groupBy);
        return this;
    }

    public DbMapper having(String having) {
        this.dbOrmParam.get().setHaving(" HAVING " + having);
        return this;
    }

    public DbMapper orderBy(String orderBy) {
        this.dbOrmParam.get().setOrderBy(" ORDER BY " + orderBy);
        return this;
    }

    public DbMapper limit(Integer limit) {
        this.dbOrmParam.get().setLimit(limit);
        return this;
    }

    public DbMapper limit(Integer offset, Integer limit) {
        this.dbOrmParam.get().setOffset(offset);
        this.dbOrmParam.get().setLimit(limit);
        return this;
    }

    public DbMapper leftJoin(String leftJoin) {
        return join(" LEFT JOIN " + leftJoin + " ");
    }

    public DbMapper rightJoin(String rightJoin) {
        return join(" RIGHT JOIN " + rightJoin + " ");
    }

    public DbMapper innerJoin(String innerJoin) {
        return join(" INNER JOIN " + innerJoin + " ");
    }

    public DbMapper join(String join) {
        this.dbOrmParam.get().setJoin(join);
        return this;
    }

    /**
     * 准备SQL
     *
     * @param sqlType 可选：rawSQL | query | queryOne | queryPage | update ｜ forceUpdate | insert | batchInsert | delete ｜ forceDelete
     */
    private String prepareSQL(String sqlType) {
        final DbOrmParam dbOrmParam = this.dbOrmParam.get();

        // 优先使用 RawSQL 作为查询条件
        if (dbOrmParam.getRawSQL() != null) {
            logger.info("==>  Preparing: {}", dbOrmParam.getRawSQL());
            return dbOrmParam.getRawSQL();
        }

        // 表名不允许为空
        if (dbOrmParam.getTableName() == null) {
            throw new RuntimeException("DbMapper cause error, tableName is null");
        }

        // 完整SQL
        StringBuilder fullSQL = new StringBuilder();

        switch (sqlType) {
            case "query":
            case "queryOne":
            case "queryPage":
                fullSQL.append("SELECT ");
                if ("queryPage".equals(sqlType)) {
                    fullSQL.append("count(*) AS count");
                } else {
                    fullSQL.append(dbOrmParam.getFields() == null ? "*" : dbOrmParam.getFields());
                }

                fullSQL.append(" FROM ");
                fullSQL.append(dbOrmParam.getTableName());

                // LEFT JOIN / INNER JOIN
                fullSQL.append(dbOrmParam.getJoin() == null ? "" : dbOrmParam.getJoin());

                if (dbOrmParam.getWhere() != null) {
                    fullSQL.append(" WHERE ").append(dbOrmParam.getWhere());
                }

                fullSQL.append(dbOrmParam.getGroupBy() == null ? "" : dbOrmParam.getGroupBy());
                fullSQL.append(dbOrmParam.getHaving() == null ? "" : dbOrmParam.getHaving());

                // 分页查 count 时，不需要 order by 语句
                if (!"queryPage".equals(sqlType)) {
                    fullSQL.append(dbOrmParam.getOrderBy() == null ? "" : dbOrmParam.getOrderBy());
                }

                // 查询一条 及 查询多条
                if ("queryOne".equals(sqlType) || "queryPage".equals(sqlType)) {
                    fullSQL.append(" LIMIT 1");
                } else {
                    fullSQL.append(" LIMIT ").append(dbOrmParam.getLimit());
                    fullSQL.append(" OFFSET ").append(dbOrmParam.getOffset());
                }

                break;
            case "insert":
            case "batchInsert":
                if (dbOrmParam.getFields() == null) {
                    throw new RuntimeException("DbMapper cause error, insert statement must call/set fields(\"some fields\")");
                }

                fullSQL.append("INSERT INTO ").append(dbOrmParam.getTableName());
                fullSQL.append(" (").append(dbOrmParam.getFields()).append(") ");
                fullSQL.append("VALUES (");

                Object[] args = new Object[]{};

                if ("insert".equals(sqlType)) {
                    args = dbOrmParam.getArgs();

                    if (dbOrmParam.getArgs() == null) {
                        throw new RuntimeException("DbMapper cause error, insert statement must be set to Object... args");
                    }
                } else {
                    if (dbOrmParam.getBatchArgs() == null || dbOrmParam.getBatchArgs().size() == 0) {
                        throw new RuntimeException("DbMapper cause error, batchInsert batchArgs is empty");
                    }

                    args = (Object[]) dbOrmParam.getBatchArgs().get(0);
                }

                for (int i = 0; i < args.length; i++) {
                    fullSQL.append("?, ");
                }
                fullSQL.delete(fullSQL.length() - 2, fullSQL.length());
                fullSQL.append(")");

                break;
            case "update":
            case "forceUpdate":
                if (dbOrmParam.getFields() == null) {
                    throw new RuntimeException("DbMapper cause error, update statement must call/set fields(\"some fields\")");
                }

                if ("update".equals(sqlType) && dbOrmParam.getWhere() == null) {
                    throw new RuntimeException("DbMapper cause error, update statement where condition is miss, consider force update(true, Object... args) ?");
                }

                fullSQL.append("UPDATE ").append(dbOrmParam.getTableName()).append(" SET ");

                String updateFields;
                if (dbOrmParam.getFields().contains(",")) {
                    updateFields = dbOrmParam.getFields().replace(",", " = ?,");
                } else {
                    updateFields = dbOrmParam.getFields() + " = ?";
                }

                fullSQL.append(updateFields);

                if (dbOrmParam.getWhere() != null) {
                    fullSQL.append(" WHERE ").append(dbOrmParam.getWhere());
                }

                break;
            case "delete":
            case "forceDelete":
                if ("delete".equals(sqlType) && dbOrmParam.getWhere() == null) {
                    throw new RuntimeException("DbMapper cause error, delete statement where condition is miss, consider force update(true) ?");
                }

                fullSQL.append("DELETE FROM ").append(dbOrmParam.getTableName());
                if (dbOrmParam.getWhere() != null) {
                    fullSQL.append(" WHERE ").append(dbOrmParam.getWhere());
                }

                break;
            default:
                throw new RuntimeException("DbMapper cause error, invalid sqlType = " + sqlType);
                // break;
        }

        logger.info("==>  Preparing: {}", fullSQL);
        logger.info("==> Parameters: {}" + (dbOrmParam.getBatchArgs() != null ? ",..." : ""), dbOrmParam.getBatchArgs() != null
                ? Arrays.toString((Object[]) dbOrmParam.getBatchArgs().get(0))
                : Arrays.toString(dbOrmParam.getArgs()));

        return fullSQL.toString();
    }

    /**
     * 查询结果，映射到新的对象
     */
    public <T> DbMapper entity(Class<T> entity) {
        this.dbOrmParam.get().setEntity(entity);
        return this;
    }

    /**
     * 原始 SQL 查询，适合复杂SQL的情况
     */
    public DbMapper rawSQL(String sql, Object... args) {
        dbOrmParam.get().setArgs(args);
        dbOrmParam.get().setRawSQL(sql);
        return this;
    }

    /**
     * 查询一条数据
     */
    public <T> T find() {
        String sql = prepareSQL("queryOne");
        Object[] args = dbOrmParam.get().getArgs();

        final Class<T> entity = dbOrmParam.get().getEntity();
        long timeBegin = System.currentTimeMillis();
        final T result = dbOrmParam.get().getDbTemplate().queryForObject(sql, args, entity);
        logger.info("<==  Cost time: {}ms", System.currentTimeMillis() - timeBegin);

        // 执行已到终点，清理变量
        this.cleanUp();

        return result;
    }

    /**
     * 查询多条数据
     */
    public <T> List<T> findList() {
        String sql = prepareSQL("query");
        Object[] args = dbOrmParam.get().getArgs();

        final Class<T> entity = dbOrmParam.get().getEntity();
        long timeBegin = System.currentTimeMillis();
        final List<T> result = dbOrmParam.get().getDbTemplate().queryForList(sql, args, entity);
        logger.info("<==  Cost time: {}ms", System.currentTimeMillis() - timeBegin);

        // 执行已到终点，清理变量
        this.cleanUp();

        return result;
    }

    /**
     * 分页查询数据
     */
    public <T> DbPage<T> findPage() {
        if (dbOrmParam.get().getLimit() == null || dbOrmParam.get().getOrderBy() == null) {
            throw new RuntimeException("DbMapper cause error, findPage statement need ORDER BY and LIMIT");
        }

        final DbPage<T> page = new DbPage<>();

        String sqlCount = prepareSQL("queryPage");
        Object[] args = dbOrmParam.get().getArgs();

        // 查询开始时间
        long timeBegin = System.currentTimeMillis();

        // 结算总页数
        Integer count = dbOrmParam.get().getDbTemplate().queryForObject(sqlCount, args, Integer.class);
        page.setTotal(count.intValue());

        // 查询数据集
        final String sql = prepareSQL("query");
        final Class<T> entity = dbOrmParam.get().getEntity();
        final List<T> result = dbOrmParam.get().getDbTemplate().queryForList(sql, args, entity);
        page.setRecords(result);

        logger.info("<==  Cost time: {}ms", System.currentTimeMillis() - timeBegin);

        return page;
    }

    /**
     * 插入数据
     */
    public int insert(Object... args) {
        dbOrmParam.get().setArgs(args);
        String sql = prepareSQL("insert");

        KeyHolder keyHolder = new GeneratedKeyHolder();

        final SimplePreparedStatementCreator simplePreparedStatementCreator
                = new SimplePreparedStatementCreator(new ArgsTypePreparedStatementSetter(args, null), sql);

        long timeBegin = System.currentTimeMillis();
        dbOrmParam.get().getDbTemplate().update(simplePreparedStatementCreator, keyHolder);
        logger.info("<==  Cost time: {}ms", System.currentTimeMillis() - timeBegin);

        // 执行已到终点，清理变量
        this.cleanUp();

        return keyHolder.getKey();
    }

    /**
     * 批量插入
     */
    public int[] batchInsert(List<Object[]> batchArgs) {
        return batchInsert(batchArgs, null);
    }

    /**
     * 批量插入
     */
    public int[] batchInsert(List<Object[]> batchArgs, int[] argTypes) {
        dbOrmParam.get().setBatchArgs(batchArgs);
        String sql = prepareSQL("batchInsert");

        long timeBegin = System.currentTimeMillis();
        int[] result = dbOrmParam.get().getDbTemplate().batchUpdate(sql, batchArgs, argTypes);
        logger.info("<==  Cost time: {}ms", System.currentTimeMillis() - timeBegin);

        // 执行已到终点，清理变量
        this.cleanUp();

        return result;
    }

    /**
     * 更新数据
     *
     * @param isForce 是否强制更新，避免误操作
     */
    public int update(boolean isForce, Object... args) {
        // 合并 where 的 args 和 行参的 args
        Object[] argsWhere = dbOrmParam.get().getArgs();
        if (argsWhere == null) {
            dbOrmParam.get().setArgs(args);
        } else {
            List<Object> argList = new ArrayList<>(Arrays.asList(args));
            argList.addAll(Arrays.asList(argsWhere));

            // 重新赋值合，并后的新参数列表
            args = argList.toArray();

            dbOrmParam.get().setArgs(args);
        }

        String sql = prepareSQL(isForce ? "forceUpdate" : "update");

        long timeBegin = System.currentTimeMillis();
        int result = dbOrmParam.get().getDbTemplate().update(sql, args);
        logger.info("<==  Cost time: {}ms", System.currentTimeMillis() - timeBegin);

        // 执行已到终点，清理变量
        this.cleanUp();

        return result;
    }

    /**
     * 更新数据 - 正常更新（非强制）
     */
    public int update(Object... args) {
        return update(false, args);
    }

    /**
     * 删除数据
     *
     * @param isForce 是否强制删除，避免误删数据
     */
    public int delete(boolean isForce) {
        String sql = prepareSQL(isForce ? "forceDelete" : "delete");

        long timeBegin = System.currentTimeMillis();
        int result = dbOrmParam.get().getDbTemplate().update(sql, dbOrmParam.get().getArgs());
        logger.info("<==  Cost time: {}ms", System.currentTimeMillis() - timeBegin);

        // 执行已到终点，清理变量
        this.cleanUp();

        return result;
    }

    /**
     * 正常删除（非强制）
     */
    public int delete() {
        return delete(false);
    }

    /**
     * 执行原始SQL操作
     * 支持 update / insert / delete
     */
    public int execute() {
        String sql = prepareSQL("rawSQL");

        long timeBegin = System.currentTimeMillis();
        int result = dbOrmParam.get().getDbTemplate().update(sql, dbOrmParam.get().getArgs());
        logger.info("<==  Cost time: {}ms", System.currentTimeMillis() - timeBegin);

        return result;
    }
}
