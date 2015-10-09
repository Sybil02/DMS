package dcm.combinantion;


import common.ADFUtils;
import common.DmsUtils;
import common.JSFUtils;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.faces.event.ActionEvent;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;

import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowIterator;

import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;

import oracle.jbo.server.DBTransaction;

import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

import team.epm.dcm.view.DcmCombinationViewImpl;
import team.epm.dcm.view.DcmCombinationViewRowImpl;

public class CombinationBean {
    private static ADFLogger logger =
        ADFLogger.createADFLogger(CombinationBean.class);
    private RichPopup popup;
    private RichInputText combinationName;
    private RichInputText combinationCode;
    private RichTable valueSetTable;
    private RichTable combinationTable;
    private RowKeySet selectedRows = new RowKeySetImpl();
    private RichOutputText msgField;

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }

    private void refreshCombination(DcmCombinationViewRowImpl comRow) throws SQLException {
        //存储值集编码，用于表的列名
        List<String> columns = new ArrayList<String>();
        //存储值集的表名
        List<String> srcTables = new ArrayList<String>();
        //当前语言
        String locale = ADFContext.getCurrent().getLocale().toString();
        //
        DCIteratorBinding combinationIter =
            ADFUtils.findIterator("DcmCombinationViewIterator");
        //
        DCIteratorBinding valueSetIter =
            ADFUtils.findIterator("DmsValueSetViewIterator");
        ViewObject valuelookup = valueSetIter.getViewObject();
        valuelookup.executeQuery();
        //获取当前选中的行
        RowIterator comVsRow = comRow.getDcmComVsView();
        while (comVsRow.hasNext()) {
            Row relationrow = comVsRow.next();
            String valueSetId = (String)relationrow.getAttribute("ValueSetId");
            Key key = new Key(new Object[] { valueSetId, locale });
            Row[] valuesetRow = valuelookup.findByKey(key, 1);
            columns.add((String)valuesetRow[0].getAttribute("Code"));
            srcTables.add((String)valuesetRow[0].getAttribute("Source"));
        }
        DcmCombinationViewImpl vo =
            (DcmCombinationViewImpl)combinationIter.getViewObject();
        vo.refreshCombinationRecord((String)comRow.getAttribute("Code"),
                                    srcTables, columns);
    }

    public void refreshRecord(ActionEvent actionEvent) {
        DCIteratorBinding combinationIter =
            ADFUtils.findIterator("DcmCombinationViewIterator");
        if (combinationIter.getCurrentRow() == null) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.combination.refresh_record_warning"));
        }
        try {
            this.refreshCombination((DcmCombinationViewRowImpl)combinationIter.getCurrentRow());
            JSFUtils.addFacesInformationMessage(DmsUtils.getMsg("dms.common.operation_success"));
        } catch (SQLException e) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("common.operation_failed_with_exception"));
        }
    }

    public void createCombination(ActionEvent actionEvent) {
        Pattern p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");
        String combinationName = (String)this.combinationName.getValue();
        String combinationCode = (String)this.combinationCode.getValue();
        if (combinationName == null || combinationName.trim().length() < 1) {
            this.msgField.setValue(DmsUtils.getMsg("dms.combination.msg.name_not_null"));
            return;
        } else if (combinationCode == null ||
                   combinationCode.trim().length() < 1 ||
                   combinationCode.length() > 26 ||
                   !p.matcher(combinationCode).find()) {
            this.msgField.setValue(DmsUtils.getMsg("dcm.combination.msg.code_principle"));
            return;
        } else if (this.valueSetTable.getSelectedRowKeys() == null ||
                   this.valueSetTable.getSelectedRowKeys().size() < 1) {
            this.msgField.setValue(DmsUtils.getMsg("dcm.combination.msg.select_valueset"));
            return;
        } else {
            StringBuffer sql = new StringBuffer();
            sql.append("create table \"").append(combinationCode.toUpperCase()).append("\"");
            sql.append("(id varchar2(32)");
            ViewObject combinationVo =
                ADFUtils.findIterator("DcmCombinationViewIterator").getViewObject();
            ViewObject comVsVo =
                ADFUtils.findIterator("DcmComVsViewIterator").getViewObject();
            Row comRow = combinationVo.createRow();
            comRow.setAttribute("Name", combinationName);
            comRow.setAttribute("Code", combinationCode.toUpperCase());
            combinationVo.insertRow(comRow);
            Iterator itr = this.valueSetTable.getSelectedRowKeys().iterator();
            RowSetIterator rowSetIterator =
                ADFUtils.findIterator("DmsValueSetViewIterator").getRowSetIterator();
            while (itr.hasNext()) {
                List key = (List)itr.next();
                Row row = rowSetIterator.getRow((Key)key.get(0));
                Row comVsRow = comVsVo.createRow();
                comVsRow.setAttribute("CombinationId",
                                      comRow.getAttribute("Id"));
                comVsRow.setAttribute("ValueSetId", row.getAttribute("Id"));
                comVsVo.insertRow(comVsRow);
                sql.append(",\"").append(row.getAttribute("Code")).append("\" varchar2(100)");
            }
            sql.append(",CONSTRAINT PK_").append(combinationCode.toUpperCase()).append(" PRIMARY KEY(ID))");
            try {
                ((DBTransaction)combinationVo.getApplicationModule().getTransaction()).executeCommand(sql.toString());
                combinationVo.getApplicationModule().getTransaction().commit();
                this.refreshCombination((DcmCombinationViewRowImpl)comRow);
                this.popup.cancel();
                AdfFacesContext.getCurrentInstance().addPartialTarget(this.combinationTable);
            } catch (Exception e) {
                combinationVo.getApplicationModule().getTransaction().rollback();
                this.logger.severe(e);
                this.msgField.setValue(DmsUtils.getMsg("dcm.combination.msg.create_table_error"));
                return;
            }
        }
    }

    public void setCombinationName(RichInputText combinationName) {
        this.combinationName = combinationName;
    }

    public RichInputText getCombinationName() {
        return combinationName;
    }

    public void setCombinationCode(RichInputText combinationCode) {
        this.combinationCode = combinationCode;
    }

    public RichInputText getCombinationCode() {
        return combinationCode;
    }

    public void setValueSetTable(RichTable valueSetTable) {
        this.valueSetTable = valueSetTable;
    }

    public RichTable getValueSetTable() {
        return valueSetTable;
    }

    public void setCombinationTable(RichTable combinationTable) {
        this.combinationTable = combinationTable;
    }

    public RichTable getCombinationTable() {
        return combinationTable;
    }

    public void setSelectedRows(RowKeySet selectedRows) {
        this.selectedRows = selectedRows;
    }

    public RowKeySet getSelectedRows() {
        return selectedRows;
    }

    public void setMsgField(RichOutputText msgField) {
        this.msgField = msgField;
    }

    public RichOutputText getMsgField() {
        return msgField;
    }
}
