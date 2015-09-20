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

import oracle.adfinternal.view.faces.model.binding.FacesCtrlListBinding;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

public class ValueSetEditBean {
    public ValueSetEditBean() {

    }

    Map<String, List<SelectItem>> valCodeSelectMap = null;

    public Map<String, List<SelectItem>> getValCodeSelectMap() {
        if(valCodeSelectMap != null)
            return valCodeSelectMap;
        
        valCodeSelectMap = new HashMap<String, List<SelectItem>> ();
        
     
        ViewObject vo =
            ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject();
        Row row = vo.first();
        DBTransaction db =
            (DBTransaction)vo.getApplicationModule().getTransaction();
        while (row != null) {
            System.out.println(row.getAttribute("Source")+"  "+row.getAttribute("Code"));
            if(row.getAttribute("Source") == null)
            {
                row = vo.next();
                continue;
            }
            String source = row.getAttribute("Source").toString();
            String code = row.getAttribute("Code").toString();
            
            List<SelectItem> items = new ArrayList<SelectItem>();
            items.add(new SelectItem(null, ""));
            Statement stmt = db.createStatement(DBTransaction.DEFAULT);
            String sql = "select code,meaning from " + source;
            ResultSet rs = null;

            try {
                rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String mcode = rs.getString("code").toString();
                    String meaning = rs.getString("meaning").toString();
                    items.add(new SelectItem(mcode, meaning));
                }
            } catch (Exception e) {
                System.out.println("出错 " + sql); 
            } finally {
                try {
                    rs.close();
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } 
            
            valCodeSelectMap.put(source, items);
            row = vo.next();
        }

        for (Map.Entry e : valCodeSelectMap.entrySet()) {
            System.out.println(e.getKey() + "   " + e.getValue());
        }
    
        vo.first();
        return valCodeSelectMap;
    }

    public void setValCodeSelect(Map<String, List<SelectItem>> valCodeSelect) {
        this.valCodeSelectMap = valCodeSelect;
    }
    
    
    //如果值集源改变，则对应要改变默认值
    
    public void valueSourceChange(ValueChangeEvent valueChangeEvent) {
        String nvalue = (String)valueChangeEvent.getNewValue();
        String ovalue = (String)valueChangeEvent.getOldValue();

        if(ovalue == null)
            return ;
        
        if( nvalue.equals(ovalue) ){
            return ;
        } 
        List<SelectItem> test = valCodeSelectMap.remove(ovalue);
        
        if(nvalue == null)
            return ;
 
        ViewObject vo =
            ADFUtils.findIterator("DmsValueSetViewIterator").getViewObject();
        DBTransaction db =
            (DBTransaction)vo.getApplicationModule().getTransaction();

        Statement stmt = db.createStatement(DBTransaction.DEFAULT);
        System.out.println("table   " + nvalue);
        String sql = "select code,meaning from " + nvalue;
        ResultSet rs = null;

        try {
            rs = stmt.executeQuery(sql);
            List<SelectItem> items = new ArrayList<SelectItem>();
            items.add(new SelectItem(null, ""));
            
            while (rs.next()) {
                
                String mcode = rs.getString("code").toString();
                String meaning = rs.getString("meaning").toString();
                System.out.println(mcode+"    "+meaning);
                
                items.add(new SelectItem(mcode, meaning));
            }
            valCodeSelectMap.put(nvalue, items);
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                                                         new FacesMessage("值集表出错"));
        } finally {
            try {
                rs.close();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
