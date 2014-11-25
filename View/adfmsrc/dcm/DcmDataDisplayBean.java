package dcm;

import common.ADFUtils;
import common.DmsUtils;

import dcm.template.TemplateTreeItem;
import dcm.template.TemplateTreeModel;

import dms.dynamicShell.TabContext;

import dms.login.Person;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTree;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;

import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaItem;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;

import oracle.jbo.server.DBTransaction;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.UploadedFile;

import org.hexj.excelhandler.reader.ExcelReaderUtil;
import org.hexj.excelhandler.reader.IRowReader;

public class DcmDataDisplayBean {
    private CollectionModel dataModel;
    private String curTemplateId;
    private String templateName;
    //模版是否只读
    private String temlateReadOnly;
    //模版对应后台表
    private String templateSrcTable;
    //模版对应视图
    private String templateSrcView;
    //模版对应零时表
    private String templateTmpTable;
    //模版预处理程序（执行于校验程序前）
    private String templatePreProgram;
    //模版处理程序（将数据从临时表导入到真实表）
    private String templateProgram;
    //模版善后程序（执行于数据导入到真实表后）
    private String templateAfterProgram;
    //模版处理模式（覆盖式导入、增量导入）
    private String tempalteMode;
    //模版定义文件
    private String templateFile;
    //数据起始行号
    private int dataStartLine;
    //模版对应组合ID
    private String combinationId;
    //模版列定义
    private List<ColumnDef> colsdef = new ArrayList<ColumnDef>();
    private List<ComHeader> templateHeader = new ArrayList<ComHeader>();
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(DcmDataDisplayBean.class);
    private RichInputFile fileInput;
    private Map headerComponents = new LinkedHashMap();

    public DcmDataDisplayBean() {
        this.dataModel = new DcmDataTableModel();
        this.initTemplateData();
        this.initColsDef();
        this.initModelData();
        this.initCombination();
    }

    public CollectionModel getDataModel() {
        return this.dataModel;
    }

    public void valueChangeListener(ValueChangeEvent valueChangeEvent) {
        Map rowData = (Map)this.dataModel.getRowData();
        if (((DcmDataTableModel)this.dataModel).getSelectedRows().size() > 1) {
            String msg =
                DmsUtils.getMsg("dcm.msg.can_not_select_multiple_row");
            FacesMessage fm = new FacesMessage("", msg);
            FacesContext.getCurrentInstance().addMessage(null, fm);
            return;
        }
        if (null == rowData.get("OPERATION")) {
            rowData.put("OPERATION", DcmDataTableModel.OPERATE_UPDATE);
        }
    }

    public void rowSelectionListener(SelectionEvent selectionEvent) {
        // Add event code here...
        RichTable table = (RichTable)selectionEvent.getSource();
        RowKeySet rks = selectionEvent.getAddedSet();
        if (rks != null) {
            int setSize = rks.size();
            if (setSize == 0) {
                return;
            }
            Object rowKey = rks.iterator().next();
            table.setRowKey(rowKey);
        }
    }

    public String operation_new() {
        // Add event code here...
        return null;
    }

    public String operation_delete() {
        // Add event code here...
        return null;
    }

    public String operation_save() {
        // Add event code here...
        return null;
    }

    public String operation_reset() {
        // Add event code here...
        return null;
    }

    public void operation_import() {
        String filePath = this.uploadFile();
        this.fileInput.resetValue();
        if (null != filePath) {
            this.handleExcel(filePath);
        }
        return;
    }
    //获取当前的组合

    private String getCurCombinationRecord() {
        String comRecordId = null;
        if (this.combinationId != null) {
            ViewObject vo =
                ADFUtils.findIterator("DcmCombinationViewIterator").getViewObject();
            ViewCriteria vc = vo.createViewCriteria();
            ViewCriteriaRow vcr = vc.createViewCriteriaRow();
            ViewCriteriaItem item = vcr.ensureCriteriaItem("Id");
            item.setOperator("=");
            item.getValues().get(0).setValue(this.combinationId);
            vc.addRow(vcr);
            vo.applyViewCriteria(vc);
            vo.executeQuery();
            String combinationCode = null;
            if (vo.hasNext()) {
                Row row = vo.next();
                combinationCode = (String)row.getAttribute("Code");
            }
            if (null != combinationCode) {
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT ID FROM ").append("\"").append(combinationCode.toUpperCase()).append("\"");
                sql.append(" WHERE 1=1");
                for (Object headerCode : headerComponents.keySet()) {
                    sql.append(" AND ");
                    sql.append("\"").append(headerCode.toString().toUpperCase()).append("\"='").append(((RichSelectOneChoice)headerComponents.get(headerCode)).getValue()).append("'");
                }
                DBTransaction trans =
                    (DBTransaction)ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject().getApplicationModule().getTransaction();
                Statement stat = trans.createStatement(1);
                try {
                    ResultSet rs = stat.executeQuery(sql.toString());
                    if (rs.next()) {
                        comRecordId = rs.getString("ID");
                    }
                } catch (SQLException e) {
                    this._logger.severe(e);
                }
            }
        }
        return comRecordId;
    }
    //读取excel数据到零时表

    private void handleExcel(String fileName) {
        DBTransaction trans =
            (DBTransaction)ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject().getApplicationModule().getTransaction();
        String combinationRecord =
            ObjectUtils.toString(this.getCurCombinationRecord());
        //清空已有零时表数据
        try {
            trans.createStatement(0).execute("DELETE FROM \"" +
                                             this.templateTmpTable.toUpperCase() +
                                             "\" WHERE TEMPLATE_ID='" +
                                             this.curTemplateId +
                                             "' AND COM_RECORD_ID " +
                                             (this.combinationId == null ?
                                              "IS NULL" :
                                              ("='" + combinationRecord +
                                               "'")));
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        String operator =
            ((Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"))).getId();
        RowReader reader =
            new RowReader(trans, this.dataStartLine, this.curTemplateId,
                          combinationRecord, this.templateTmpTable,
                          this.colsdef.size(), operator);
        try {
            ExcelReaderUtil.readExcel(reader, fileName, true);
            reader.close();
        } catch (Exception e) {
            this._logger.severe(e);
            String msg = DmsUtils.getMsg("dcm.excel_handle_error");
            FacesMessage fm = new FacesMessage("", msg);
            FacesContext.getCurrentInstance().addMessage(null, fm);
        }

    }
    //文件上传

    private String uploadFile() {
        UploadedFile file = (UploadedFile)this.fileInput.getValue();
        if (!(file.getFilename().endsWith(".xls") ||
              file.getFilename().endsWith(".xlsx"))) {
            String msg = DmsUtils.getMsg("dcm.file_format_error");
            FacesMessage fm = new FacesMessage("", msg);
            FacesContext.getCurrentInstance().addMessage(null, fm);
            return null;
        }
        File dmsBaseDir = new File("DMS/" + this.templateName);
        if (!dmsBaseDir.exists()) {
            dmsBaseDir.mkdirs();
        }
        String fileName =
            "DMS/" + this.templateName + "/" + file.getFilename();
        try {
            InputStream inputStream = file.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(fileName);
            BufferedInputStream bufferedInputStream =
                new BufferedInputStream(inputStream);
            BufferedOutputStream bufferedOutputStream =
                new BufferedOutputStream(outputStream);
            byte[] buffer = new byte[10240];
            int bytesRead = 0;
            while ((bytesRead = bufferedInputStream.read(buffer, 0, 10240)) !=
                   -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }
            bufferedOutputStream.flush();
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            file.dispose();
        } catch (IOException e) {
            this._logger.severe(e);
            String msg = DmsUtils.getMsg("dcm.file_upload_error");
            FacesMessage fm = new FacesMessage("", msg);
            FacesContext.getCurrentInstance().addMessage(null, fm);
            return null;
        }
        return (new File(fileName)).getAbsolutePath();
    }

    public String operation_export() {
        // Add event code here...
        return null;
    }

    public String operation_download() {
        // Add event code here...
        return null;
    }
    //初始化模版信息

    private void initTemplateData() {
        this.curTemplateId =
                (String)ADFContext.getCurrent().getPageFlowScope().get("curTemplateId");
        ViewObject templateView =
            ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject();
        ViewCriteria vc = templateView.createViewCriteria();
        ViewCriteriaRow vcr = vc.createViewCriteriaRow();
        ViewCriteriaItem item = vcr.ensureCriteriaItem("Id");
        item.setOperator("=");
        item.getValues().get(0).setValue(this.curTemplateId);
        vc.addRow(vcr);
        templateView.applyViewCriteria(vc);
        templateView.executeQuery();
        if (templateView.hasNext()) {
            Row row = templateView.next();
            this.temlateReadOnly = (String)row.getAttribute("Readonly");
            this.templateSrcTable = (String)row.getAttribute("DbTable");
            this.templateSrcView = (String)row.getAttribute("DbView");
            this.templateTmpTable = (String)row.getAttribute("TmpTable");
            this.templatePreProgram = (String)row.getAttribute("PreProgram");
            this.templateProgram = (String)row.getAttribute("HandleProgram");
            this.templateAfterProgram =
                    (String)row.getAttribute("AfterProgram");
            this.tempalteMode = (String)row.getAttribute("HandleMode");
            this.templateFile = (String)row.getAttribute("TemplateFile");
            this.dataStartLine =
                    ((oracle.jbo.domain.Number)row.getAttribute("DataStartLine")).intValue();
            this.combinationId = (String)row.getAttribute("CombinationId");
            this.templateName = (String)row.getAttribute("Name");
        }
    }
    //初始化模版列定义信息

    private void initColsDef() {
        ViewObject colsView =
            ADFUtils.findIterator("DcmTemplateColumnViewIterator").getViewObject();
        ViewCriteria vc = colsView.createViewCriteria();
        ViewCriteriaRow vcr = vc.createViewCriteriaRow();
        ViewCriteriaItem item = vcr.ensureCriteriaItem("TemplateId");
        item.setOperator("=");
        item.getValues().get(0).setValue(this.curTemplateId);
        vc.addRow(vcr);
        colsView.applyViewCriteria(vc);
        colsView.executeQuery();
        while (colsView.hasNext()) {
            Row row = colsView.next();
            ColumnDef colDef = new ColumnDef();
            colDef.setLabel((String)row.getAttribute("ColumnLabel"));
            colDef.setCode((String)row.getAttribute("DbTableCol"));
            colDef.setIsPk((String)row.getAttribute("IsPk"));
            colDef.setReadonly((String)row.getAttribute("Readonly"));
            colDef.setType((String)row.getAttribute("DataType"));
            colDef.setVisible((String)row.getAttribute("Visible"));
            this.colsdef.add(colDef);
        }
        ((DcmDataTableModel)this.dataModel).setColsdef(this.colsdef);
    }
    //初始化模版数据

    private void initModelData() {
        List<Map> data = new ArrayList<Map>();
        DBTransaction dbTransaction =
            (DBTransaction)ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject().getApplicationModule().getTransaction();
        String sql = this.getQuerySql();
        PreparedStatement stat =
            dbTransaction.createPreparedStatement(sql, -1);
        ResultSet rs = null;
        try {
            rs = stat.executeQuery();
            while (rs.next()) {
                Map row = new HashMap();
                for (ColumnDef col : this.colsdef) {
                    row.put(col.getCode(),
                            rs.getString(col.getCode().toUpperCase()));
                }
                data.add(row);
            }
            this.dataModel.setWrappedData(data);
        } catch (SQLException e) {
            this._logger.severe(e);
        }
    }
    //获取数据查询语句

    private String getQuerySql() {
        StringBuffer sql_select = new StringBuffer(200);
        StringBuffer sql_from = new StringBuffer(100);
        StringBuffer sql_where = new StringBuffer(100);
        if (null != this.templateSrcView) {
            sql_select.append("SELECT ROW_ID");
            sql_from.append(" ").append("FROM ").append(this.templateSrcView);
        } else {
            sql_select.append("SELECT ROWID ROW_ID");
            sql_from.append(" ").append("FROM ").append(this.templateSrcTable);
        }
        for (ColumnDef col : this.colsdef) {
            sql_select.append(",\"").append(col.getCode().toUpperCase()).append("\"");
        }
        sql_where.append(" ORDER BY IDX");
        return sql_select.toString() + sql_from.toString() +
            sql_where.toString();
    }
    //初始化组合信息

    private void initCombination() {
        if (null != this.combinationId) {
            ViewObject vo =
                ADFUtils.findIterator("DcmComVsViewIterator").getViewObject();
            vo.setNamedWhereClauseParam("combinationId", this.combinationId);
            vo.executeQuery();
            while (vo.hasNext()) {
                Row row = vo.next();
                ComHeader header = new ComHeader();
                header.setName((String)row.getAttribute("Name"));
                header.setIsAuthority((String)row.getAttribute("IsAuthority"));
                header.setSrcTable((String)row.getAttribute("Source"));
                header.setValueSetId((String)row.getAttribute("ValueSetId"));
                header.setValues(this.fetchHeaderValueList(header));
                header.setCode((String)row.getAttribute("Code"));
                this.templateHeader.add(header);
            }
        }
    }
    //获取某一值集值列表

    private List<SelectItem> fetchHeaderValueList(ComHeader header) {
        List<SelectItem> values = new ArrayList<SelectItem>();
        DBTransaction dbTransaction =
            (DBTransaction)ADFUtils.findIterator("DcmTemplateViewIterator").getViewObject().getApplicationModule().getTransaction();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT CODE,MEANING FROM ");
        sql.append(header.getSrcTable());
        sql.append(" WHERE LOCALE=? ORDER BY IDX");
        PreparedStatement stat =
            dbTransaction.createPreparedStatement(sql.toString(), -1);
        try {
            stat.setString(1,
                           (String)((Person)ADFContext.getCurrent().getSessionScope().get("cur_user")).getLocale());
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                SelectItem item = new SelectItem();
                item.setLabel(rs.getString("MEANING"));
                item.setValue(rs.getString("CODE"));
                values.add(item);
            }
        } catch (SQLException e) {
            this._logger.severe(e);
        }
        return values;
    }

    public String getCombinationId() {
        return combinationId;
    }

    public void setTemplateHeader(List<ComHeader> templateHeader) {
        this.templateHeader = templateHeader;
    }

    public List<ComHeader> getTemplateHeader() {
        return templateHeader;
    }

    public void setFileInput(RichInputFile fileInput) {
        this.fileInput = fileInput;
    }

    public RichInputFile getFileInput() {
        return fileInput;
    }

    public void headerSelectChangeListener(ValueChangeEvent valueChangeEvent) {
        // Add event code here...
    }

    public Map getHeaderComponents() {
        return headerComponents;
    }
}
