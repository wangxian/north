package top.xiqiu.north.db;

import java.util.List;

public class DbPage<T> {
    /**
     * 总数据量
     */
    private int total;

    /**
     * 每页数据数量
     */
    private int pageSize;

    /**
     * 总页数
     */
    private int pageCount;

    /**
     * 当前页
     */
    private int currentPage;

    /**
     * 数据记录
     */
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

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public String toString() {
        return "DbPage{" +
                "total=" + total +
                ", pageSize=" + pageSize +
                ", pageCount=" + pageCount +
                ", currentPage=" + currentPage +
                ", records=" + records +
                '}';
    }
}
