package dcm.template;

import common.ADFUtils;

import common.DmsUtils;

import dcm.DcmDataTableModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Map;

import java.util.Set;

import javax.faces.event.ActionEvent;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTableUtils;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.nav.RichCommandToolbarButton;

import oracle.jbo.ApplicationModule;
import oracle.jbo.AttributeDef;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.server.RowImpl;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

import org.apache.myfaces.trinidad.model.CollectionModel;

import weblogic.cache.webapp.KeySet;

public class TemplateBackingBean {

    private String templateId;

    private String tablename;

    private List<Map<String, Object>> data;

    private List<String> columns;

    private List<Map> headers;
    private String curTemplate;
    private KeySet selectedRows;

    private static Set dcmRemainAttr = new HashSet();
    private RichTable recordTable;
    static {
        dcmRemainAttr.add("IDX");
        dcmRemainAttr.add("COM_RECORD_ID");
        dcmRemainAttr.add("CREATED_AT");
        dcmRemainAttr.add("UPDATED_AT");
        dcmRemainAttr.add("CREATED_BY");
        dcmRemainAttr.add("UPDATED_BY");
        dcmRemainAttr.add("ID");
    }

    public ViewObject getCombinationRecordView(ActionEvent actionEvent) {
        String combinationId = "";
        DCIteratorBinding tempIter =
            ADFUtils.findIterator("DcmTemplateView1Iterator");
        String template_Id =
            (String)tempIter.getCurrentRow().getAttribute("Id");
        DCIteratorBinding iter =
            ADFUtils.findIterator("DcmCombinationView1Iterator");
        Row combinationRow = iter.getCurrentRow();
        String tablename = (String)combinationRow.getAttribute("Code");
        ApplicationModule am =
            ADFUtils.getApplicationModuleForDataControl("DcmModuleDataControl");
        String sql =
            "select * from Dcm_Template_Combination t,DFDF t1 where t.template_id=:1 " +
            "and t1.id=t.combination_record_id";
        ViewObjectImpl vo =
            (ViewObjectImpl)am.createViewObjectFromQueryStmt("", sql);

        vo.setWhereClauseParam(0, template_Id);
        vo.executeQuery();
        AttributeDef attr[] = vo.getAttributeDefs();
        while (vo.hasNext()) {
            Row row = vo.next();

            for (int i = 0; i < attr.length; i++)
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

    public void createTemplate(ActionEvent actionEvent) throws SQLException {
        ViewObject templateVo =
            ADFUtils.findIterator("DcmTemplateView1Iterator").getViewObject();
        ViewObject columnVo =
            ADFUtils.findIterator("DcmTemplateColumnView1Iterator").getViewObject();
        for (Row templateRow : templateVo.getAllRowsInRange()) {
            ViewRowImpl template = (ViewRowImpl)templateRow;
            //如果是新增模版则从数据库里生成列信息
            if (template.getEntities()[0].getEntityState() == 0) {
                DBTransaction db =
                    (DBTransaction)columnVo.getApplicationModule().getTransaction();
                Statement stmt = db.createStatement(DBTransaction.DEFAULT);
                StringBuffer sql = new StringBuffer();
                sql.append("select upper(t.COLUMN_NAME) \"COL_NAME\",upper(t.DATA_TYPE) \"COL_TYPE\",t.COLUMN_ID \"COL_ID\" from user_tab_columns t");
                sql.append(" where upper(t.TABLE_NAME)=upper('").append(template.getAttribute("DbTable")).append("')");
                sql.append(" order by t.COLUMN_ID");
                ResultSet rs = stmt.executeQuery(sql.toString());
                while (rs.next()) {
                    if (dcmRemainAttr.contains(rs.getString("COL_NAME"))) {
                        continue;
                    }
                    Row row = columnVo.createRow();
                    row.setAttribute("ColumnLabel", rs.getString("COL_NAME"));
                    row.setAttribute("DbTableCol", rs.getString("COL_NAME"));
                    row.setAttribute("Seq", rs.getString("COL_ID"));
                    String dataType = rs.getString("COL_TYPE");
                    if ("NUMBER".equals(dataType)) {
                        row.setAttribute("DataType", "NUMBER");
                    } else if ("DATE".equals(dataType) ||
                               "TIMESTAMP".equals(dataType)) {
                        row.setAttribute("DataType", "DATE");
                    } else {
                        row.setAttribute("DataType", "TEXT");
                    }
                    row.setAttribute("TemplateId",
                                     template.getAttribute("Id"));
                    columnVo.insertRow(row);
                }
                rs.close();
                stmt.close();
            }
        }
        columnVo.getApplicationModule().getTransaction().commit();
    }

    public List<Map> getComheaderInfo() {
        ViewObject templateVo =
            ADFUtils.findIterator("DcmTemplateView1Iterator").getViewObject();
        ViewObject templateHeaderVo =
            ADFUtils.findIterator("DcmComVsQueryViewIterator").getViewObject();
        if(this.curTemplate!=null&&this.curTemplate.equals(templateVo.getCurrentRow().getAttribute("Id"))){
            this.curTemplate = (String)templateVo.getCurrentRow().getAttribute("Id");
            return this.headers;
        }
        this.headers = new ArrayList<Map>();
        if (templateVo.getCurrentRow() != null &&
            templateVo.getCurrentRow().getAttribute("CombinationId") != null) {
            templateHeaderVo.setNamedWhereClauseParam("combinationId",
                                                      templateVo.getCurrentRow().getAttribute("CombinationId"));
            templateHeaderVo.executeQuery();
            while (templateHeaderVo.hasNext()) {
                Row h = templateHeaderVo.next();
                Map header = new HashMap();
                header.put("label", h.getAttribute("Name"));
                header.put("code", h.getAttribute("Code"));
                headers.add(header);
            }
            Map header = new HashMap();
            header.put("code", "STATUS");
            header.put("label",
                       DmsUtils.getMsg("dcm.combinationRecord.status"));
            headers.add(header);
        }
        return headers;
    }

    public void openComRecord(ActionEvent actionEvent) {
       DCIteratorBinding iterator=  ADFUtils.findIterator("getCombinationRecordViewIterator");
       ViewObject vo=ADFUtils.findIterator("DcmTemplateCombinationViewIterator").getViewObject();
        ViewObject templateVo =
            ADFUtils.findIterator("DcmTemplateView1Iterator").getViewObject();
       RowSetIterator rowSetIterator=iterator.getRowSetIterator();
       for(Object obj: this.recordTable.getSelectedRowKeys()){
           Key key = (Key)((List)obj).get(0);
           Row row = rowSetIterator.getRow(key);
           String comRecordId = (String)row.getAttribute("ID");
           String tid = (String)row.getAttribute("TID");
           String status=(String)row.getAttribute("STATUS");
           if("COLSE".equals(status)){
               if(tid!=null){
                   Key key1=new Key(new String[]{tid});
                   Row[] rows=vo.findByKey(key1, 1);
                   if(rows!=null&&rows.length>0){
                       Row orow=rows[0];
                       orow.setAttribute("Status", "OPEN");
                   }
               }else{
                   Row nrow=vo.createRow();
                   nrow.setAttribute("ComRecordId", comRecordId);
                   nrow.setAttribute("TemplateId", templateVo.getCurrentRow().getAttribute("Id"));
                   nrow.setAttribute("Status", "OPEN");
                   vo.insertRow(nrow);
               }
           }
       }
       vo.getApplicationModule().getTransaction().commit();
       iterator.getViewObject().executeQuery();
    }

    public void closeComRecord(ActionEvent actionEvent) {
        DCIteratorBinding iterator=  ADFUtils.findIterator("getCombinationRecordViewIterator");
        ViewObject vo=ADFUtils.findIterator("DcmTemplateCombinationViewIterator").getViewObject();
        RowSetIterator rowSetIterator=iterator.getRowSetIterator();
        for(Object obj: this.recordTable.getSelectedRowKeys()){
            Key key = (Key)((List)obj).get(0);
            Row row = rowSetIterator.getRow(key);
            String comRecordId = (String)row.getAttribute("ID");
            String tid = (String)row.getAttribute("TID");
            String status=(String)row.getAttribute("STATUS");
                if(tid!=null&&"OPEN".equals(status)){
                    Key key1=new Key(new String[]{tid});
                    Row[] rows=vo.findByKey(key1, 1);
                    if(rows!=null&&rows.length>0){
                        Row orow=rows[0];
                        orow.setAttribute("Status", "CLOSE");
                    }
                }
        }
        vo.getApplicationModule().getTransaction().commit();
        iterator.getViewObject().executeQuery();
    }

    public void setRecordTable(RichTable recordTable) {
        this.recordTable = recordTable;
    }

    public RichTable getRecordTable() {
        return recordTable;
    }

    public void setSelectedRows(KeySet selectedRows) {
        this.selectedRows = selectedRows;
    }

    public KeySet getSelectedRows() {
        return selectedRows;
    }
}
