package top.xiqiu.north.db;

/**
 * 接口：作用为获取 insert 后的自增 primary id
 */
public interface KeyHolder {
    void setKey(Integer key);

    Integer getKey();
}
