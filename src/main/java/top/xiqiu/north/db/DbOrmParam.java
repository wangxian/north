package top.xiqiu.north.db;

/**
 * DbMapper 拼接SQL所需各个参数
 */
public class DbOrmParam {
    private String tableName;
    private String fields;
    private String where;
    private String groupBy;
    private String oderBy;
    private String join;

    private Integer offset = 0;
    private Integer limit;

    private Class<?> entity;

    private DbTemplate dbTemplate;

    private String rawSQL;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getOderBy() {
        return oderBy;
    }

    public void setOderBy(String oderBy) {
        this.oderBy = oderBy;
    }

    public String getJoin() {
        return join;
    }

    public void setJoin(String join) {
        this.join = join;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Class<?> getEntity() {
        return entity;
    }

    public void setEntity(Class<?> entity) {
        this.entity = entity;
    }

    public String getRawSQL() {
        return rawSQL;
    }

    public void setRawSQL(String rawSQL) {
        this.rawSQL = rawSQL;
    }

    public DbTemplate getDbTemplate() {
        return dbTemplate;
    }

    public void setDbTemplate(DbTemplate dbTemplate) {
        this.dbTemplate = dbTemplate;
    }
}
