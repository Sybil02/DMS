package dms.valueSet;


import common.ADFUtils;
import common.JSFUtils;

import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.view.rich.event.QueryEvent;

import oracle.adfinternal.view.faces.bi.util.JsfUtils;
import oracle.adfinternal.view.faces.model.binding.FacesCtrlHierBinding.FacesModel;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import org.apache.myfaces.trinidad.event.SelectionEvent;


public class ValueSetEditBean {
    public ValueSetEditBean() {

        ViewObject vo =
            ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject();
        
        valCodeSelectMap = new HashMap<String, List<SelectItem>>();
        
        while (vo.hasNext()) {
            Row row = vo.next();
            String defaultMeaning = (String) row.getAttribute("DefaultCode");
            String source = (String) row.getAttribute("Source");
            List<SelectItem> items = new ArrayList<SelectItem>();
            items.add(new SelectItem(null,null));
            if(defaultMeaning != null)
                items.add(new SelectItem(defaultMeaning,defaultMeaning));
            
            valCodeSelectMap.put(source, items);
        }
        vo.reset(); 
        setCurrentRowSource();
    }

    Map<String, List<SelectItem>> valCodeSelectMap = null;


    public Map<String, List<SelectItem>> getValCodeSelectMap() {

        return valCodeSelectMap;
    }

    public void setValCodeSelect(Map<String, List<SelectItem>> valCodeSelect) {
        this.valCodeSelectMap = valCodeSelect;
    }


    //如果值集源改变，则对应要改变默认值

    public void valueSourceChange(ValueChangeEvent valueChangeEvent) {

        String nvalue = (String)valueChangeEvent.getNewValue();
        String ovalue = (String)valueChangeEvent.getOldValue();

        if (ovalue == null)
            return;

        if (nvalue.equals(ovalue)) {
            return;
        }

        valCodeSelectMap.remove(ovalue);

        if (nvalue == null)
            return;


        setSource(nvalue,
                  ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject());

    }

    /**
     * 设置当前行的值集
     * @param
     * @return
     */
    private void setCurrentRowSource() {

        try {
            ViewObject vo =
                ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject();

            Row row = vo.getCurrentRow();

            String source = (String)row.getAttribute("Source");

            setSource(source, vo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据值集源获取值集信息
     * @param source
     * @return
     */
    private void setSource(String source, ViewObject vo) {

        if (valCodeSelectMap.get(source) != null && valCodeSelectMap.get(source).size() > 2) {
            return;
        }

        DBTransaction db =
            (DBTransaction)vo.getApplicationModule().getTransaction();

        List<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem(null, ""));
        Statement stmt = db.createStatement(DBTransaction.DEFAULT);
        String sql = "select code,meaning from " + source;
        ResultSet rs = null;

        try {
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                //String mcode = rs.getString("code").toString();
                String meaning = rs.getString("meaning").toString();
                items.add(new SelectItem(meaning, meaning));
            }

            valCodeSelectMap.put(source, items);

        } catch (Exception e) {
            System.out.println("出错 " + sql);
            FacesContext.getCurrentInstance().addMessage(null,
                                                         new FacesMessage("提示：值集表出错，不存在这个表"));
        } finally {
            try {
                rs.close();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 获取值集源信息
     * @param selectionEvent
     */
    public void selectListener(SelectionEvent selectionEvent) {

        FacesModel model =
            (FacesModel)JSFUtils.resolveExpression("#{bindings.DmsValueSetView.collectionModel}");
        model.makeCurrent(selectionEvent);

        setCurrentRowSource();

    }

    public void tableQueryListener(QueryEvent queryEvent) {

        Class[] cla = { QueryEvent.class };
        Object[] parm = { queryEvent };
        JSFUtils.resloveMethodExpression("#{bindings.DmsValueSetViewQuery.processQuery}",
                                         Void.class, cla, parm);

        setCurrentRowSource();

    }
}
