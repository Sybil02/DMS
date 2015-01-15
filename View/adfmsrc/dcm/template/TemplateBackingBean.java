package dcm.template;

import common.ADFUtils;

import common.DmsUtils;

import common.JSFUtils;

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

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.server.ViewRowImpl;

import org.apache.commons.lang.ObjectUtils;

import weblogic.cache.webapp.KeySet;

public class TemplateBackingBean {
    private List<Map> headers;
    private String curTemplate;
    private KeySet selectedRows;
    private RichTable recordTable;
    private static Set dcmRemainAttr = new HashSet();
    private static ADFLogger logger =
        ADFLogger.createADFLogger(TemplateBackingBean.class);
    private RichPopup comPopup;
    private RichPopup validationPopup;
    static {
        dcmRemainAttr.add("IDX");
        dcmRemainAttr.add("COM_RECORD_ID");
        dcmRemainAttr.add("CREATED_AT");
        dcmRemainAttr.add("UPDATED_AT");
        dcmRemainAttr.add("CREATED_BY");
        dcmRemainAttr.add("UPDATED_BY");
    }
    //创建模板
    public void createTemplate(ActionEvent actionEvent) {
        ViewObject templateVo =
            ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject();
        ViewObject columnVo =
            ADFUtils.findIterator("DcmTemplateColumnViewIterator").getViewObject();
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
                try {
                    ResultSet rs = stmt.executeQuery(sql.toString());
                    while (rs.next()) {
                        if (dcmRemainAttr.contains(rs.getString("COL_NAME"))) {
                            continue;
                        }
                        Row row = columnVo.createRow();
                        row.setAttribute("ColumnLabel",
                                         rs.getString("COL_NAME"));
                        row.setAttribute("DbTableCol",
                                         rs.getString("COL_NAME"));
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
                } catch (Exception e) {
                    this.logger.severe(e);
                    JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.template.create_error_msg"));
                    return;
                }
            }
        }
        columnVo.getApplicationModule().getTransaction().commit();
        JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("dms.common.operation_success"));
    }
    //获取组合头信息
    public List<Map> getComheaderInfo() {
        ViewObject templateVo =
            ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject();
        ViewObject templateHeaderVo =DmsUtils.getDcmApplicationModule().getDcmComVsQueryView();
        if (this.curTemplate != null &&
            this.curTemplate.equals(templateVo.getCurrentRow().getAttribute("Id"))) {
            this.curTemplate =
                    (String)templateVo.getCurrentRow().getAttribute("Id");
            return this.headers;
        } else {
            if (null == templateVo.getCurrentRow())
                return new ArrayList<Map>();
            this.curTemplate =
                    (String)templateVo.getCurrentRow().getAttribute("Id");
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
    //打开组合
    public void openComRecord(ActionEvent actionEvent) {
        DCIteratorBinding iterator =
            ADFUtils.findIterator("getCombinationRecordViewIterator");
        ViewObject vo =DmsUtils.getDcmApplicationModule().getDcmTemplateCombinationView();
        ViewObject templateVo =
            ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject();
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
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.recordTable);
    }

    public void closeComRecord(ActionEvent actionEvent) {
        DCIteratorBinding iterator =
            ADFUtils.findIterator("getCombinationRecordViewIterator");
        ViewObject vo =DmsUtils.getDcmApplicationModule().getDcmTemplateCombinationView();
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

    public void removeTemplate(ActionEvent actionEvent) {
        ViewObject templateView =
            ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject();
        Row row = templateView.getCurrentRow();
        if (row == null) {
            return;
        }
        String templateId = ObjectUtils.toString(row.getAttribute("Id"));
        String clear_validation_sql =
            "delete from dcm_template_validation t where exists(select 1 from dcm_template_column c where c.id=t.column_id and c.template_id='" +
            templateId + "')";
        String clear_column_sql =
            "delete from dcm_template_column t where t.template_id='" +
            templateId + "'";
        String clear_authority_sql =
            "delete from dcm_role_template t where t.template_id='" +
            templateId + "'";
        String clear_combination =
            "delete from dcm_template_combination t where t.template_id='" +
            templateId + "'";
        DBTransaction trans =
            (DBTransaction)templateView.getApplicationModule().getTransaction();
        trans.executeCommand(clear_validation_sql);
        trans.executeCommand(clear_column_sql);
        trans.executeCommand(clear_authority_sql);
        trans.executeCommand(clear_combination);
        row.remove();
        trans.commit();
    }
    //添加列
    public void addColumn(ActionEvent actionEvent) {
        ViewObject templateVo =
            ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject();
        Row curTemplate = templateVo.getCurrentRow();
        if (curTemplate != null) {
            ViewObject columnVo =
                ADFUtils.findIterator("DcmTemplateColumnViewIterator").getViewObject();
            Row row = columnVo.createRow();
            row.setAttribute("TemplateId", curTemplate.getAttribute("Id"));
            columnVo.insertRow(row);
        }
    }


    public void setComPopup(RichPopup comPopup) {
        this.comPopup = comPopup;
    }

    public RichPopup getComPopup() {
        return comPopup;
    }
    //弹出组合管理窗口
    public void showComPopup(ActionEvent actionEvent) {
        ViewObject vo=ADFUtils.findIterator("getCombinationRecordViewIterator").getViewObject();
        if(vo.getApplicationModule().getTransaction().isDirty()){
            JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("dms.common.save_data_alert"));
        }else{
            RichPopup.PopupHints hint=new  RichPopup.PopupHints();
            this.comPopup.show(hint);
        }
    }

    public void showValidationPopup(ActionEvent actionEvent) {
        RichPopup.PopupHints hint=new RichPopup.PopupHints();
        this.validationPopup.show(hint);
    }

    public void setValidationPopup(RichPopup validationPopup) {
        this.validationPopup = validationPopup;
    }

    public RichPopup getValidationPopup() {
        return validationPopup;
    }
}
