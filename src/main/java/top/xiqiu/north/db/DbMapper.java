package top.xiqiu.north.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.TableName;

import java.util.List;

/**
 * 快速、轻量的 JDBC 操作工具类
 * base on DbTemplate
 */
public class DbMapper {
    private static DbMapper instance;

    /**
     * ORM 参数
     */
    private ThreadLocal<DbOrmParam> dbOrmParam = new ThreadLocal<>();

    /**
     * SQL 执行达到终点后，清理临时变量
     */
    private void cleanUp() {
        if (dbOrmParam != null) {
            dbOrmParam.remove();
            dbOrmParam = null;
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

    public DbMapper orderBy(String groupBy) {
        this.dbOrmParam.get().setOrderBy(" ORDER BY " + groupBy);
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
     * @param sqlType 可选：query | update | insert | delete
     */
    private String prepareSQL(String sqlType) {
        final DbOrmParam dbOrmParam = this.dbOrmParam.get();

        // 优先使用 RawSQL 作为查询条件
        if (dbOrmParam.getRawSQL() != null) {
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
                fullSQL.append("SELECT ");
                fullSQL.append(dbOrmParam.getFields() == null ? "*" : dbOrmParam.getFields());
                fullSQL.append(" FROM ");
                fullSQL.append(dbOrmParam.getTableName());

                // LEFT JOIN / INNER JOIN
                fullSQL.append(dbOrmParam.getJoin() == null ? "" : dbOrmParam.getJoin());

                if (dbOrmParam.getWhere() != null) {
                    fullSQL.append(" WHERE ").append(dbOrmParam.getWhere());
                }

                fullSQL.append(dbOrmParam.getGroupBy() == null ? "" : dbOrmParam.getGroupBy());
                fullSQL.append(dbOrmParam.getHaving() == null ? "" : dbOrmParam.getHaving());
                fullSQL.append(dbOrmParam.getOrderBy() == null ? "" : dbOrmParam.getOrderBy());

                fullSQL.append(" LIMIT ").append(dbOrmParam.getLimit());
                fullSQL.append(" OFFSET ").append(dbOrmParam.getOffset());

                break;
        }

        return fullSQL.toString();
    }

    /**
     * 查询结果，映射到新的对象
     */
    public <T> DbMapper entity(Class<T> entity) {
        this.dbOrmParam.get().setEntity(entity);
        return this;
    }

    public void find() {

    }

    public <T> List<T> findList() {
        final Logger logger = LoggerFactory.getLogger(DbMapper.class);

        String sql = prepareSQL("query");
        logger.info("execute SQL --- {}", sql);

        Object[] args = dbOrmParam.get().getArgs();
        logger.info("execute args --- {}", args);

        final Class<T> entity = dbOrmParam.get().getEntity();
        final List<T> result = dbOrmParam.get().getDbTemplate().queryForList(sql, args, entity);

        // 执行已到终点，清理变量
        this.cleanUp();

        return result;
    }

    public void findPage() {

    }

    public int delete() {
        return 1;
    }

    public int update() {
        return 1;
    }

    public int insert() {
        return 1;
    }

    /**
     * 批量插入
     */
    public int[] batchInsert(List<Object[]> args) {
        return batchInsert(args, null);
    }

    /**
     * 批量插入
     */
    public int[] batchInsert(List<Object[]> args, int[] argTypes) {
        return null;
    }
}
