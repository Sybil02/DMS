package dgpt;

import common.ADFUtils;
import common.DmsUtils;

import common.JSFUtils;

import dcm.PcColumnDef;

import dcm.PcDataTableModel;

import dcm.PcExcel2003WriterImpl;
import dcm.PcExcel2007WriterImpl;

import dms.login.Person;

import java.io.OutputStream;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.Types;

import java.util.ArrayList;
import java.util.HashMap;
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

import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import oracle.jbo.server.ViewRowImpl;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class htkpReturnBean {
    private RichPopup dataExportWnd;

    public htkpReturnBean() {
        super();
        this.curUser = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
        this.dataModel = new PcDataTableModel();
        List<Map> d = new ArrayList<Map>();
        this.dataModel.setWrappedData(d);
        this.initList();
    }
    private Person curUser;
    private String year;
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
    private CollectionModel dataModel;
    private List<PcColumnDef> pcColsDef = new ArrayList<PcColumnDef>();
    //是否是2007及以上格式
    private boolean isXlsx = true;

    public void initList(){
        this.versionList = queryValues("version");
        this.yearList = queryValues("year");
        this.pNameList = queryPname();
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
            sql = "SELECT DISTINCT C.BH_USER_PRO_C1 CODE,B.MEANING FROM DCM_COMBINATION_17 C,BH_USER_PRO_C1 B WHERE B.MEANING IN" +
                "(SELECT T.PRO_CODE||'-'||T.PRO_DESC FROM SAP_DMS_PROJECT_Privilege T " +
                " WHERE T.ATTRIBUTE4='admin' AND T.ATTRIBUTE3='ZX')" +
                " AND C.BH_USER_PRO_C1 = B.CODE";
        }else{
            sql = "SELECT DISTINCT C.BH_USER_PRO_C1 CODE,B.MEANING FROM DCM_COMBINATION_17 C,BH_USER_PRO_C1 B WHERE B.MEANING IN" +
                "(SELECT T.PRO_CODE||'-'||T.PRO_DESC FROM SAP_DMS_PROJECT_PRIVILEGE_V T " +
                " WHERE T.ID = 'a9f3429fab254da4b30709f847664125' AND T.ATTRIBUTE3='ZX')" +
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
    //查询数据
    public String queryData(LinkedHashMap<String,String> labelMap){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT ID FROM DCM_COMBINATION_17 WHERE HLS_YEAR_C='"+this.year+"' " +
            "AND HLS_VERSION_C='"+this.version+"' AND BH_USER_PRO_C1='"+this.pName+"'";
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
        System.out.println(qSql.toString());
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
        this.pcColsDef.clear();
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            PcColumnDef newCol = new PcColumnDef(map.getKey(),map.getValue(),isReadonly);
            this.pcColsDef.add(newCol);
        }
        //this.pcColsDef.add(new PcColumnDef("ROW_ID","ROW_ID",false));
        ((PcDataTableModel)this.dataModel).setPcColsDef(this.pcColsDef);
        return labelMap;
    }
    
    public void createTableModel(){
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
                    if(entry.getValue().equals("OCCUR_COST") || entry.getValue().toString().startsWith("LAST") || 
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
    
    public void getLine(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT DISTINCT ENTITY,INDUSTRY_LINE,BUSINESS_LINE,PRODUCT_LINE,PROJECT_TYPE " +
            "FROM CONT_INVOICE_RETURN_BUDGET_5 WHERE COM_RECORD_ID='"+this.connectId+"'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            entity = "";
            hLine = "";
            yLine = "";
            cLine = "";
            pType = "";
            while(rs.next()){
                entity = rs.getString("ENTITY");
                hLine = rs.getString("INDUSTRY_LINE");
                yLine = rs.getString("BUSINESS_LINE");
                cLine = rs.getString("PRODUCT_LINE");
                pType = rs.getString("PROJECT_TYPE");
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void yearChange(ValueChangeEvent valueChangeEvent) {
        year = (String)valueChangeEvent.getNewValue();
        if(year==null||pName==null||version==null){
            return;
        }
        this.createTableModel();
    }

    public void pnameChange(ValueChangeEvent valueChangeEvent) {
        pName = (String) valueChangeEvent.getNewValue();
        if(year==null||pName==null||version==null){
            return;
        }
        this.createTableModel();
    }

    public void versionChange(ValueChangeEvent valueChangeEvent) {
        version = (String) valueChangeEvent.getNewValue();
        if(year==null||pName==null||version==null){
            return;
        }
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
        this.deleteTemp();
        this.copyToTemp();
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        if(this.validation()){
            this.inputPro();
            for(Map<String,String> rowdata : modelData){
                rowdata.put("OPERATION", null);
            }
        }else{
            JSFUtils.addFacesErrorMessage("校验不通过");
        }
    }
    //新增行
    public void operation_new(ActionEvent actionEvent) {
        this.getLine();
        List<Map> modelData = (List<Map>) this.dataModel.getWrappedData();
        Map newRow = new HashMap();
        for(PcColumnDef col : this.pcColsDef){
            if(col.getDbTableCol().equals("ENTITY")){
                newRow.put(col.getDbTableCol(), entity);
            }else if(col.getDbTableCol().equals("INDUSTRY_LINE")){
                newRow.put(col.getDbTableCol(), hLine);      
            }else if(col.getDbTableCol().equals("BUSINESS_LINE")){
                newRow.put(col.getDbTableCol(), yLine);                                                    
            }else if(col.getDbTableCol().equals("PRODUCT_LINE")){
                newRow.put(col.getDbTableCol(), cLine);                                                    
            }else if(col.getDbTableCol().equals("PROJECT_TYPE")){
                newRow.put(col.getDbTableCol(), pType);                                                   
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
        System.out.println(sql.toString()+sql_value.toString());
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
                    stmt.setString(i++ , rowdata.get(entry.getValue()));
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
    public void deleteTemp(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "DELETE FROM CONT_INVOICE_RETURN_BUDGET_5_T WHERE COM_RECORD_ID='"+this.connectId+"' " +
            "AND CREATED_BY='"+this.curUser.getId()+"'";
        System.out.println(sql);
        try {
            stat.executeUpdate(sql);
            stat.close();
            trans.commit();
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
        System.out.println("校验程序"+flag);
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
    //导出
    public void operation_export(FacesContext facesContext,
                                 OutputStream outputStream) {
        this.dataExportWnd.cancel();
        String type = this.isXlsx ? "xlsx" : "xls";
        try {
            if("xls".equals(type)){
                PcExcel2003WriterImpl writer = new PcExcel2003WriterImpl(
                                                    this.queryData(this.getLabelMap()),
                                                   this.connectId,
                                                    this.pcColsDef,
                                                    outputStream);
            
                writer.writeToFile();
            }else{
                PcExcel2007WriterImpl writer = new PcExcel2007WriterImpl(
                                                    this.queryData(this.getLabelMap()),
                                                    2,this.pcColsDef);
                writer.process(outputStream, this.connectId);
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    //导出文件名
    public String getExportDataExcelName(){
        if(isXlsx){
            return "年度预算-合同开票回款（在执行）_"+this.connectId+".xlsx";
        }else{
            return "年度预算-合同开票回款（在执行）_"+this.connectId+".xls";
        }
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
}
