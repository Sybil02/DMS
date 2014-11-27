package dcm.template;

import common.ADFUtils;

import dcm.DcmDataTableModel;

import java.util.List;

import java.util.Map;

import javax.faces.event.ActionEvent;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTableUtils;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.nav.RichCommandToolbarButton;

import oracle.jbo.ApplicationModule;
import oracle.jbo.AttributeDef;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.RowImpl;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

public class TemplateBackingBean {
   
    private String templateId;
    
    private String tablename;
   
    private List<Map<String,Object>>  data;
    
    private List<String> columns;
    
    public ViewObject getCombinationRecordView(ActionEvent actionEvent){
        String combinationId="";
        DCIteratorBinding tempIter=ADFUtils.findIterator("DcmTemplateView1Iterator");
        String template_Id = (String)tempIter.getCurrentRow().getAttribute("Id");
        DCIteratorBinding iter=ADFUtils.findIterator("DcmCombinationView1Iterator");    
        Row combinationRow=iter.getCurrentRow();
        String tablename = (String)combinationRow.getAttribute("Code");
        ApplicationModule am=ADFUtils.getApplicationModuleForDataControl("DcmModuleDataControl");
        String sql="select * from Dcm_Template_Combination t,DFDF t1 where t.template_id=:1 " +
            "and t1.id=t.combination_record_id";
        ViewObjectImpl vo =
            (ViewObjectImpl)am.createViewObjectFromQueryStmt("", sql);
        
        vo.setWhereClauseParam(0, template_Id);
        vo.executeQuery();
        AttributeDef attr[]=vo.getAttributeDefs();
        while(vo.hasNext()){
         Row row=vo.next();
            
            for (int i=0;i<attr.length;i++)
            row.getAttribute(attr[i].getName());
        }
        
       return vo;
        
    }


    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getTablename() {
        return tablename;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumns() {
        return columns;
    }
}
