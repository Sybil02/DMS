package dms.Menu;

import common.ADFUtils;

import dms.login.Person;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import oracle.adf.model.binding.DCIteratorBinding;

import oracle.adf.share.ADFContext;

import oracle.jbo.ViewObject;

import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.SortCriterion;

import team.epm.dms.view.DmsMenuTreeViewRowImpl;

public class MenuTreeModel extends ChildPropertyTreeModel {
    private ViewObject vo;
    private String locale;

    public MenuTreeModel() {
        super();
        String pid =
            FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("menu_id") +
            "";
        pid = pid.length() > 0 ? pid : "null";
        DCIteratorBinding binding =
            ADFUtils.findIterator("DmsMenuTreeViewIterator");
        this.vo = binding.getViewObject();
        this.locale =
                ((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getLocale();

        List<MenuItem> root = this.getChildMenuItem(pid);
        for (MenuItem itm : root) {
            itm.setChildren(this.getChildMenuItem(itm.getId()));
        }

        this.setChildProperty("children");
        this.setWrappedData(root);
    }

    private List<MenuItem> getChildMenuItem(String pid) {
        this.vo.setNamedWhereClauseParam("locale", this.locale);
        this.vo.setNamedWhereClauseParam("p_id", pid);
        vo.executeQuery();
        List<MenuItem> items = new ArrayList<MenuItem>();
        while (vo.hasNext()) {
            MenuItem item = new MenuItem((DmsMenuTreeViewRowImpl)vo.next());
            items.add(item);
        }
        return items;
    }

    @Override
    protected CollectionModel createChildModel(Object object) {
        return super.createChildModel(object);
    }

    @Override
    public void enterContainer() {
        super.enterContainer();
    }

    @Override
    public void exitContainer() {
        super.exitContainer();
    }

    @Override
    protected Object getChildData(Object object) {
        return super.getChildData(object);
    }

    @Override
    public Object getContainerRowKey(Object object) {
        return super.getContainerRowKey(object);
    }

    @Override
    public int getRowCount() {
        return super.getRowCount();
    }

    @Override
    public Object getRowData() {
        MenuItem item = (MenuItem)super.getRowData();
        if (item.getChildren() == null) {
            item.setChildren(this.getChildMenuItem(item.getId()));
        }
        return super.getRowData();
    }

    @Override
    public int getRowIndex() {
        return super.getRowIndex();
    }

    @Override
    public Object getRowKey() {
        return super.getRowKey();
    }

    @Override
    public List<SortCriterion> getSortCriteria() {
        return super.getSortCriteria();
    }

    @Override
    public Object getWrappedData() {
        return super.getWrappedData();
    }

    @Override
    public boolean isContainer() {
        return super.isContainer();
    }

    @Override
    public boolean isRowAvailable() {
        return super.isRowAvailable();
    }

    @Override
    public boolean isSortable(String string) {
        return super.isSortable(string);
    }

    @Override
    public void setRowIndex(int i) {
        super.setRowIndex(i);
    }

    @Override
    public void setRowKey(Object object) {
        super.setRowKey(object);
    }

    @Override
    public void setSortCriteria(List<SortCriterion> list) {
        super.setSortCriteria(list);
    }

    @Override
    public void setWrappedData(Object object) {
        super.setWrappedData(object);
    }
}
