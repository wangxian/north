package top.xiqiu.north.db;

/**
 * 获取 insert 后的自增 primary id
 */
public class GeneratedKeyHolder implements KeyHolder {
    private Integer key;

    @Override
    public void setKey(Integer key) {
        this.key = key;
    }

    @Override
    public Integer getKey() {
        return key;
    }
}
