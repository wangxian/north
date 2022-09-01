package top.xiqiu.north.db;

/**
 * 分页参数
 */
public class Pagination {
    private int pageSize;
    private int currentPage;

    public Pagination(int currentPage, int pageSize) {
        if (currentPage < 1) {
            currentPage = 1;
        }

        this.currentPage = currentPage;
        this.pageSize    = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
