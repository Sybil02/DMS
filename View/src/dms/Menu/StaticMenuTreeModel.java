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

import team.epm.dms.view.DmsMenuTreeViewRowImpl;

public class StaticMenuTreeModel extends ChildPropertyTreeModel{
    public StaticMenuTreeModel() {
        super();
        String pid =ObjectUtils.toString(
            FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("menu_id"));
        pid = pid.length() > 0 ? pid : "null";
        
        List<MenuItem> list = getChildMenuItem(pid);
        
        setChildProperty("children");
        setWrappedData(list);
    }
    
    
    private List<MenuItem> getChildMenuItem(String pid) {
         
        ViewObject vo = ADFUtils.findIterator("DmsMenuTreeViewIterator").getViewObject();
        
        Person curUser = (Person)ADFContext.getCurrent().getSessionScope().get("cur_user");
        vo.setNamedWhereClauseParam("locale", curUser.getLocale());
        vo.setNamedWhereClauseParam("p_id", pid);
        vo.executeQuery();
        List<MenuItem> items = new ArrayList<MenuItem>();                   
        while (vo.hasNext()) {
            MenuItem item = new MenuItem((DmsMenuTreeViewRowImpl)vo.next());
            if(this.isMenuVisible(item)){ 
                items.add(item);
            }
        }
        for(MenuItem item : items) {
            item.setChildren(getChildMenuItem(item.getId())); 
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
}
