package dms.Menu;

import common.ADFUtils;

import dms.login.Person;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;

import javax.faces.context.FacesContext;

import oracle.adf.model.binding.DCIteratorBinding;

import oracle.adf.share.ADFContext;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.SortCriterion;

import team.epm.dms.view.DmsMenuTreeViewRowImpl;

public class MenuTreeModel extends ChildPropertyTreeModel {
    private ViewObject vo;
    private String locale;

    public MenuTreeModel() {
        super();
        String pid =ObjectUtils.toString(
            FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("menu_id"));
        pid = pid.length() > 0 ? pid : "null";
        DCIteratorBinding binding =
            ADFUtils.findIterator("DmsMenuTreeViewIterator");
        this.vo = binding.getViewObject();
        List<MenuItem> root = this.getChildMenuItem(pid);
        for (MenuItem itm : root) {
            itm.setChildren(this.getChildMenuItem(itm.getId()));
        }
        this.setChildProperty("children");
        this.setWrappedData(root);
    }

    private List<MenuItem> getChildMenuItem(String pid) {
        Person curUser = (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
        this.vo.setNamedWhereClauseParam("locale", curUser.getLocale());
        this.vo.setNamedWhereClauseParam("p_id", pid);
        vo.executeQuery();
        List<MenuItem> items = new ArrayList<MenuItem>();                   
        while (vo.hasNext()) {
            MenuItem item = new MenuItem((DmsMenuTreeViewRowImpl)vo.next());
            if(this.isMenuVisible(item)){
                items.add(item);
            }
        }
        return items;
    }
    private boolean isMenuVisible(MenuItem menu){
        Person curUser = (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
        Map authoriedFunction = (Map)ADFContext.getCurrent().getSessionScope().get("authoriedFunction");
        if("admin".equals(curUser.getAcc())){
            return true;
        }
        if(menu.getFunctionId()!=null){
            if(authoriedFunction.get(menu.getFunctionId())==null){
                return false;
            }else{
                return true;
            }
        }else{
            ViewObject vo=ADFUtils.findIterator("DmsSubMenuQueryViewIterator").getViewObject();
            vo.setNamedWhereClauseParam("locale", curUser.getLocale());
            vo.setNamedWhereClauseParam("id",menu.getId());
            vo.executeQuery();
            while(vo.hasNext()){
               Row row= vo.next();
                if(row.getAttribute("FunctionId")!=null&&authoriedFunction.get(row.getAttribute("FunctionId"))!=null){
                    return true;
                }
            }
        }
        return false;
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
