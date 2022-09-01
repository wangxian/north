package top.xiqiu.north.db;

import java.util.List;

/**
 * DbMapper 拼接SQL所需各个参数
 */
public class DbOrmParam<T> {
    private String tableName;
    private String fields;
    private String where;
    private String groupBy;
    private String having;
    private String orderBy;
    private String join;

    private Integer offset = 0;
    private Integer limit = 1000;

    private Class<T> entity;
    private Object[] args;
    private List<Object[]> batchArgs;

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

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
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

    public Class<T> getEntity() {
        return entity;
    }

    public void setEntity(Class<T> entity) {
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

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getHaving() {
        return having;
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public List<Object[]> getBatchArgs() {
        return batchArgs;
    }

    public void setBatchArgs(List<Object[]> batchArgs) {
        this.batchArgs = batchArgs;
    }
}
