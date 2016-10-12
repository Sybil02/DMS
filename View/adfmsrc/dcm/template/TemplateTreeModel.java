package dcm.template;

import common.ADFUtils;

import common.DmsUtils;

import dms.login.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.faces.context.FacesContext;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;

import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.SortCriterion;

public class TemplateTreeModel  extends ChildPropertyTreeModel {
    private Map authoriedTemplate=new HashMap();
    public TemplateTreeModel() {
        super();

        ViewObject vo=DmsUtils.getDcmApplicationModule().getDcmUserTemplateView();
        vo.executeQuery();
        while(vo.hasNext()){
            Row row=vo.next();
            this.authoriedTemplate.put(row.getAttribute("TemplateId"), "TemplateName");
        }
        List<TemplateTreeItem> root = this.getChildTreeItem(null);
        for (TemplateTreeItem itm : root) {
            if(TemplateTreeItem.TYPE_CATEGORY.equals(itm.getType())){
                itm.setChildren(this.getChildTreeItem(itm.getId()));
            }
        }

        this.setChildProperty("children");
        this.setWrappedData(root);
    }

    private List<TemplateTreeItem> getChildTreeItem(String pid) {
        pid=pid==null ? "is null" : "='"+pid+"'";
        List<TemplateTreeItem> items = new ArrayList<TemplateTreeItem>();
        ViewObject catVo = DmsUtils.getDcmApplicationModule().getDcmTemplateCatView();
        ViewObject templateVo =DmsUtils.getDcmApplicationModule().getDcmTemplateView();
        ViewCriteria vc=catVo.createViewCriteria();
        ViewCriteriaRow vcRow = vc.createViewCriteriaRow();
        vcRow.setAttribute("PId", pid);
        vc.addElement(vcRow);
        catVo.applyViewCriteria(vc);
        catVo.reset();
        catVo.executeQuery();
        catVo.getViewCriteriaManager().setApplyViewCriteriaNames(null);
        while (catVo.hasNext()) {
            Row row = catVo.next();
            String id=ObjectUtils.toString(row.getAttribute("Id"));
            String label=ObjectUtils.toString(row.getAttribute("Name"));
            TemplateTreeItem item = new TemplateTreeItem(id,label,TemplateTreeItem.TYPE_CATEGORY);
            if(this.menuVisible(item)){
                items.add(item);
            }
        }
        vc=templateVo.createViewCriteria();
        vcRow = vc.createViewCriteriaRow();
        vcRow.setAttribute("CategoryId", pid);
        vc.addElement(vcRow);
        templateVo.applyViewCriteria(vc);
        templateVo.executeQuery();
        templateVo.getViewCriteriaManager().setApplyViewCriteriaNames(null);
        while (templateVo.hasNext()) {
            Row row = templateVo.next();
            String id=ObjectUtils.toString(row.getAttribute("Id"));
            String label=ObjectUtils.toString(row.getAttribute("Name"));
            TemplateTreeItem item = new TemplateTreeItem(id,label,TemplateTreeItem.TYPE_TEMPLATE);
            if(this.menuVisible(item)){
                items.add(item);
            }
        }
        return items;
    }
    
    private boolean menuVisible(TemplateTreeItem item){
        if(TemplateTreeItem.TYPE_TEMPLATE.equals(item.getType())){
            if(this.authoriedTemplate.get(item.getId())==null){
                return false;
            }else{
                return true;
            }
        }else{
            ViewObject vo=DmsUtils.getDcmApplicationModule().getDcmCatTemplateQueryView();
            vo.setNamedWhereClauseParam("categoryId", item.getId());
            vo.executeQuery();
            while(vo.hasNext()){
                Row row=vo.next();
                if(this.authoriedTemplate.get(row.getAttribute("TemplateId"))!=null){
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
        TemplateTreeItem item = (TemplateTreeItem)super.getRowData();
        if (item.getChildren() == null
            &&TemplateTreeItem.TYPE_CATEGORY.equals(item.getType())) {
            item.setChildren(this.getChildTreeItem(item.getId()));
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
