package common;

import javax.faces.event.FacesEvent;

public abstract class TablePagination {
    //每页加载500行
    private static final int pageSize=500;
    //当前页
    private int curPage;
    //总页数
    private int totalPage;
    //数据总行数
    private int totalCount;
    public TablePagination() {
    }

    public void setCurPage(int curPage) {
        if(curPage>0&&curPage<=this.totalPage){
            this.curPage = curPage;
        }
    }

    public int getCurPage() {
        return curPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalCount(int totalCount) {
        if(totalCount>0){
            this.totalCount = totalCount;
            this.totalPage=(Integer)((this.totalCount+this.pageSize-1)/this.pageSize);
        }
    }
    public int getStartPoint(){
        return (this.curPage-1)*this.pageSize;  
    }
    public int getEndPoint(){
        return this.curPage*this.pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }
    public String getPaginationSql(String originSql){
        StringBuffer sql=new StringBuffer();
        sql.append("SELECT * FROM(SELECT/*+ FIRST_ROWS */QS.*,ROWNUM AS R_NUM FROM")
           .append("(").append(originSql).append(") QS WHERE ROWNUM<=")
           .append(this.getEndPoint()).append(") WHERE R_NUM >").append(this.getStartPoint());
        return sql.toString();
    }
    public String getCountSql(String originSql){
        StringBuffer sql=new StringBuffer();
        sql.append("SELECT COUNT(1) FROM (").append(originSql).append(")");
        return sql.toString();
    }
    //下一页
    public abstract void nextPage(FacesEvent event);
    //上一页
    public abstract void prePage(FacesEvent event);
    //刷新
    public abstract void refreshPage(FacesEvent event);
    //跳到
    public abstract void gotoPage(FacesEvent event);
    //第一页
    public abstract void firstPage(FacesEvent event);
    //最后一页
    public abstract void lastPage(FacesEvent event);
}
