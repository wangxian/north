package top.xiqiu.north.db;

import java.util.List;

public class DbPage<T> {
    private int total;
    private List<T> records;

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "DbPage{" +
                "total=" + total +
                ", records=" + records +
                '}';
    }
}
