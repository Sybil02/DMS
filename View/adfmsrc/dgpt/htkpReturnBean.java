package dgpt;

import common.ADFUtils;
import common.DmsLog;
import common.DmsUtils;

import common.JSFUtils;

import common.lov.DmsComBoxLov;

import common.lov.ValueSetRow;

import dcm.HtkpRowReader;
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

import java.text.SimpleDateFormat;

import java.util.ArrayList;
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

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.component.rich.input.RichInputFile;

import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;

import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import oracle.jbo.server.ViewRowImpl;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.UploadedFile;

import org.hexj.excelhandler.reader.ExcelReaderUtil;

public class htkpReturnBean {
    private RichPopup dataExportWnd;
    private RichInputFile fileInput;
    private RichPopup dataImportWnd;
    private RichPopup errorWindow;
    private RichTable subTable;
    private RichTable subTable2;
    private RichPopup ptBlockPoup;
    private RichPopup adminBlockPoup;

    public htkpReturnBean() {
        super();
        this.curUser = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
        if("10000".equals(this.curUser.getId())){
            isEDITABLE = true;
            isManager = true;
        }else{
            isEDITABLE = false;
            isManager = false;
        }
        this.dataModel = new PcDataTableModel();
        List<Map> d = new ArrayList<Map>();
        this.dataModel.setWrappedData(d);
        this.initList();
    }
    private Person curUser;
    private String year;
    private String yearNeed;
    private List<SelectItem> yearList;
    private String pName;
    private List<SelectItem> pNameList;
    private String version;
    private List<SelectItem> versionList;
    private String connectId;
    private String entity;
    private String hLine;
    private String yLine;
    private String cLine;
    private String pType;
    private String entityName;
    private String hLineName;
    private String yLineName;
    private String cLineName;
    private String pTypeName;
    private DmsComBoxLov proLov;
    private CollectionModel dataModel;
    LinkedHashMap<String,String> lineMap = new LinkedHashMap<String,String>();
    private List<PcColumnDef> pcColsDef = new ArrayList<PcColumnDef>();
    //是否是2007及以上格式
    private boolean isXlsx = true;
    //是否已选表头
    private boolean isSelected = false;
    
    private boolean isBlock;
    private boolean isManager;
    DmsLog dmsLog = new DmsLog();
    //表体下拉框列表
    private List<SelectItem> detailList;
    private List<SelectItem> taxList;
    private boolean isEDITABLE ;

    public void initList(){
        this.versionList = queryValues("version");
        this.yearList = queryValues("year");
        this.pNameList = queryPname();
        this.initProLov(pNameList);
        this.detailList = queryList("HLS_CONTRACT_DETAIL_C");
        this.taxList = queryList("HLS_TAX_RATE_C");
    }
    private void initProLov(List<SelectItem> pnameList){
        List<ValueSetRow> vsl = new ArrayList<ValueSetRow>();
        for(SelectItem sim : pnameList){
            ValueSetRow vsr = new ValueSetRow(sim.getLabel(),sim.getLabel(),sim.getLabel());
            vsl.add(vsr);
        }
        this.proLov = new DmsComBoxLov(vsl);
    }
    //表头，版本和年份
    public List<SelectItem> queryValues(String mark){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "";
        List<SelectItem> values = new ArrayList<SelectItem>();
        if("version".equals(mark)){
            sql = "SELECT DISTINCT D.HLS_VERSION_C||'-'||V.MEANING MEANING,V.CODE FROM DCM_COMBINATION_17 D,HLS_VERSION_C V " +
                " WHERE D.HLS_VERSION_C= V.CODE ORDER BY V.CODE";
        }else if("year".equals(mark)){
            sql = "SELECT DISTINCT D.HLS_YEAR_C CODE,Y.MEANING FROM DCM_COMBINATION_17 D,HLS_YEAR_C Y  " +
                "WHERE D.HLS_YEAR_C = Y.CODE ORDER BY Y.MEANING";
        }
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
    //表头。项目名称
    public List<SelectItem> queryPname(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "";
        List<SelectItem> values = new ArrayList<SelectItem>();
        if("10000".equals(this.curUser.getId())){
            sql = "SELECT DISTINCT C.BH_USER_PRO_C1 CODE,B.MEANING FROM DCM_COMBINATION_17 C,HLS_HTKP_PROJECT_V B WHERE " +
                "  C.BH_USER_PRO_C1 = B.CODE";
        }else{
            sql = "SELECT DISTINCT C.BH_USER_PRO_C1 CODE,B.MEANING FROM DCM_COMBINATION_17 C,HLS_HTKP_PROJECT_V B WHERE  B.MEANING IN" +
                "(SELECT T.PRO_CODE || '-' || T.PRO_DESC FROM SAP_DMS_PROJECT_PRIVILEGE T " +
                " WHERE T.ATTRIBUTE7 = '"+this.curUser.getAcc()+"'" +
                " OR T.ATTRIBUTE5 IN(" +
                " SELECT G.GROUP_ID FROM DMS_USER_GROUP G WHERE G.USER_ID='"+this.curUser.getId()+"')) " +
                "AND C.BH_USER_PRO_C1 = B.CODE";
        }
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
    
    public List<SelectItem> queryList(String source){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
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
    
    //查询是否为可编辑此项目
    public void selectIsEditable(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT ATTRIBUTE7 FROM SAP_DMS_PROJECT_PRIVILEGE WHERE " +
            "PRO_CODE ||'-'||PRO_DESC = '"+this.pName+"'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                if(this.curUser.getAcc().equals(rs.getString("ATTRIBUTE7"))){
                    isEDITABLE= true;
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("isEditable:"+this.isEDITABLE);
    }
    
    //查询数据
    public String queryData(LinkedHashMap<String,String> labelMap){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String name = this.pName.substring(0, pName.indexOf("-",pName.indexOf("-")+1));
        //在合同开票组合中查找id
        String sql = "SELECT ID FROM DCM_COMBINATION_17 WHERE HLS_YEAR_C='"+this.year+"' " +
            "AND HLS_VERSION_C='"+this.version+"' AND BH_USER_PRO_C1='"+name+"'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            connectId = "";
            while(rs.next()){
                connectId = rs.getString("ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //查询数据
        StringBuffer qSql = new StringBuffer();
        qSql.append("SELECT ");
        for(Map.Entry<String,String> entry : labelMap.entrySet()){
            qSql.append(entry.getValue()).append(",");
        }
        qSql.append("ROWID AS ROW_ID FROM CONT_INVOICE_RETURN_BUDGET_5 WHERE COM_RECORD_ID='").append(this.connectId).append("'");
        //查询是否冻结
        String isBlockSql = "SELECT COUNT(1) AS COUNTS FROM DMS_HTKP_BLOCK WHERE COM_RECORD_ID ='"+this.connectId+"'";
        Statement isBlockStat = trans.createStatement(DBTransaction.DEFAULT);
        ResultSet isBlockRs;
        try {
            isBlockRs = isBlockStat.executeQuery(isBlockSql);
            while(isBlockRs.next()){
            System.out.println(Integer.parseInt(isBlockRs.getString("COUNTS")));
                if(Integer.parseInt(isBlockRs.getString("COUNTS"))>0){
                    System.out.println(">0");
                    isBlock = true;
                }else{
                    isBlock = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return qSql.toString();
    }
    
    public String queryData2(LinkedHashMap<String,String> labelMap){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String name = this.pName.substring(0, pName.indexOf("-",pName.indexOf("-")+1));
        //在合同开票组合中查找id
        String sql = "SELECT ID FROM DCM_COMBINATION_17 WHERE HLS_YEAR_C='"+this.year+"' " +
            "AND HLS_VERSION_C='"+this.version+"' AND BH_USER_PRO_C1='"+name+"'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            connectId = "";
            while(rs.next()){
                connectId = rs.getString("ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //查询数据
        StringBuffer qSql = new StringBuffer();
        qSql.append("SELECT CONTRACT_BILLING_DETAIL,TAX_RATE,RESPONSIBLE," +
            "ENTITY,INDUSTRY_LINE,BUSINESS_LINE,PRODUCT_LINE,PROJECT_TYPE,OCCUR_COST,LAST_OCT,LAST_NOV,LAST_DEC,")
            .append("CUR_JAN,CUR_FEB,CUR_MAR,CUR_APR,CUR_MAY,CUR_JUN,CUR_JUL,CUR_AUG,CUR_SEP,CUR_OCT,CUR_NOV,CUR_DEC,")
            .append("NEXT_JAN,NEXT_FEB,NEXT_MAR,NEXT_APR,NEXT_MAY,NEXT_JUN,NEXT_OTHERS,");
        qSql.append("ROW_ID FROM cont_invoice_return_budget_5_v WHERE COM_RECORD_ID='").append(this.connectId).append("'");
        return qSql.toString();
    }
    
    public LinkedHashMap<String,String> getLabelMap(){
        LinkedHashMap<String,String> labelMap = new LinkedHashMap<String,String>();
        labelMap.put("合同开票回款明细", "CONTRACT_BILLING_DETAIL");
        labelMap.put("开票计划税率", "TAX_RATE");
        labelMap.put("回款责任人", "RESPONSIBLE");
        labelMap.put("实体", "ENTITY");
        labelMap.put("行业线", "INDUSTRY_LINE");
        labelMap.put("业务线", "BUSINESS_LINE");
        labelMap.put("产品线", "PRODUCT_LINE");
        labelMap.put("合同类型", "PROJECT_TYPE");
        labelMap.put("已发生", "OCCUR_COST");
        labelMap.put("上年10月", "LAST_OCT");
        labelMap.put("上年11月", "LAST_NOV");
        labelMap.put("上年12月", "LAST_DEC");
        labelMap.put("本年1月", "CUR_JAN");
        labelMap.put("本年2月", "CUR_FEB");
        labelMap.put("本年3月", "CUR_MAR");
        labelMap.put("本年4月", "CUR_APR");
        labelMap.put("本年5月", "CUR_MAY");
        labelMap.put("本年6月", "CUR_JUN");
        labelMap.put("本年7月", "CUR_JUL");
        labelMap.put("本年8月", "CUR_AUG");
        labelMap.put("本年9月", "CUR_SEP");
        labelMap.put("本年10月", "CUR_OCT");
        labelMap.put("本年11月", "CUR_NOV");
        labelMap.put("本年12月", "CUR_DEC");
        labelMap.put("下年1月", "NEXT_JAN");
        labelMap.put("下年2月", "NEXT_FEB");
        labelMap.put("下年3月", "NEXT_MAR");
        labelMap.put("下年4月", "NEXT_APR");
        labelMap.put("下年5月", "NEXT_MAY");
        labelMap.put("下年6月", "NEXT_JUN");
        labelMap.put("下年7月以后", "NEXT_OTHERS");
        boolean isReadonly = false;
        int flag = 1;
        this.pcColsDef.clear();
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            if(flag<=8){
                PcColumnDef newCol = new PcColumnDef(map.getKey(),map.getValue(),isReadonly,"");
                this.pcColsDef.add(newCol);
            }else{
                PcColumnDef newCol = new PcColumnDef(map.getKey(),map.getValue(),isReadonly,"NUMBER");
                this.pcColsDef.add(newCol);
            }
            flag++;
            
        }
        this.pcColsDef.add(new PcColumnDef("ROW_ID","ROW_ID",false,""));
        ((PcDataTableModel)this.dataModel).setPcColsDef(this.pcColsDef);
        return labelMap;
    }
    //构建数据
    public void createTableModel(){
        this.getLine();
        LinkedHashMap<String,String> labelMap = getLabelMap();
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = queryData(labelMap);
        List<Map> data = new ArrayList<Map>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql.toString());
            while(rs.next()){
                Map row = new HashMap();
                for(Map.Entry entry : labelMap.entrySet()){
                    if(entry.getValue().equals("ENTITY")){
                        row.put(entry.getValue(), entityName);
                    }else if(entry.getValue().equals("INDUSTRY_LINE")){
                        row.put(entry.getValue(), hLineName);
                    }else if(entry.getValue().equals("BUSINESS_LINE")){
                        row.put(entry.getValue(), yLineName);
                    }else if(entry.getValue().equals("PRODUCT_LINE")){
                        row.put(entry.getValue(), cLineName);
                    }else if(entry.getValue().equals("PROJECT_TYPE")){
                        row.put(entry.getValue(), pTypeName);
                    }else if(entry.getValue().equals("OCCUR_COST") || entry.getValue().toString().startsWith("LAST") || 
                       entry.getValue().toString().startsWith("CUR") || entry.getValue().toString().startsWith("NEXT")||
                        entry.getValue().equals("TAX_RATE") ){
                        row.put(entry.getValue(),this.getPrettyNumber(rs.getString(entry.getValue().toString())));
                    }else{
                        row.put(entry.getValue(),rs.getString(entry.getValue().toString()));
                    }
                }
                row.put("ROW_ID", rs.getString("ROW_ID"));
                row.put("COM_RECORD_ID", connectId);
                data.add(row);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.dataModel.setWrappedData(data);
        ((PcDataTableModel)this.dataModel).setLabelMap(labelMap);
    }
    //获取行业线等信息
    public void getLine(){
        String name = this.pName.substring(0, pName.indexOf("-",pName.indexOf("-")+1));
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT DISTINCT T.S_ENTITY,T.S_Entity_Name,T.industry_line_code,T.industry_line,T.business_line_code," +
                    "T.business_line,T.product_line_code,T.product_line,T.project_type_code,T.project_type_desc " +
                     "FROM HLS_HTKP_ALL T "+" WHERE T.pspnr='"+name+"'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            entity = "";
            hLine = "";
            yLine = "";
            cLine = "";
            pType = "";
            entityName = "";
            hLineName = "";
            yLineName = "";
            cLineName = "";
            pTypeName = "";
            while(rs.next()){
                entity = rs.getString("S_ENTITY");
                hLine = rs.getString("industry_line_code");
                yLine = rs.getString("business_line_code");
                cLine = rs.getString("product_line_code");
                pType = rs.getString("project_type_code");
                entityName = rs.getString("S_Entity_Name");
                hLineName = rs.getString("industry_line");
                yLineName = rs.getString("business_line");
                cLineName = rs.getString("product_line");
                pTypeName = rs.getString("project_type_desc");
            }
            lineMap.put("ENTITY", entity);
            lineMap.put("INDUSTRY_LINE", hLine);
            lineMap.put("BUSINESS_LINE", yLine);
            lineMap.put("PRODUCT_LINE", cLine);
            lineMap.put("PROJECT_TYPE", pType);
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //年下拉框值改变
    public void yearChange(ValueChangeEvent valueChangeEvent) {
        year = (String)valueChangeEvent.getNewValue();
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT MEANING FROM HLS_YEAR_C WHERE CODE = '"+this.year+"'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                this.yearNeed = rs.getString("MEANING");
            }
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(year==null||pName==null||version==null){
            isSelected = false;
            return;
        }else{
            isSelected = true;  
        }
        System.out.println("San of ---------");
        this.selectIsEditable();
        this.createTableModel();
    }
    //项目下拉框值改变
    public void pnameChange(ValueChangeEvent valueChangeEvent) {
        pName = (String) valueChangeEvent.getNewValue();
        System.out.println(pName);
        if(year==null||pName==null||version==null){
            isSelected = false;
            return;
        }else{
            isSelected = true;  
        }
        System.out.println("San of ---------");
        this.selectIsEditable();
        this.createTableModel();
    }
    //版本下拉框值改变
    public void versionChange(ValueChangeEvent valueChangeEvent) {
        version = (String) valueChangeEvent.getNewValue();
        if(year==null||pName==null||version==null){
            isSelected = false;
            return;
        }else{
            isSelected = true;  
        }
        System.out.println("San of ---------");

        this.selectIsEditable();
        this.createTableModel();
    }
    //调整数字显示格式
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
    //保存
    public void operation_save(ActionEvent actionEvent) {
        //清空临时表数据
        this.deleteTempAndError();
        this.copyToTemp();
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        if(this.validation()){
            this.inputPro();
            dmsLog.operationLog(this.curUser.getAcc(),"RETURN_"+this.connectId,this.getCom(),"UPDATE");
            for(Map<String,String> rowdata : modelData){
                rowdata.put("OPERATION", null);
            }
            this.after();
        }else{
            //JSFUtils.addFacesErrorMessage("校验不通过");
            this.showErrorPop();
        }
        this.createTableModel();
    }
    //新增行
    public void operation_new(ActionEvent actionEvent) {
        this.getLine();
        List<Map> modelData = (List<Map>) this.dataModel.getWrappedData();
        Map newRow = new HashMap();
        for(PcColumnDef col : this.pcColsDef){
            if(col.getDbTableCol().equals("ENTITY")){
                newRow.put(col.getDbTableCol(), entityName);
            }else if(col.getDbTableCol().equals("INDUSTRY_LINE")){
                newRow.put(col.getDbTableCol(), hLineName);      
            }else if(col.getDbTableCol().equals("BUSINESS_LINE")){
                newRow.put(col.getDbTableCol(), yLineName);                                                    
            }else if(col.getDbTableCol().equals("PRODUCT_LINE")){
                newRow.put(col.getDbTableCol(), cLineName);                                                    
            }else if(col.getDbTableCol().equals("PROJECT_TYPE")){
                newRow.put(col.getDbTableCol(), pTypeName);                                                   
            }else{
                newRow.put(col.getDbTableCol(), null);
            }     
        }
        newRow.put("OPERATION",PcDataTableModel.OPERATE_CREATE);
        modelData.add(0, newRow);
    }  
    //删除
    public void operation_delete(ActionEvent actionEvent) {
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        RowKeySet keySet =
            ((PcDataTableModel)this.dataModel).getSelectedRows();
        for (Object key : keySet) {
            Map rowData = (Map)this.dataModel.getRowData(key);
            //若为新增操作则直接从数据集删除数据
            if (PcDataTableModel.OPERATE_CREATE.equals(rowData.get("OPERATION"))) {
                modelData.remove(rowData);
            } 
            //若为更新或数据未修改则直接将数据集数据标记为删除
            else if (PcDataTableModel.OPERATE_UPDATE.equals(rowData.get("OPERATION")) ||
                       null == rowData.get("OPERATION")) {
                rowData.put("OPERATION", PcDataTableModel.OPERATE_DELETE);
            }
            //已经为删除状态的数据无需做任何处理
        }
    }
    //取消
    public void reset(ActionEvent actionEvent) {
        DmsUtils.getDmsApplicationModule().getTransaction().rollback();
        this.createTableModel();
    }
    //获取下拉框选择信息
    private String getCom(){
        String text = this.year+"_"+this.pName+"_"+this.version;
        return text;
    }
    //保存到临时表
    public void copyToTemp(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        StringBuffer sql = new StringBuffer();
        StringBuffer sql_value = new StringBuffer();
        sql.append("INSERT INTO CONT_INVOICE_RETURN_BUDGET_5_T (");
        sql_value.append(" VALUES(");
        LinkedHashMap<String,String> map = this.getLabelMap();
        int last = map.size()+1;
        for(Map.Entry<String,String> entry : map.entrySet()){
            sql.append(entry.getValue()+",");
            sql_value.append("?,");
        }
        sql.append("CREATED_BY,ROW_ID,ROW_NUM,OPERATION,COM_RECORD_ID)");
        sql_value.append("'"+this.curUser.getId()+"',?,?,?,'"+this.connectId+"')");
        PreparedStatement stmt = trans.createPreparedStatement(sql.toString()+sql_value.toString(), 0);
        //获取数据
        int rowNum = 1;
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        try {
            for(Map<String,String> rowdata : modelData){
            stmt.setString(last, rowdata.get("ROW_ID"));
            stmt.setInt(last+1,rowNum);
            rowNum++;
            if(PcDataTableModel.OPERATE_UPDATE.equals(rowdata.get("OPERATION"))){
                stmt.setString(last+2,PcDataTableModel.OPERATE_UPDATE);
            }else if(PcDataTableModel.OPERATE_CREATE.equals(rowdata.get("OPERATION"))){
                stmt.setString(last+2,PcDataTableModel.OPERATE_CREATE);
            }else if(PcDataTableModel.OPERATE_DELETE.equals(rowdata.get("OPERATION"))){
                stmt.setString(last+2,PcDataTableModel.OPERATE_DELETE);
            }else{
                stmt.setString(last+2,PcDataTableModel.OPERATE_UPDATE);
            }
            //if(null!=rowdata.get("OPERATION")){
                int i =1;
                for(Map.Entry<String,String> entry : map.entrySet()){
                    if(entry.getValue().equals("ENTITY")){
                        stmt.setString(i++, entity);
                    }else if(entry.getValue().equals("INDUSTRY_LINE")){
                        stmt.setString(i++, hLine);      
                    }else if(entry.getValue().equals("BUSINESS_LINE")){
                        stmt.setString(i++, yLine);                                                    
                    }else if(entry.getValue().equals("PRODUCT_LINE")){
                        stmt.setString(i++, cLine);                                                    
                    }else if(entry.getValue().equals("PROJECT_TYPE")){
                        stmt.setString(i++, pType);                                                   
                    }else{
                        stmt.setString(i++ , rowdata.get(entry.getValue()));
                    }
                }
                stmt.addBatch();
                stmt.executeBatch();
            //}
            }
            stmt.close();
            trans.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } 
    }
    
    //清空临时表
    public void deleteTempAndError(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "DELETE FROM CONT_INVOICE_RETURN_BUDGET_5_T WHERE COM_RECORD_ID='"+this.connectId+"' " +
            "AND CREATED_BY='"+this.curUser.getId()+"'";
        try {
            stat.executeUpdate(sql);
            stat.close();
            trans.commit();
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
    
    //校验程序
    public boolean validation(){
        boolean flag = true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.HTKPRETURN_VALIDATION(?,?)}", 0);
        try {
            cs.setString(1, this.curUser.getId());
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            if("N".equals(cs.getString(2))){
                flag = false;
            }
            cs.close();
        } catch (SQLException e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }
    
    //导入程序
    public void inputPro(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.HTKPRETURN_INPUTPRO(?,?)}", 0);
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
    
    //善后程序
    public void after(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        String name = this.pName.substring(0, pName.indexOf("-",pName.indexOf("-")+1));
        //项目名称
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.HTKPRETURN_AFTER(?,?,?,?,?)}", 0);
        try {
            cs.setString(1,this.curUser.getId() );
            cs.setString(2,this.connectId);
            cs.setString(3, name);
            cs.setString(4,this.yearNeed);
            cs.setString(5,version);
            cs.execute();
            trans.commit();
            cs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //导出
    public void operation_export(FacesContext facesContext,
                                 OutputStream outputStream) {
        this.dataExportWnd.cancel();
        String type = this.isXlsx ? "xlsx" : "xls";
        try {
            if("xls".equals(type)){
                PcExcel2003WriterImpl writer = new PcExcel2003WriterImpl(
                                                    this.queryData2(this.getLabelMap()),
                                                   "年度预算-合同开票回款（在执行）",
                                                    this.pcColsDef,
                                                    outputStream);
                writer.writeToFile();
            }else{
                PcExcel2007WriterImpl writer = new PcExcel2007WriterImpl(
                                                    this.queryData2(this.getLabelMap()),
                                                    2,this.pcColsDef);
                writer.process(outputStream, "年度预算-合同开票回款（在执行）");
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        dmsLog.operationLog(this.curUser.getAcc(),"RETURN_"+this.connectId,this.getCom(),"EXPORT");
    }
    
    //导出文件名
    public String getExportDataExcelName(){
        if(isXlsx){
            return "年度预算-合同开票回款（在执行）_"+this.connectId+".xlsx";
        }else{
            return "年度预算-合同开票回款（在执行）_"+this.connectId+".xls";
        }
    }
    
    //导入
    public void operation_import(ActionEvent actionEvent) {
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        this.dataImportWnd.cancel();
        //表头为空
        if(this.year==null||this.pName==null||this.version==null){
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
        if(this.validation()){
            //如果校验成功，调用导入程序
            this.import_inputPro();
            this.after();
            dmsLog.operationLog(this.curUser.getAcc(),"RETURN_"+this.connectId,this.getCom(),"IMPORT");
        }else{
            //JSFUtils.addFacesErrorMessage("校验不通过");
            //显示错误窗口
            this.showErrorPop();
        }
        this.createTableModel(); 
    }
    
    //导入程序
    public void import_inputPro(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.HTKPRETURNIMPORT_INPUTPRO(?,?)}", 0);
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
        File dmsBaseDir = new File("DMS/UPLOAD/合同开票回款" );
        //如若文件路径不存在则创建文件目录
        if (!dmsBaseDir.exists()) {
            dmsBaseDir.mkdirs();
        }
        String fileName = "DMS/UPLOAD/" + "合同开票回款" + "/"+file.getFilename();
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
            e.printStackTrace();
            String msg = DmsUtils.getMsg("dcm.file_upload_error");
            JSFUtils.addFacesErrorMessage(msg);
            return null;
        }
        return (new File(fileName)).getAbsolutePath();
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
        HtkpRowReader spReader = new HtkpRowReader(trans,2,this.connectId,this.pcColsDef,this.curUser.getId(), name,this.lineMap);
        try {
                ExcelReaderUtil.readExcel(spReader, fileName, true);
                spReader.close();
            } catch (Exception e) {
                e.printStackTrace();
                JSFUtils.addFacesErrorMessage(DmsUtils.getMsg("dcm.excel_handle_error"));
                return false;
            }
        return true;
    }

    public void errorPop(ActionEvent actionEvent) {
        this.showErrorPop();
    }
    
    //显示错误框
    public void showErrorPop(){
        ViewObject vo = ADFUtils.findIterator("ProPlanCostViewIterator").getViewObject();
        vo.setNamedWhereClauseParam("dataType", "RETURN");
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        vo.executeQuery();
        this.errorWindow.show(ph);
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

    public void setPName(String pName) {
        this.pName = pName;
    }

    public String getPName() {
        return pName;
    }

    public void setPNameList(List<SelectItem> pNameList) {
        this.pNameList = pNameList;
    }

    public List<SelectItem> getPNameList() {
        return pNameList;
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

    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }

    public String getConnectId() {
        return connectId;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
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

    public void setCLine(String cLine) {
        this.cLine = cLine;
    }

    public String getCLine() {
        return cLine;
    }

    public void setPType(String pType) {
        this.pType = pType;
    }

    public String getPType() {
        return pType;
    }

    public void setDataModel(CollectionModel dataModel) {
        this.dataModel = dataModel;
    }

    public CollectionModel getDataModel() {
        return dataModel;
    }

    public void setPcColsDef(List<PcColumnDef> pcColsDef) {
        this.pcColsDef = pcColsDef;
    }

    public List<PcColumnDef> getPcColsDef() {
        return pcColsDef;
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

    public void setFileInput(RichInputFile fileInput) {
        this.fileInput = fileInput;
    }

    public RichInputFile getFileInput() {
        return fileInput;
    }

    public void setDataImportWnd(RichPopup dataImportWnd) {
        this.dataImportWnd = dataImportWnd;
    }

    public RichPopup getDataImportWnd() {
        return dataImportWnd;
    }

    public void setErrorWindow(RichPopup errorWindow) {
        this.errorWindow = errorWindow;
    }

    public RichPopup getErrorWindow() {
        return errorWindow;
    }

    public void setDetailList(List<SelectItem> detailList) {
        this.detailList = detailList;
    }

    public List<SelectItem> getDetailList() {
        return detailList;
    }

    public void setTaxList(List<SelectItem> taxList) {
        this.taxList = taxList;
    }

    public List<SelectItem> getTaxList() {
        return taxList;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isIsSelected() {
        return isSelected;
    }

    public void setProLov(DmsComBoxLov proLov) {
        this.proLov = proLov;
    }

    public DmsComBoxLov getProLov() {
        return proLov;
    }

    public void setIsEDITABLE(boolean isEDITABLE) {
        this.isEDITABLE = isEDITABLE;
    }

    public boolean isIsEDITABLE() {
        return isEDITABLE;
    }

    public void beBlocked(ActionEvent actionEvent) {
        System.out.println(this.connectId);
        //查询表中是否有该组合的记录，没有则插入
        String beBlockSql = "INSERT INTO DMS_HTKP_BLOCK (COM_RECORD_ID,IS_BLOCK) VALUES ('"+this.connectId+
            "','true')";
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        try {
            stat.executeUpdate(beBlockSql);
            trans.commit();
            stat.close();
            this.isBlock = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void outBlock(ActionEvent actionEvent) {
        String outBlockSql = "DELETE FROM DMS_HTKP_BLOCK WHERE COM_RECORD_ID = '"+this.connectId+"'";
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        try {
            stat.executeUpdate(outBlockSql);
            trans.commit();
            stat.close();
            this.isBlock = false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setIsBlock(boolean isBlock) {
        this.isBlock = isBlock;
    }

    public boolean isIsBlock() {
        return isBlock;
    }

    public void setSubTable(RichTable subTable) {
        this.subTable = subTable;
    }

    public RichTable getSubTable() {
        return subTable;
    }

    public void outBlock2(ActionEvent actionEvent) {
        if(this.subTable.getSelectedRowKeys() ==  null ) return;
        RowKeySet rks = this.subTable.getSelectedRowKeys();
        ViewObject vo = ADFUtils.findIterator("AdminHtkpStatusIterator").getViewObject();
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Iterator itr = rks.iterator();
        try {
            while(itr.hasNext()){
                List ls = (List)itr.next();
                Key key =(Key)ls.get(0);
                Row row = vo.getRow(key);
                StringBuffer sql = new StringBuffer();
                if(row.getAttribute("IsBlock").equals("已冻结")){
                    sql.append("DELETE FROM DMS_HTKP_BLOCK WHERE COM_RECORD_ID = '").append(row.getAttribute("Id")).append("'");
                    stat.executeUpdate(sql.toString());
                }
                if(row.getAttribute("Proname").equals(this.pName)){
                    this.isBlock = false;
                }
            }
            stat.close();
            trans.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setIsManager(boolean isManager) {
        this.isManager = isManager;
    }

    public boolean isIsManager() {
        return isManager;
    }

    public void setSubTable2(RichTable subTable2) {
        this.subTable2 = subTable2;
    }

    public RichTable getSubTable2() {
        return subTable2;
    }

    public void setPtBlockPoup(RichPopup ptBlockPoup) {
        this.ptBlockPoup = ptBlockPoup;
    }

    public RichPopup getPtBlockPoup() {
        return ptBlockPoup;
    }

    public void setAdminBlockPoup(RichPopup adminBlockPoup) {
        this.adminBlockPoup = adminBlockPoup;
    }

    public RichPopup getAdminBlockPoup() {
        return adminBlockPoup;
    }

    public void showBlockStatus(ActionEvent actionEvent) {
        if(this.curUser.getId().equals("10000")){
            this.showAdminStatus();
        }else{
            this.showPtStatus();
        }
    }
    
    public void showAdminStatus(){
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        this.adminBlockPoup.show(ph);
    }
    
    public void showPtStatus(){
        ViewObject vo = ADFUtils.findIterator("PtHtkpStatusIterator").getViewObject();
        vo.setNamedWhereClauseParam("userId", this.curUser.getId());
        vo.setNamedWhereClauseParam("userAcc", this.curUser.getAcc());
        vo.executeQuery();
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        this.ptBlockPoup.show(ph);
    }

    public void outBlock3(ActionEvent actionEvent) {
        if(this.subTable2.getSelectedRowKeys() ==  null ) return;
        RowKeySet rks = this.subTable2.getSelectedRowKeys();
        ViewObject vo = ADFUtils.findIterator("PtHtkpStatusIterator").getViewObject();
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        Iterator itr = rks.iterator();
        try {
            while(itr.hasNext()){
                List ls = (List)itr.next();
                Key key =(Key)ls.get(0);
                Row row = vo.getRow(key);
                StringBuffer sql = new StringBuffer();
                if(row.getAttribute("IsBlock").equals("已冻结")){
                    sql.append("DELETE FROM DMS_HTKP_BLOCK WHERE COM_RECORD_ID = '").append(row.getAttribute("Id")).append("'");
                    stat.executeUpdate(sql.toString());
                }
                if(row.getAttribute("Proname").equals(this.pName)){
                    this.isBlock = false;
                }
            }
            stat.close();
            trans.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
