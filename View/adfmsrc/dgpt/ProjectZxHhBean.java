package dgpt;

import common.ADFUtils;
import common.DmsLog;
import common.DmsUtils;
import common.JSFUtils;

import common.lov.DmsComBoxLov;
import common.lov.ValueSetRow;

import dcm.DcmDataDisplayBean;
import dcm.PcColumnDef;
import dcm.PcDataTableModel;
import dcm.PcExcel2003WriterImpl;
import dcm.PcExcel2007WriterImpl;
import dcm.SPRowReader;

import dms.login.Person;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.output.RichPanelCollection;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.UploadedFile;

import org.hexj.excelhandler.reader.ExcelReaderUtil;

public class ProjectZxHhBean {
    private Person curUser;
    private RichPanelCollection panelaCollection;
    private CollectionModel dataModel;
    private List<PcColumnDef> pcColsDef = new ArrayList<PcColumnDef>();

    //日志
    private static ADFLogger _logger =ADFLogger.createADFLogger(DcmDataDisplayBean.class);

    //是否是2007及以上格式
    private boolean isXlsx = true;
    //页面绑定组件
    private RichPopup dataExportWnd;
    private RichPopup errorWindow;
    private Boolean isIncrement = true;
    private RichPopup dataImportWnd;
    private RichInputFile fileInput;
    private RichPopup statusWindow;
    private RichPopup adminBlockPop;
    private RichTable subTable;
    private RichTable subTable2;


    public ProjectZxHhBean() {
        super();
        this.curUser = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
        if("10000".equals(this.curUser.getId())){
            isManager = true;
            isEDITABLE = true;
        }else{
            isManager = false;
            isEDITABLE = false;
        }
        isSelected = true;
        this.dataModel = new PcDataTableModel();
        List<Map> d = new ArrayList<Map>();
        this.dataModel.setWrappedData(d);
        this.initList();
    }
    private String year;
    private List<SelectItem> yearList;
    private String entity;
    private String pLine;
    private String pname;
    private List<SelectItem> pnameList;
    private DmsComBoxLov proLov;
    private String version;
    private List<SelectItem> versionList;
    private String proType;
    private String hLine;
    private String yLine;
    private String department;
    private String pStart;
    private String pEnd;
    private String connectId;
    public static final String TYPE_ZZX="ZX";
    private String isBlock;
    private boolean isManager;
    DmsLog dmsLog = new DmsLog();
    private boolean isEDITABLE ;
    private boolean isSelected;
    
    private void initList(){
        this.yearList = queryYears("HLS_YEAR_C");
        this.pnameList = proValues("PRO_PLAN_COST_HEADER_HH","PROJECT_NAME");
        this.initProLov(pnameList);
        this.versionList = queryValues1("PRO_PLAN_COST_HEADER_HH","VERSION");
    }
    
    private void initProLov(List<SelectItem> pnameList){
        List<ValueSetRow> vsl = new ArrayList<ValueSetRow>();
        for(SelectItem sim : pnameList){
            ValueSetRow vsr = new ValueSetRow(sim.getLabel(),sim.getLabel(),sim.getLabel());
            vsl.add(vsr);
        }
        this.proLov = new DmsComBoxLov(vsl);
    }
    
    private LinkedHashMap<String,String> getLabelMap(){
        LinkedHashMap<String,String> labelMap = new LinkedHashMap<String,String>();
        
        if(pStart == null || pEnd == null){
            return  labelMap;    
        }
        
        labelMap.put("WBS", "WBS");
        labelMap.put("NETWORK", "NETWORK");
        labelMap.put("WORK_CODE", "WORK_CODE");
        // 里程碑
        labelMap.put("MILESTONE_CODE", "MILESTONE_CODE");
        labelMap.put("MILESTONE", "MILESTONE");
        labelMap.put("WORK","WORK");
        labelMap.put("TERM_CODE", "TERM_CODE");
        labelMap.put("TERM","TERM");
        labelMap.put("COST_DETAIL", "COST_DETAIL");
        labelMap.put("CENTER","CENTER");
        labelMap.put("WORK_TYPE","WORK_TYPE");
        labelMap.put("BOM_CODE","BOM_CODE");
        labelMap.put("UNIT","UNIT");
        labelMap.put("PLAN_COST","PLAN_COST");
        labelMap.put("OCCURRED", "OCCURRED");
        labelMap.put("SURPLUS", "SURPLUS");
    //        labelMap.put("PLAN_QUANTITY", "PLAN_QUANTITY");
    //        labelMap.put("PLAN_AMOUNT", "PLAN_AMOUNT");
    //        labelMap.put("OCCURRED_QUANTITY", "OCCURRED_QUANTITY");
    //        labelMap.put("OCCURRED_AMOUNT", "OCCURRED_AMOUNT");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM");
        List<Date> monthList;
        Date start;
        Date end ;
        try {
            start = sdf.parse(pStart);
            end = sdf.parse(pEnd);
            monthList = this.findDates(start, end);
            int i ;
            for(i=0 ; i < monthList.size() ; i++){
                labelMap.put("M"+i, "Y"+sdf.format(monthList.get(i)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        labelMap.put("SUM_AFTER_JUL","SUM_AFTER_JUL");
    //        labelMap.put("LGF_NUM", "LGF_NUM");
    //        labelMap.put("LGF_TYPE", "LGF_TYPE");
        //构造列导出
        this.pcColsDef.clear();
        List<String> list = new ArrayList<String>();
        list.add("WBS");
        list.add("网络号");
        list.add("作业号");
        list.add("里程碑状态");
        list.add("作业活动");
        list.add("预算项编码");
        list.add("预算项");
        list.add("预算科目");
        list.add("工作中心");
        list.add("作业类型");
        list.add("物料编码");
        list.add("单位");
        list.add("计划成本");
        list.add("已发生（截至上年9月）");
        list.add("未发生");
    //        list.add("预计总数量");
    //        list.add("预计总金额");
    //        list.add("已发生数量");
    //        list.add("已发生金额");
        list.add("上年10月");
        list.add("上年11月");
        list.add("上年12月");
        list.add("本年1月");
        list.add("本年2月");
        list.add("本年3月");
        list.add("本年4月");
        list.add("本年5月");
        list.add("本年6月");
        list.add("本年7月");
        list.add("本年8月");
        list.add("本年9月");
        list.add("本年10月");
        list.add("本年11月");
        list.add("本年12月");
        list.add("下年1月");
        list.add("下年2月");
        list.add("下年3月");
        list.add("下年4月");
        list.add("下年5月");
        list.add("下年6月");
        list.add("下年7月以后");
        int i =0;
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            if(i<11){
                PcColumnDef newCol = new PcColumnDef(list.get(i),map.getValue(),false,"");
                this.pcColsDef.add(newCol);
            }else if(i>=11&&i<35){
                PcColumnDef newCol = new PcColumnDef(list.get(i),map.getValue(),false,"NUMBER");
                this.pcColsDef.add(newCol);
            }else{
                break;
            }
            i++;
        }
        this.pcColsDef.add(new PcColumnDef("ROW_ID","ROW_ID",false,""));
        ((PcDataTableModel)this.dataModel).setPcColsDef(this.pcColsDef);
        return labelMap;
    }
    
    public void createTableModel(){
        //行的Map
        LinkedHashMap<String,String> labelMap = getLabelMap();
        //
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = this.querySql(labelMap);
        List<Map> data = new ArrayList<Map>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql.toString());
            while(rs.next()){
                Map row = new HashMap();
                for(Map.Entry entry : labelMap.entrySet()){
                    if(entry.getValue().equals("PLAN_COST") || entry.getValue().equals("OCCURRED") || 
                       entry.getValue().equals("SUM_AFTER_JUL") || entry.getValue().toString().startsWith("Y")){
                        row.put(entry.getValue(),this.getPrettyNumber(rs.getString(entry.getValue().toString())));
                    }else{
                        row.put(entry.getValue(),rs.getString(entry.getValue().toString()));
                    }
                }
                row.put("ROW_ID", rs.getString("ROW_ID"));
                row.put("LGF_NUM", rs.getString("LGF_NUM"));
                row.put("LGF_TYPE", rs.getString("LGF_TYPE"));
                row.put("PLAN_QUANTITY", rs.getString("PLAN_QUANTITY"));
                row.put("PLAN_AMOUNT", rs.getString("PLAN_AMOUNT"));
                row.put("OCCURRED_QUANTITY", rs.getString("OCCURRED_QUANTITY"));
                row.put("OCCURRED_AMOUNT", rs.getString("OCCURRED_AMOUNT"));
                row.put("CONNECT_ID", connectId);
                data.add(row);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.dataModel.setWrappedData(data);
        ((PcDataTableModel)this.dataModel).setLabelMap(labelMap);
        this.selectIsEditable();
    }
    
    public static String getPrettyNumber(String number) {  
        if(number == null) return "";
        if(number.equals("0.0")){
            number = "";    
        }
        if(number.startsWith(".")){
            number = "0" + number;    
        }
        while(number.contains(".")&&number.endsWith("0")){
            number = number.substring(0,number.length()-2);    
        }
        return number;  
    }
    //查询语句
    public String querySql(LinkedHashMap<String,String> labelMap){
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        
        for(Map.Entry<String,String> entry : labelMap.entrySet()){
            sql.append(entry.getValue()).append(",");
        }
        sql.append("ROWID AS ROW_ID,LGF_NUM,LGF_TYPE,PLAN_QUANTITY,PLAN_AMOUNT,")
            .append("OCCURRED_QUANTITY,OCCURRED_AMOUNT FROM PRO_PLAN_COST_BODY_HH WHERE CONNECT_ID = '")
            .append(connectId).append("'");
        sql.append(" AND DATA_TYPE = '").append(this.TYPE_ZZX).append("' ORDER BY WBS,NETWORK,TO_NUMBER(WORK_CODE)");
        return sql.toString();
    }
    //时间段
    public static List<Date> findDates(Date dBegin, Date dEnd) {  
            List lDate = new ArrayList();  
            lDate.add(dBegin);  
            Calendar calBegin = Calendar.getInstance();  
            // 使用给定的 Date 设置此 Calendar 的时间    
            calBegin.setTime(dBegin);  
            Calendar calEnd = Calendar.getInstance();  
            // 使用给定的 Date 设置此 Calendar 的时间    
            calEnd.setTime(dEnd);  
            // 测试此日期是否在指定日期之后    
            while (dEnd.after(calBegin.getTime())) {  
                // 根据日历的规则，为给定的日历字段添加或减去指定的时间量    
                calBegin.add(Calendar.MONTH, 1);  
                lDate.add(calBegin.getTime());
            }  
            return lDate;  
        }  
    //年份下拉列表
    private List<SelectItem> queryYears(String source){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT CODE,MEANING FROM "+ source;
        List<SelectItem> values = new ArrayList<SelectItem>();
        ResultSet rs;

        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                SelectItem sim = new SelectItem(rs.getString("CODE"),rs.getString("MEANING"));
                values.add(sim);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }
    
    //项目名称下拉列表
    private List<SelectItem> proValues(String source,String col){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "";
        if(this.curUser.getId().equals("10000")){
            sql= "SELECT DISTINCT P."+col+" FROM "+source+" P " + 
                " WHERE P.DATA_TYPE='"+this.TYPE_ZZX+"'";
        }else{
            sql = "SELECT DISTINCT P."+col+" FROM "+source+" P WHERE P.PROJECT_NAME IN (" + 
            "SELECT T.PRO_CODE||'-'||T.PRO_DESC FROM SAP_DMS_PROJECT_PRIVILEGE_V T WHERE ID = '"+this.curUser.getId()+"'"+
            ") AND P.DATA_TYPE =\'"+this.TYPE_ZZX+"\'";
        }
        List<SelectItem> values = new ArrayList<SelectItem>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                SelectItem sim = new SelectItem(rs.getString(col),rs.getString(col));
                values.add(sim);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }
    
    //版本下拉列表
    private List<SelectItem> queryValues1(String source,String col){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT DISTINCT "+col+",VERSION_NAME FROM "+source+" WHERE DATA_TYPE =\'"+this.TYPE_ZZX+"\'";
        List<SelectItem> values = new ArrayList<SelectItem>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                SelectItem sim = new SelectItem(rs.getString(col),rs.getString(col)+"-"+rs.getString("VERSION_NAME"));
                values.add(sim);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }
    
    //项目名称下拉框change
    public void projectChange(ValueChangeEvent valueChangeEvent) {
        pname =(String) valueChangeEvent.getNewValue();
        
        if(year!=null&&pname!=null){
            DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            String sql = "SELECT DISTINCT VERSION,VERSION_NAME FROM PRO_PLAN_COST_HEADER_HH WHERE DATA_TYPE =\'"+this.TYPE_ZZX+"\'"+
                " AND PROJECT_NAME='"+pname+"' AND HLS_YEAR = '"+this.year+"'";
            List<SelectItem> values = new ArrayList<SelectItem>();
            ResultSet rs;
            try {
                rs = stat.executeQuery(sql);
                while(rs.next()){
                    SelectItem sim = new SelectItem(rs.getString("VERSION"),rs.getString("VERSION")+"-"+rs.getString("VERSION_NAME"));
                    values.add(sim);
                }
                rs.close();
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.versionList = values;
        }
        if(year==null||version==null||pname==null){
                return;
        }else{
            isSelected = false;
            this.queryData();
            this.createTableModel();
        }
    }
    
    //版本下拉框change
    public void versionChange(ValueChangeEvent valueChangeEvent) {
        version =(String) valueChangeEvent.getNewValue();
        if(year==null||version==null||pname==null){
            return;
        }else{
            isSelected = false;
            this.queryData();
            this.createTableModel();
        }
    }
    
    //年下拉框change
    public void yearChange(ValueChangeEvent valueChangeEvent) {
        year = (String)valueChangeEvent.getNewValue();
        if(year!=null&&pname!=null){
            DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            String sql = "SELECT DISTINCT VERSION,VERSION_NAME FROM PRO_PLAN_COST_HEADER_HH WHERE DATA_TYPE =\'"+this.TYPE_ZZX+"\'"+
                " AND PROJECT_NAME='"+pname+"' AND HLS_YEAR = '"+this.year+"'";
            List<SelectItem> values = new ArrayList<SelectItem>();
            ResultSet rs;
            try {
                rs = stat.executeQuery(sql);
                while(rs.next()){
                    SelectItem sim = new SelectItem(rs.getString("VERSION"),rs.getString("VERSION")+"-"+rs.getString("VERSION_NAME"));
                    values.add(sim);
                }
                rs.close();
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.versionList = values;
        }
        if(year==null||version==null||pname==null){
            return;
        }else{
            isSelected = false;
            this.queryData();
            this.createTableModel();
        }
    }
    
    public void queryData(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT ENTITY_NAME,PRODUCT_LINE,PROJECT_TYPE,INDUSTRY_LINE,BUSINESS_LINE,DEPARTMENT,CONNECT_ID,PROJECT_START,PROJECT_END,"
                    + "IS_BLOCK FROM PRO_PLAN_COST_HEADER_HH WHERE VERSION = \'"+version+"\'";
        sql = sql +" AND HLS_YEAR=\'"+year+"\'";
        sql = sql + " AND PROJECT_NAME=\'"+pname+"\'";
        sql = sql + " AND DATA_TYPE = \'"+this.TYPE_ZZX+"\'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            entity="";
            pLine="";
            proType="";
            hLine="";
            yLine="";
            department="";
            connectId="";
            isBlock = "false";
            while(rs.next()){
                entity = rs.getString("ENTITY_NAME");
                pLine = rs.getString("PRODUCT_LINE");
                proType = rs.getString("PROJECT_TYPE");
                hLine = rs.getString("INDUSTRY_LINE");
                yLine = rs.getString("BUSINESS_LINE");
                department = rs.getString("DEPARTMENT");
                connectId=rs.getString("CONNECT_ID");
                pStart = rs.getString("PROJECT_START");
                pEnd = rs.getString("PROJECT_END");
                isBlock = rs.getString("IS_BLOCK");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //行选中时设置当前选中行
    public void rowSelectionListener(SelectionEvent selectionEvent) {
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
    
    //保存操作
    public void operation_save() {
        //清空临时表和错误表数据
        this.deleteTempAndError();
        //数据存入临时表
        this.goToTemp();
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        //校验
        if(this.validation()){
            this.inputPro();
            dmsLog.operationLog(this.curUser.getAcc(),this.TYPE_ZZX+"_"+this.connectId,this.getCom(),"UPDATE");
            for(Map<String,String> rowdata : modelData){
                if("UPDATE".equals(rowdata.get("OPERATION"))){
                    rowdata.put("OPERATION", null);
                }
            }
        }else{
            this.showErrorPop();
        }
    }
    
    public void goToTemp(){
        //数据插入临时表
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        StringBuffer sql = new StringBuffer();
        StringBuffer sql_value = new StringBuffer();
        sql_value.append(" VALUES(");
        sql.append("INSERT INTO PRO_PLAN_COST_BODY_TEMP(") ;
        LinkedHashMap<String,String> map = this.getLabelMap();
        for(Map.Entry<String,String> entry : map.entrySet()){
            sql.append(entry.getValue()+",");
            sql_value.append("?,");
        }
        sql.append("CONNECT_ID,CREATED_BY,DATA_TYPE,ROW_NO,ROW_ID,");
        sql.append("LGF_NUM,LGF_TYPE,PLAN_QUANTITY,PLAN_AMOUNT,OCCURRED_QUANTITY,OCCURRED_AMOUNT)");
        sql_value.append("\'"+connectId+"\',\'"+this.curUser.getId()+"\',\'"+this.TYPE_ZZX+"\',?,?,");
        sql_value.append("?,?,?,?,?,?)");
        PreparedStatement stmt = trans.createPreparedStatement(sql.toString()+sql_value.toString(), 0);
        int rowNum = 1;
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        for(Map<String,String> rowdata : modelData){
            //if("UPDATE".equals(rowdata.get("OPERATION"))){
                try {
                    int i = 1;
                    for(Map.Entry entry : map.entrySet()){
                        stmt.setString(i++, rowdata.get(entry.getValue()));
                    }
                    stmt.setInt(i, rowNum);
                    stmt.setString(i+1, rowdata.get("ROW_ID"));
                    stmt.setString(i+2, rowdata.get("LGF_NUM"));
                    stmt.setString(i+3, rowdata.get("LGF_TYPE"));
                    stmt.setString(i+4, rowdata.get("PLAN_QUANTITY"));
                    stmt.setString(i+5, rowdata.get("PLAN_AMOUNT"));
                    stmt.setString(i+6, rowdata.get("OCCURRED_QUANTITY"));
                    stmt.setString(i+7, rowdata.get("OCCURRED_AMOUNT"));
                    rowNum++;
                    stmt.addBatch();
                    stmt.executeBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }        
        //}
        trans.commit();
    }
    public void deleteTempAndError(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        //删除临时表数据
        String sqldelete = "DELETE FROM PRO_PLAN_COST_BODY_TEMP T WHERE T.CREATED_BY = \'"+this.curUser.getId()+"\'";
        Statement st = trans.createStatement(DBTransaction.DEFAULT);
        try {
            st.executeUpdate(sqldelete);
            trans.commit();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //清空错误表数据
        String sqlError = "DELETE FROM PRO_PLAN_COST_ERROR T WHERE T.CREATED_BY = \'"+this.curUser.getId()+"\'";
        Statement sta = trans.createStatement(DBTransaction.DEFAULT);
        try {
            sta.executeUpdate(sqlError);
            trans.commit();
            sta.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void inputPro(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.ZZX_INPUTPRO_HH(?)}", 0);
        try {
            cs.setString(1,this.curUser.getId() );
            cs.execute();
            trans.commit();
            cs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //选中行，修改
    public void valueChangeLinstener(ValueChangeEvent valueChangeEvent) {
        Map rowMap = (Map)this.dataModel.getRowData();
        if (((PcDataTableModel)this.dataModel).getSelectedRows().size() > 1) {
            String msg =DmsUtils.getMsg("dcm.msg.can_not_select_multiple_row");
            JSFUtils.addFacesInformationMessage(msg);
            return;
        }
        if(rowMap.get("OPERATION") == null){
            rowMap.put("OPERATION", PcDataTableModel.getOPERATE_UPDATE());    
        }                
    }
    
    //校验程序
    public boolean validation(){
        boolean flag = true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.ZZX_VALIDATION_HH(?,?,?)}", 0);
        try {
            cs.setString(1, this.curUser.getId());
            cs.setString(2, this.TYPE_ZZX);
            cs.registerOutParameter(3, Types.VARCHAR);
            cs.execute();
            if("N".equals(cs.getString(3))){
                flag = false;
            }
            cs.close();
            trans.commit();
        } catch (SQLException e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }
    
    public void reset(ActionEvent actionEvent) {
        DmsUtils.getDmsApplicationModule().getTransaction().rollback();
        this.queryData();
        this.createTableModel();
    }
    //导出excel
    public void operation_export(FacesContext facesContext, OutputStream outputStream) {
        this.dataExportWnd.cancel();
        String type = this.isXlsx ? "xlsx" : "xls";
        try {
            if("xls".equals(type)){
                PcExcel2003WriterImpl writer = new PcExcel2003WriterImpl(
                                                   this.querySql(this.getLabelMap()),
                                                   "在执行项目",
                                                    this.pcColsDef,
                                                    outputStream);
            
                writer.writeToFile();
            }else{
                PcExcel2007WriterImpl writer = new PcExcel2007WriterImpl(
                                                    this.querySql(this.getLabelMap()),
                                                    2,this.pcColsDef);
                writer.process(outputStream, "在执行项目");
                outputStream.flush();
            }
        } catch (Exception e) {
            this._logger.severe(e);
        } 
        dmsLog.operationLog(this.curUser.getAcc(),this.TYPE_ZZX+"_"+this.connectId,this.getCom(),"EXPORT");
    }
    
    private String getCom(){
        String text = this.year+"_"+this.entity+"_"+this.hLine+"_"+this.yLine+"_"+this.department+"_"+
                      this.pLine+"_"+this.pname+"_"+this.version+"_"+this.proType;
        return text;
    }
    
    //导出文件名
    public String getExportDataExcelName(){
        if(isXlsx){
            return "在执行项目_"+this.connectId+"_杭和.xlsx";
        }else{
            return "在执行项目_"+this.connectId+"_杭和.xls";
        }
    }
    
    //更改为冻结状态
    public void beBlocked(ActionEvent actionEvent) {
        //清空临时表和错误表数据
        this.deleteTempAndError();
        //数据存入临时表
        this.goToTemp();
        if(this.validation()){
            this.inputPro();
            String sql = "UPDATE PRO_PLAN_COST_HEADER_HH SET (IS_BLOCK) = 'true' WHERE HLS_YEAR = \'"+year;
            sql = sql + "\' AND PROJECT_NAME =\'"+pname+"\' AND VERSION=\'"+version+"\'";
            DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            int flag =-1;
            try {
                flag = stat.executeUpdate(sql);
                trans.commit();
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(flag!=-1){
                isBlock = "true";
            }
        }else{
            this.showErrorPop();
        }
    }
    //显示错误框
    public void showErrorPop(){
        ViewObject vo = ADFUtils.findIterator("ProPlanCostViewIterator").getViewObject();
        vo.setNamedWhereClauseParam("dataType", this.TYPE_ZZX);
        vo.executeQuery();
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        this.errorWindow.show(ph);
    }
    //显示冻结状态信息
    public void showBlockStatusPop(){
            ViewObject vo = ADFUtils.findIterator("PTBlockStatusIterator").getViewObject();
            vo.setNamedWhereClauseParam("dataType", this.TYPE_ZZX);
            vo.setNamedWhereClauseParam("userId", this.curUser.getId());
            vo.executeQuery();
            RichPopup.PopupHints ph = new RichPopup.PopupHints();
            this.statusWindow.show(ph);
    }
    //显示admin用户冻结状态信息
    public void showAdminStatusPop(){
        ViewObject vo = ADFUtils.findIterator("adminBlockStatusIterator").getViewObject();
        vo.setNamedWhereClauseParam("dataType", this.TYPE_ZZX);
    //        vo.setNamedWhereClauseParam("userAcc", this.curUser.getAcc());
        vo.executeQuery();
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        this.adminBlockPop.show(ph);
    }
    //用户解冻，更改冻结状态
    public void outBlock(ActionEvent actionEvent) {
        String sql = "UPDATE PRO_PLAN_COST_HEADER_HH SET (IS_BLOCK) = 'false' WHERE HLS_YEAR = \'"+year;
        sql = sql + "\' AND PROJECT_NAME =\'"+pname+"\' AND VERSION=\'"+version+"\'";
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        int flag =-1;
        try {
            flag = stat.executeUpdate(sql);
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(flag!=-1){
            isBlock = "false";
        }
    }

    public void operation_import(ActionEvent actionEvent) {
        this.dataImportWnd.cancel();
        //表头为空
        if(this.year==null||this.pname==null||this.version==null){
            JSFUtils.addFacesErrorMessage("请选择表头信息");
            return;
        }
        //上传文件为空
        if (null == this.fileInput.getValue()) {
            JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.plz_select_import_file"));
            return;
        }
        //获取文件上传路径
        String filePath = this.uploadFile();
        
        if (null == filePath) {
            return;
        }
        
        //读取excel数据到数据库临时表
        try {
            if (!this.handleExcel(filePath, connectId)) {
            return;
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.fileInput.resetValue();
        //校验程序
        if(this.validation_import()){
            DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            StringBuffer sql = new StringBuffer();
            sql.append("UPDATE PRO_PLAN_COST_BODY_TEMP T SET(T.PLAN_QUANTITY,T.PLAN_AMOUNT,T.OCCURRED_QUANTITY,T.OCCURRED_AMOUNT,T.LGF_NUM,T.LGF_TYPE) ")
                .append("=(SELECT P.PLAN_QUANTITY,P.PLAN_AMOUNT,P.OCCURRED_QUANTITY,P.OCCURRED_AMOUNT,P.LGF_NUM,P.LGF_TYPE FROM PRO_PLAN_COST_BODY_HH P WHERE P.CONNECT_ID = '").append(this.connectId)
                .append("' AND T.ROW_ID = P.ROWID)").append(" WHERE T.CONNECT_ID='").append(this.connectId).append("'");
            
            try {
                stat.executeUpdate(sql.toString());
                trans.commit();
                stat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(this.validation()){
                this.inputPro_import();
    //                this.createTableModel();
            }else{
                this.showErrorPop();
            }
        }else {
            //若出现错误则显示错误信息提示框
            //JSFUtils.addFacesErrorMessage("WBS等字段不可修改");
            this.showErrorPop();
        }
        //刷新数据
        this.createTableModel();
        dmsLog.operationLog(this.curUser.getAcc(),this.TYPE_ZZX+"_"+this.connectId,this.getCom(),"IMPORT");
    }
    
    //校验程序
    public boolean validation_import(){
        boolean flag = true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.ZZXIMPORT_VALIDATION_HH(?,?,?,?)}", 0);
        try {
            cs.setString(1, this.curUser.getId());
            cs.setString(2, this.TYPE_ZZX);
            cs.setString(3,this.connectId);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();
            if("N".equals(cs.getString(4))){
                flag = false;
            }
            cs.close();
            trans.commit();
        } catch (SQLException e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }
    
    //导入导入
    public void inputPro_import(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.ZZXIMPORT_INPUTPRO_HH(?,?)}", 0);
        try {
            cs.setString(1,this.curUser.getId() );
            cs.setString(2,this.connectId);
            cs.execute();
            trans.commit();
            cs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //读取excel数据到临时表
    private boolean handleExcel(String fileName, String curComRecordId) throws SQLException {
        DBTransaction trans =(DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        //清空已有临时表数据
        this.deleteTempAndError();
        UploadedFile file = (UploadedFile)this.fileInput.getValue();
        String fname = file.getFilename();
        String name = fname.substring(fname.indexOf("_")+1, fname.indexOf("."));
        if(!name.equals(this.connectId)){
            JSFUtils.addFacesErrorMessage("请选择正确的文件");
            return false;
        }
        SPRowReader spReader = new SPRowReader(trans,2,this.connectId,this.pcColsDef,this.curUser.getId(),this.TYPE_ZZX,name);
        try {
                ExcelReaderUtil.readExcel(spReader, fileName, true);
                spReader.close();
            } catch (Exception e) {
                this._logger.severe(e);
                JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.excel_handle_error"));
                return false;
            }
        return true;
    }
    
    //文件上传
    private String uploadFile() {
        UploadedFile file = (UploadedFile)this.fileInput.getValue();
        if (!(file.getFilename().endsWith(".xls") ||
              file.getFilename().endsWith(".xlsx"))) {
            String msg = DmsUtils.getMsg("dcm.file_format_error");
            JSFUtils.addFacesErrorMessage(msg);
            return null;
        }
        String fileExtension =file.getFilename().substring(file.getFilename().lastIndexOf("."));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(new Date());
        File dmsBaseDir = new File("DMS/UPLOAD/在执行项目" );
        //如若文件路径不存在则创建文件目录
        if (!dmsBaseDir.exists()) {
            dmsBaseDir.mkdirs();
        }
        String fileName = "DMS/UPLOAD/" + "在执行项目" + "/"+file.getFilename();
        try {
            InputStream inputStream = file.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(fileName);
            BufferedInputStream bufferedInputStream =new BufferedInputStream(inputStream);
            BufferedOutputStream bufferedOutputStream =
                new BufferedOutputStream(outputStream);
            byte[] buffer = new byte[10240];
            int bytesRead = 0;
            while ((bytesRead = bufferedInputStream.read(buffer, 0, 10240)) !=-1) {
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
            JSFUtils.addFacesErrorMessage(msg);
            return null;
        }
        return (new File(fileName)).getAbsolutePath();
    }
    
    //显示项目冻结状态框
    public void blockStatusPop(ActionEvent actionEvent) {
        if(this.curUser.getId().equals("10000")){
            this.showAdminStatusPop();
        }else{
            this.showBlockStatusPop();
        }
    }

    //查询是否为可编辑此项目
    public void selectIsEditable(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT EDITABLE FROM SAP_DMS_PROJECT_PRIVILEGE_V WHERE ID = '"+this.curUser.getId()+"' " +
            "AND PRO_CODE ||'-'||PRO_DESC = '"+this.pname+"'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                if("Y".equals(rs.getString("EDITABLE"))){
                    isEDITABLE= true;
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void goUnBlock(ActionEvent actionEvent) {
        if(this.subTable.getSelectedRowKeys() == null) return ;
        RowKeySet rsk = this.subTable.getSelectedRowKeys();
        ViewObject vo = ADFUtils.findIterator("adminBlockStatusIterator").getViewObject();
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Iterator itr = rsk.iterator();
        try {
            while(itr.hasNext()){
                List ls = (List)itr.next();
                Key key =(Key)ls.get(0);
                Row row = vo.getRow(key);
                StringBuffer sql = new StringBuffer();
                if(row.getAttribute("IsBlock").equals("已冻结")){
                    sql.append("UPDATE PRO_PLAN_COST_HEADER_HH SET (IS_BLOCK) = 'false' WHERE HLS_YEAR = \'"+row.getAttribute("Code"))
                        .append("' AND PROJECT_NAME = '"+row.getAttribute("ProjectName"))
                        .append("' AND IS_BLOCK='true' ").append("AND DATA_TYPE = '"+this.TYPE_ZZX+"'");
                    stat.executeUpdate(sql.toString());
                }
                if(row.getAttribute("ProjectName").equals(this.pname)){
                    this.isBlock = "false";
                }
            }
            stat.close();
            trans.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        vo.executeQuery();
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.subTable);
    }
    
    public void goUnBlock2(ActionEvent actionEvent) {
        if(this.subTable2.getSelectedRowKeys() == null) return ;
        RowKeySet rsk = this.subTable2.getSelectedRowKeys();
        ViewObject vo = ADFUtils.findIterator("adminBlockStatusIterator").getViewObject();
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Iterator itr = rsk.iterator();
        try {
            while(itr.hasNext()){
                List ls = (List)itr.next();
                Key key =(Key)ls.get(0);
                Row row = vo.getRow(key);
                StringBuffer sql = new StringBuffer();
                if(row.getAttribute("IsBlock").equals("已冻结")){
                    sql.append("UPDATE PRO_PLAN_COST_HEADER_HH SET (IS_BLOCK) = 'false' WHERE HLS_YEAR = \'"+row.getAttribute("Code"))
                        .append("' AND PROJECT_NAME = '"+row.getAttribute("ProjectName"))
                        .append("' AND IS_BLOCK='true' ").append("AND DATA_TYPE = '"+this.TYPE_ZZX+"'");
                    stat.executeUpdate(sql.toString());
                }
                if(row.getAttribute("ProjectName").equals(this.pname)){
                    this.isBlock = "false";
                }
            }
            stat.close();
            trans.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        vo.executeQuery();
        AdfFacesContext.getCurrentInstance().addPartialTarget(this.subTable2);
    }
    
    public void setYear(String year) {
        this.year = year;
    }

    public String getYear() {
        return year;
    }

    public void setYearList(List<SelectItem> yearList) {
        this.yearList = yearList;
    }

    public List<SelectItem> getYearList() {
        return yearList;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }

    public void setPLine(String pLine) {
        this.pLine = pLine;
    }

    public String getPLine() {
        return pLine;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getPname() {
        return pname;
    }

    public void setPnameList(List<SelectItem> pnameList) {
        this.pnameList = pnameList;
    }

    public List<SelectItem> getPnameList() {
        return pnameList;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersionList(List<SelectItem> versionList) {
        this.versionList = versionList;
    }

    public List<SelectItem> getVersionList() {
        return versionList;
    }

    public void setProType(String proType) {
        this.proType = proType;
    }

    public String getProType() {
        return proType;
    }

    public void setHLine(String hLine) {
        this.hLine = hLine;
    }

    public String getHLine() {
        return hLine;
    }

    public void setYLine(String yLine) {
        this.yLine = yLine;
    }

    public String getYLine() {
        return yLine;
    }

    public void setPanelaCollection(RichPanelCollection panelaCollection) {
        this.panelaCollection = panelaCollection;
    }

    public RichPanelCollection getPanelaCollection() {
        return panelaCollection;
    }

    public void setDataModel(CollectionModel dataModel) {
        this.dataModel = dataModel;
    }

    public CollectionModel getDataModel() {
        return this.dataModel;
    }

    public void setPcColsDef(List<PcColumnDef> pcColsDef) {
        this.pcColsDef = pcColsDef;
    }

    public List<PcColumnDef> getPcColsDef() {
        return pcColsDef;
    }

    public void setPStart(String pStart) {
        this.pStart = pStart;
    }

    public String getPStart() {
        return this.pStart;
    }

    public void setPEnd(String pEnd) {
        this.pEnd = pEnd;
    }

    public String getPEnd() {
        return pEnd;
    }

    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }

    public String getConnectId() {
        return this.connectId;
    }

    public void setIsXlsx(boolean isXlsx) {
        this.isXlsx = isXlsx;
    }

    public boolean isIsXlsx() {
        return isXlsx;
    }

    public void setDataExportWnd(RichPopup dataExportWnd) {
        this.dataExportWnd = dataExportWnd;
    }

    public RichPopup getDataExportWnd() {
        return dataExportWnd;
    }

    public void setIsBlock(String isBlock) {
        this.isBlock = isBlock;
    }

    public String getIsBlock() {
        return isBlock;
    }
    
    public void setErrorWindow(RichPopup errorWindow) {
        this.errorWindow = errorWindow;
    }

    public RichPopup getErrorWindow() {
        return errorWindow;
    }

    public void errorPop(ActionEvent actionEvent) {
        this.showErrorPop();
    }

    public void setIsManager(boolean isManager) {
        this.isManager = isManager;
    }

    public boolean isIsManager() {
        return isManager;
    }
    public void setIsIncrement(Boolean isIncrement) {
        this.isIncrement = isIncrement;
    }

    public Boolean getIsIncrement() {
        return isIncrement;
    }
    
    public boolean getIsReplaceDefault(){
        return !isIncrement;
    }
    public void setIsReplaceDefault(boolean flag){
       
    }
    
    public void setDataImportWnd(RichPopup dataImportWnd) {
        this.dataImportWnd = dataImportWnd;
    }

    public RichPopup getDataImportWnd() {
        return dataImportWnd;
    }

    public void inputFile_valueChangeListener(ValueChangeEvent event) {
        this.fileInput.setValue(event.getNewValue());
    }

    public void setFileInput(RichInputFile fileInput) {
        this.fileInput = fileInput;
    }

    public RichInputFile getFileInput() {
        return fileInput;
    }
    public void setStatusWindow(RichPopup statusWindow) {
        this.statusWindow = statusWindow;
    }

    public RichPopup getStatusWindow() {
        return statusWindow;
    }

    public void setAdminBlockPop(RichPopup adminBlockPop) {
        this.adminBlockPop = adminBlockPop;
    }

    public RichPopup getAdminBlockPop() {
        return adminBlockPop;
    }

    public void setIsEDITABLE(boolean isEDITABLE) {
        this.isEDITABLE = isEDITABLE;
    }
    
    public boolean isIsEDITABLE() {
        return isEDITABLE;
    }

    public void setProLov(DmsComBoxLov proLov) {
        this.proLov = proLov;
    }

    public DmsComBoxLov getProLov() {
        return proLov;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isIsSelected() {
        return isSelected;
    }

    public void setSubTable(RichTable subTable) {
        this.subTable = subTable;
    }

    public RichTable getSubTable() {
        return subTable;
    }

    public void setSubTable2(RichTable subTable2) {
        this.subTable2 = subTable2;
    }

    public RichTable getSubTable2() {
        return subTable2;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }
}