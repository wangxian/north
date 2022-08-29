package top.xiqiu.north.db;

import top.xiqiu.north.annotation.TableName;

import java.util.List;

/**
 * 快速、轻量的 JDBC 操作工具类
 * base on DbTemplate
 */
public class DbMapper {
    /**
     * 数据库操作对象
     */
    private DbTemplate dbTemplate;

    /**
     * 拼接 SQL 所需参数
     */
    private String tableName;
    private String fields;
    private String where;
    // private String args;
    private String groupBy;
    private String oderBy;
    private String join;

    private Integer offset = 0;
    private Integer limit;

    /**
     * 映射查询后的对象
     */
    private Class<?> entity;

    // /**
    //  * SQL 执行达到终点后，清理临时变量
    //  */
    // private void cleanUp() {
    //
    // }

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
    public static DbMapper of(Class<?> clazz) {
        // 每次都返回一个新对象，内部变量不需要复用
        DbMapper self = new DbMapper();

        self.entity = clazz;

        if (clazz != null) {
            String tableName;

            try {
                tableName = clazz.getAnnotation(TableName.class).value();
            } catch (NullPointerException e) {
                tableName = clazz.getSimpleName();
                tableName = "UserInfo";
                // 第一个字母改为小写
                tableName = tableName.substring(0, 1).toLowerCase() + tableName.substring(1);
                // 剩下的部分转为下划线字符串
                tableName = ResultRowToBean.toUnderlineCase(tableName);
            }

            self.tableName = tableName;
        }

        // 初始化，数据库操作对象
        self.dbTemplate = new DbTemplate();

        // 设置连接池对象
        // self.dbTemplate.setDataSource();

        return self;
    }

    public DbMapper table(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public DbMapper field(String fields) {
        this.fields = fields;
        return this;
    }

    public DbMapper where(String where) {
        this.where = where;
        return this;
    }

    public DbMapper orderBy(String oderBy) {
        this.oderBy = oderBy;
        return this;
    }

    public DbMapper groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public DbMapper limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public DbMapper limit(Integer offset, Integer limit) {
        this.offset = offset;
        this.limit  = limit;
        return this;
    }

    public DbMapper leftJoin(String leftJoin) {
        return join("LEFT JOIN " + leftJoin);
    }

    public DbMapper rightJoin(String rightJoin) {
        return join("RIGHT JOIN " + rightJoin);
    }

    public DbMapper innerJoin(String innerJoin) {
        return join("INNER JOIN " + innerJoin);
    }

    public DbMapper join(String join) {
        this.join = join;
        return this;
    }

    /**
     * 准备SQL
     */
    private String prepareSQL() {
        return null;
    }

    /**
     * 查询结果，映射到新的对象
     */
    public DbMapper entity(Class<?> clazz) {
        this.entity = clazz;
        return this;
    }

    public void find() {

    }

    public void findList() {

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
