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

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTableUtils;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.nav.RichCommandToolbarButton;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;

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
    private List<Map> headers;
    private String curTemplate;
    private KeySet selectedRows;
    private RichTable recordTable;
    private static Set dcmRemainAttr = new HashSet();
    static {
        dcmRemainAttr.add("IDX");
        dcmRemainAttr.add("COM_RECORD_ID");
        dcmRemainAttr.add("CREATED_AT");
        dcmRemainAttr.add("UPDATED_AT");
        dcmRemainAttr.add("CREATED_BY");
        dcmRemainAttr.add("UPDATED_BY");
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
        if (this.curTemplate != null &&
            this.curTemplate.equals(templateVo.getCurrentRow().getAttribute("Id"))) {
            this.curTemplate =
                    (String)templateVo.getCurrentRow().getAttribute("Id");
            return this.headers;
        }else{
            if(null==templateVo.getCurrentRow()) return new ArrayList<Map>();
            this.curTemplate=(String)templateVo.getCurrentRow().getAttribute("Id");
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
        DCBindingContainer dcBindings = (DCBindingContainer)BindingContext.getCurrent().getCurrentBindingsEntry();
        dcBindings.refreshControl();
        return headers;
    }

    public void openComRecord(ActionEvent actionEvent) {
        DCIteratorBinding iterator =
            ADFUtils.findIterator("getCombinationRecordViewIterator");
        ViewObject vo =
            ADFUtils.findIterator("DcmTemplateCombinationViewIterator").getViewObject();
        ViewObject templateVo =
            ADFUtils.findIterator("DcmTemplateView1Iterator").getViewObject();
        RowSetIterator rowSetIterator = iterator.getRowSetIterator();
        for (Object obj : this.recordTable.getSelectedRowKeys()) {
            Key key = (Key)((List)obj).get(0);
            Row row = rowSetIterator.getRow(key);
            String comRecordId = (String)row.getAttribute("ID");
            String tid = (String)row.getAttribute("TID");
            String status = (String)row.getAttribute("STATUS");
            if ("CLOSE".equals(status)) {
                if (tid != null) {
                    Key key1 = new Key(new String[] { tid });
                    Row[] rows = vo.findByKey(key1, 1);
                    if (rows != null && rows.length > 0) {
                        Row orow = rows[0];
                        orow.setAttribute("Status", "OPEN");
                    }
                } else {
                    Row nrow = vo.createRow();
                    nrow.setAttribute("ComRecordId", comRecordId);
                    nrow.setAttribute("TemplateId",
                                      templateVo.getCurrentRow().getAttribute("Id"));
                    nrow.setAttribute("Status", "OPEN");
                    vo.insertRow(nrow);
                }
            }
        }
        vo.getApplicationModule().getTransaction().commit();
        iterator.getViewObject().clearCache();
        iterator.getViewObject().executeQuery();
        iterator.getRowSetIterator().reset();
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.recordTable);
    }

    public void closeComRecord(ActionEvent actionEvent) {
        DCIteratorBinding iterator =
            ADFUtils.findIterator("getCombinationRecordViewIterator");
        ViewObject vo =
            ADFUtils.findIterator("DcmTemplateCombinationViewIterator").getViewObject();
        RowSetIterator rowSetIterator = iterator.getRowSetIterator();
        for (Object obj : this.recordTable.getSelectedRowKeys()) {
            Key key = (Key)((List)obj).get(0);
            Row row = rowSetIterator.getRow(key);
            String tid = (String)row.getAttribute("TID");
            String status = (String)row.getAttribute("STATUS");
            if (tid != null && "OPEN".equals(status)) {
                Key key1 = new Key(new String[] { tid });
                Row[] rows = vo.findByKey(key1, 1);
                if (rows != null && rows.length > 0) {
                    Row orow = rows[0];
                    orow.setAttribute("Status", "CLOSE");
                }
            }
        }
        vo.getApplicationModule().getTransaction().commit();
        iterator.getViewObject().clearCache();
        iterator.getViewObject().executeQuery();
        iterator.getRowSetIterator().reset();
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.recordTable);
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
