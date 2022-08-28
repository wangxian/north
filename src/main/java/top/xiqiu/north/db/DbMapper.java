package top.xiqiu.north.db;

/**
 * 快速、轻量的 JDBC 操作工具类
 * base on DbTemplate
 */
public class DbMapper {
    /**
     * 拼接 SQL 所需参数
     */
    private String fields;
    private String tableName;
    private String where;
    private String args;
    private String groupBy;
    private String oderBy;
    private Integer offset;
    private Integer limit;

    public DbMapper() {

    }

    /**
     * 提前初始化一些配置
     */
    public static DbMapper builder() {
        return new DbMapper();
    }


    /**
     * SQL 执行达到终点后，清理临时变量
     */
    private void cleanUp() {

    }

    public DbMapper form(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public DbMapper where(String where) {
        this.where = where;
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

    public int insert() {
        return 1;
    }

    public int update() {
        return 1;
    }


}
