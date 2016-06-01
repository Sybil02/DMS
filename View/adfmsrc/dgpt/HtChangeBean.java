package dgpt;

import common.DmsUtils;

import common.JSFUtils;

import dcm.DcmDataDisplayBean;
import dcm.DcmDataTableModel;
import dcm.PcColumnDef;

import dcm.PcDataTableModel;

import dcm.PcExcel2003WriterImpl;

import dcm.PcExcel2007WriterImpl;

import dms.login.Person;

import java.io.IOException;
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
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import java.util.UUID;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.output.RichPanelCollection;

import oracle.jbo.server.DBTransaction;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class HtChangeBean {
    private Person curUser;
    private RichPanelCollection panelaCollection;
    private CollectionModel dataModel;
    private List<PcColumnDef> pcColsDef = new ArrayList<PcColumnDef>();
    public HtChangeBean() {
        super();
        this.curUser = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
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
    private String version;
    private List<SelectItem> versionList;
    private String proType;
    private String hLine;
    private String yLine;
    private String pStart;
    private String pEnd;
    private String connectId;
    private String newVersion;
    private String newEnd;
    public static final String TYPE_CHANGE="CHANGE";
    //日志
    private static ADFLogger _logger =ADFLogger.createADFLogger(DcmDataDisplayBean.class);

    //是否是2007及以上格式
    private boolean isXlsx = true;
    private RichPopup dataExportWnd;
    
    private void initList(){
        this.yearList = queryYears("HLS_YEAR");
        this.pnameList = queryValues("PRO_PLAN_COST_HEADER","PROJECT_NAME");
        this.versionList = queryValues("PRO_PLAN_COST_HEADER","VERSION");
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
    
    //其他下拉列表
    private List<SelectItem> queryValues(String source,String col){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT DISTINCT "+col+" FROM "+source;
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
    //项目名称下拉框change
    public void projectChange(ValueChangeEvent valueChangeEvent) {
        pname =(String) valueChangeEvent.getNewValue();
            if(year==null||version==null||pname==null){
                return;
            }else{
                this.queryData();
            }
    }
    
    //版本下拉框change
    public void versionChange(ValueChangeEvent valueChangeEvent) {
        version =(String) valueChangeEvent.getNewValue();
        if(year==null||version==null||pname==null){
            return;
        }else{
            this.queryData();
        }
    }
    
    //年下拉框change
    public void yearChange(ValueChangeEvent valueChangeEvent) {
        year = (String)valueChangeEvent.getNewValue();
        if(year==null||version==null||pname==null){
            return;
        }else{
            this.queryData();
        }
    }
    
    //数据查询--头
     public void queryData(){
         DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
         Statement stat = trans.createStatement(DBTransaction.DEFAULT);
         String sql = "SELECT ENTITY_NAME,PRODUCT_LINE,PROJECT_TYPE,INDUSTRY_LINE,BUSINESS_LINE,CONNECT_ID,PROJECT_START,PROJECT_END"
                     + " FROM PRO_PLAN_COST_HEADER WHERE VERSION = \'"+version+"\'";
         sql = sql +" AND HLS_YEAR=\'"+year+"\'";
         sql = sql + " AND PROJECT_NAME=\'"+pname+"\'";
         ResultSet rs;
         try {
             rs = stat.executeQuery(sql);
             entity="";
             pLine="";
             proType="";
             hLine="";
             yLine="";
             connectId="";
             while(rs.next()){
                 entity = rs.getString("ENTITY_NAME");
                 pLine = rs.getString("PRODUCT_LINE");
                 proType = rs.getString("PROJECT_TYPE");
                 hLine = rs.getString("INDUSTRY_LINE");
                 yLine = rs.getString("BUSINESS_LINE");
                 connectId=rs.getString("CONNECT_ID");
                 pStart = rs.getString("PROJECT_START");
                 pEnd = rs.getString("PROJECT_END");
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
    
    public void createTableModel(String startTime,String endTime){
        //行的Map
        LinkedHashMap<String,String> labelMap = getLabelMap(startTime,endTime);
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
                for(Map.Entry<String,String> entry:labelMap.entrySet()){
                    row.put(entry.getValue(), rs.getString(entry.getValue()));
                }
                row.put("ROW_ID", rs.getString("ROW_ID"));
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
    
    //查询语句
    private String querySql(LinkedHashMap<String,String> labelMap){
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        for(Map.Entry<String,String> entry : labelMap.entrySet()){
            sql.append(entry.getValue()).append(",");
        }
        sql.append("ROWID AS ROW_ID FROM PRO_PLAN_COST_BODY WHERE CONNECT_ID = '").append(connectId).append("'");
        sql.append(" AND DATA_TYPE = '").append(this.TYPE_CHANGE).append("'");
        return sql.toString();
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
            rowMap.put("OPERATION", PcDataTableModel.OPERATE_UPDATE);    
        }                
    }
    
    //取消
    public void operation_reset(ActionEvent actionEvent) {
        DmsUtils.getDmsApplicationModule().getTransaction().rollback();
        this.queryData();
        this.createTableModel(pStart,pEnd);
    }
    
    public void getNewVE(ActionEvent actionEvent) {
        //判断时间格式
        if(!newEnd.contains("_")){
            JSFUtils.addFacesInformationMessage("日期格式是：YYYY_MM");
            return;
        }
        //生成id
        String s = UUID.randomUUID().toString();
        //去除分隔符-
        String newConnectId = s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24);
        //插入头表
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO PRO_PLAN_COST_HEADER (HLS_YEAR,ENTITY_NAME,PRODUCT_LINE,PROJECT_NAME,VERSION,PROJECT_TYPE,INDUSTRY_LINE,")
            .append("BUSINESS_LINE,PROJECT_START,PROJECT_END,CONNECT_ID) ");
        sql.append("VALUES('").append(year+"','").append(entity+"','").append(pLine+"','").append(pname+"','").append(newVersion+"','").append(proType+"','")
            .append(hLine+"','").append(yLine+"','").append(pStart+"','").append(newEnd+"','").append(newConnectId).append("')");
        try {
            stat.execute(sql.toString());
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //复制，type--》connectId
        LinkedHashMap<String,String> labelMap = getLabelMap(pStart,pEnd);
        StringBuffer sqlInsert = new StringBuffer();
        StringBuffer sqlValue = new StringBuffer();
        sqlInsert.append("INSERT INTO PRO_PLAN_COST_BODY (");
        sqlValue.append("SELECT ");
        for(Map.Entry<String,String> entry:labelMap.entrySet()){
            sqlInsert.append(entry.getValue()+",");
            sqlValue.append("T."+entry.getValue()+",");
        }
        sqlInsert.append("DATA_TYPE,CONNECT_ID)");
        sqlValue.append("'"+this.TYPE_CHANGE+"','").append(newConnectId).append("\'")
                .append(" FROM PRO_PLAN_COST_BODY T WHERE T.CONNECT_ID=\'"+connectId+"\'");
        //显示新版本，重新构造列
        try {
            trans.createStatement(DBTransaction.DEFAULT).execute(sqlInsert.toString()+sqlValue.toString());
            trans.commit();
            trans.createStatement(DBTransaction.DEFAULT).close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        versionList.add(new SelectItem(newVersion,newVersion));
        version = newVersion;
        pEnd = newEnd;
        connectId = newConnectId;
        this.createTableModel(pStart,pEnd);
    }

    //新增
    public void operation_new() {
        List<Map> modelData = (List<Map>) this.dataModel.getWrappedData();
        Map newRow = new HashMap();
        for(PcColumnDef col : this.pcColsDef){
            newRow.put(col.getDbTableCol(), null);
        }
        newRow.put("OPERATION",PcDataTableModel.OPERATE_CREATE);
        modelData.add(0, newRow);
    }
    //保存
    public void operation_save() {
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        //清空临时表数据
        String sqldelete = "DELETE FROM PRO_PLAN_COST_BODY_TEMP T WHERE T.CREATED_BY = \'"+this.curUser.getId()+"\'";
        Statement st = trans.createStatement(DBTransaction.DEFAULT);
        try {
            st.executeUpdate(sqldelete);
            trans.commit();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        StringBuffer sql = new StringBuffer();
        StringBuffer sql_value = new StringBuffer();
        sql_value.append(" VALUES(");
        sql.append("INSERT INTO PRO_PLAN_COST_BODY_TEMP(") ;
        //构建sql语句
        LinkedHashMap<String,String> map = this.getLabelMap(pStart,pEnd);
        int last = map.size()+1;
        for(Map.Entry<String,String> entry : map.entrySet()){
            sql.append(entry.getValue()+",");
            sql_value.append("?,");
        }
        sql.append("ROW_ID,CONNECT_ID,CREATED_BY,OPERATION)");
        sql_value.append("?,\'"+connectId+"\',"+this.curUser.getId()+",?)");
        PreparedStatement stmt = trans.createPreparedStatement(sql.toString()+sql_value.toString(), 0);
        //获取数据
        List<Map> modelData = (List<Map>)this.dataModel.getWrappedData();
        for(Map<String,String> rowdata : modelData){
                try {
                    stmt.setString(last, rowdata.get("ROW_ID"));
                    if(PcDataTableModel.OPERATE_UPDATE.equals(rowdata.get("OPERATION"))){
                        stmt.setString(last+1,PcDataTableModel.OPERATE_UPDATE);
                    }else if(PcDataTableModel.OPERATE_CREATE.equals(rowdata.get("OPERATION"))){
                        stmt.setString(last+1,PcDataTableModel.OPERATE_CREATE);
                    }else if(PcDataTableModel.OPERATE_DELETE.equals(rowdata.get("OPERATION"))){
                        stmt.setString(last+1,PcDataTableModel.OPERATE_DELETE);
                    }
                    if(null!=rowdata.get("OPERATION")){
                        int i =1;
                        for(Map.Entry<String,String> entry : map.entrySet()){
                            stmt.setString(i++ , rowdata.get(entry.getValue()));
                        }
                        stmt.addBatch();
                        stmt.executeBatch();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } 
        }
        trans.commit();
        //执行校验
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(this.validation()){
            //校验成功，执行导入
            this.inputPro();
            for(Map<String,String> rowdata : modelData){
                    rowdata.put("OPERATION", null);
            }
            this.createTableModel(pStart, pEnd);
        }else{
            JSFUtils.addFacesErrorMessage("数据校验不通过！");
        }
    }
    //删除
    public void operation_delete() {
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

    private LinkedHashMap<String,String> getLabelMap(String startTime,String endTime){
        LinkedHashMap<String,String> labelMap = new LinkedHashMap<String,String>();
        labelMap.put("KEY1", "WBS");
        labelMap.put("KEY2","WORK");
        labelMap.put("KEY3","TERM");
        labelMap.put("KEY4","CENTER");
        labelMap.put("KEY5","WORK_TYPE");
        labelMap.put("KEY6","BOM_CODE");
        labelMap.put("KEY7","UNIT");
        labelMap.put("KEY8","PLAN_COST");
        //labelMap.put("KEY9", "OCCURRED");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM");
        List<Date> monthList;
        Date start;
        Date end ;
        try {
            start = sdf.parse(startTime);
            end = sdf.parse(endTime);
            monthList = this.findDates(start, end);
            for(int i = 0 ; i < monthList.size() ; i++){
                labelMap.put("KEY"+(i+10), "Y"+sdf.format(monthList.get(i)));
            }
            //labelMap.put("KEY"+(monthList.size()+10),"SUM_AFTER_JUL");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        boolean isReadonly = true;
        this.pcColsDef.clear();
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            PcColumnDef newCol = new PcColumnDef(map.getValue(),map.getValue(),isReadonly);
            this.pcColsDef.add(newCol);
        }
        ((PcDataTableModel)this.dataModel).setPcColsDef(this.pcColsDef);
        return labelMap;
    }
    
    //时间段中的所有时间
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
    
    //校验程序
    public boolean validation(){
        boolean flag = true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.CHANGE_VALIDATION(?,?)}", 0);
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
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.CHANGE_INPUTPRO(?)}", 0);
        try {
            cs.setString(1,this.curUser.getId() );
            cs.execute();
            trans.commit();
            cs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
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
        return dataModel;
    }

    public void setPcColsDef(List<PcColumnDef> pcColsDef) {
        this.pcColsDef = pcColsDef;
    }

    public List<PcColumnDef> getPcColsDef() {
        return pcColsDef;
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

    public void setPStart(String pStart) {
        this.pStart = pStart;
    }

    public String getPStart() {
        return pStart;
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
        return connectId;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewEnd(String newEnd) {
        this.newEnd = newEnd;
    }

    public String getNewEnd() {
        return newEnd;
    }

    public void setIsXlsx(boolean isXlsx) {
        this.isXlsx = isXlsx;
    }

    public boolean isIsXlsx() {
        return isXlsx;
    }

    //导出
    public void operation_export(FacesContext facesContext, OutputStream outputStream) {
        this.dataExportWnd.cancel();
        String type = this.isXlsx ? "xlsx" : "xls";
        try {
            if("xls".equals(type)){
                PcExcel2003WriterImpl writer = new PcExcel2003WriterImpl(
                                                   this.querySql(this.getLabelMap(pStart, pEnd)),
                                                   "合同变更后的基准计划成本",
                                                    this.pcColsDef,
                                                    outputStream);
            
                writer.writeToFile();
            }else{
                PcExcel2007WriterImpl writer = new PcExcel2007WriterImpl(
                                                    this.querySql(this.getLabelMap(pStart, pEnd)),
                                                    2,this.pcColsDef);
                writer.process(outputStream, "合同变更后的基准计划成本");
                outputStream.flush();
            }
        } catch (Exception e) {
            this._logger.severe(e);
        } 
       
    }
    //导出文件名
    public String getExportDataExcelName(){
        if(isXlsx){
            return "合同变更后的基准计划成本.xlsx";
        }else{
            return "合同变更后的基准计划成本.xls";
        }
    }

    public void setDataExportWnd(RichPopup dataExportWnd) {
        this.dataExportWnd = dataExportWnd;
    }

    public RichPopup getDataExportWnd() {
        return dataExportWnd;
    }
}
