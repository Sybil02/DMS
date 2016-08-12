package dgpt;

import common.DmsLog;
import common.DmsUtils;
import common.JSFUtils;

import dcm.DcmDataDisplayBean;
import dcm.PcColumnDef;

import dcm.PcDataTableModel;

import dms.login.Person;

import java.io.OutputStream;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.output.RichPanelCollection;

import oracle.jbo.server.DBTransaction;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class XmdbBean {
    private Person curUser;
    private RichPanelCollection panelaCollection;
    private CollectionModel dataModel;
    private List<PcColumnDef> pcColsDef = new ArrayList<PcColumnDef>();

    //日志
    private static ADFLogger _logger =ADFLogger.createADFLogger(DcmDataDisplayBean.class);

    public XmdbBean() {
        super();
        this.curUser = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
        if("10000".equals(this.curUser.getId())){
            isManager = true;
        }else{
            isManager = false;
        }
        this.dataModel = new PcDataTableModel();
        List<Map> d = new ArrayList<Map>();
        this.dataModel.setWrappedData(d);
        this.initList();
    }
    private String year;
    private List<SelectItem> yearList;
    private String version;
    private List<SelectItem> versionList;
    private List<SelectItem> entityList;
    private List<SelectItem> typeList;
    private String pStart;
    private String pEnd;
    private String connectId;
    public static final String TYPE_ZZX="ZX";
    private String isBlock;
    private boolean isManager;
    private Date day1;
    private Date day2;
    private Date day3;
    private String exportName;
    private String entity;
    private String type;
    
    private void initList(){
        this.yearList = queryYears("HLS_YEAR_C");
        this.versionList = queryValues1("PRO_PLAN_COST_HEADER","VERSION");
        this.entityList = this.queryEntity();
        this.typeList = this.queryType();
    }
    
    private LinkedHashMap<String,String> getLabelMap(){
        LinkedHashMap<String,String> labelMap = new LinkedHashMap<String,String>();
        if(pStart == null || pEnd == null){
            return  labelMap;    
        }
        labelMap.put("PROJECT_NAME", "PROJECT_NAME");
        labelMap.put("PRODUCT_LINE", "PRODUCT_LINE");
        labelMap.put("ENTITY_NAME", "ENTITY_NAME");
        labelMap.put("INDUSTRY_LINE", "INDUSTRY_LINE");
        labelMap.put("BUSINESS_LINE", "BUSINESS_LINE");
        labelMap.put("PROJECT_TYPE", "PROJECT_TYPE");
        labelMap.put("TOTAL", "TOTAL");
        labelMap.put("FCST_COST","FCST_COST");
        labelMap.put("OCCURRED","OCCURRED");
        //CONNECT_ID
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
        labelMap.put("NEXT_ORTHERS","SUM_AFTER_JUL");
        //构造列
        boolean isReadonly = true;
        this.pcColsDef.clear();
        List<String> list = new ArrayList<String>();
        list.add("项目名称");
        list.add("产品线");
        list.add("实体");
        list.add("行业线");
        list.add("业务线");
        list.add("项目类型");
        list.add("科目");
        list.add("预计总成本");
        list.add("上年1-10月实际");
        list.add("上年11-12月预计");
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
        list.add("下年3季度以后");
        for(String cols : list){
            PcColumnDef newCol = newCol = new PcColumnDef(cols,cols,isReadonly);
            this.pcColsDef.add(newCol);
        }
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM");
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql.toString());
            while(rs.next()){
                Map row = new HashMap();
                for(Map.Entry entry : labelMap.entrySet()){
                    row.put(entry.getValue(),this.getPrettyNumber(rs.getString(entry.getValue().toString())));
                }
                Double d1 = Double.parseDouble(rs.getString("Y"+sdf.format(day1)) != null ? rs.getString("Y"+sdf.format(day1)):"0");
                Double d2 = Double.parseDouble(rs.getString("Y"+sdf.format(day2)) != null ? rs.getString("Y"+sdf.format(day2)):"0");
                Double d3 = Double.parseDouble(rs.getString("Y"+sdf.format(day3)) != null ? rs.getString("Y"+sdf.format(day3)):"0");
                Double occ = Double.parseDouble(rs.getString("OCCURRED") != null ? rs.getString("OCCURRED"):"0");
                //去掉小数点最后面的0  如 ： .0 , .10
                row.put("LAST_1_10ADJ",  this.getPrettyNumber(""+(d1+occ)));
                row.put("LAST_11_12FCST", this.getPrettyNumber(""+(d2+d3+occ)));
                row.put("NEXT_ORTHERS", this.getPrettyNumber(rs.getString("SUM_AFTER_JUL")));
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
    
    public static String getPrettyNumber(String number) {  
        if(number == null) return "";
        if(number.equals("0.0")){
            number = "";    
        }
        //.4==>0.4
        if(number.startsWith(".")){
            number = "0" + number;    
        }
        //16.0==>16
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
        sql.append("DATA_TYPE FROM PRO_PLAN_ACC_TOTAL_V WHERE HLS_YEAR = '").append(year).append("'");
        sql.append(" AND VERSION = '").append(version).append("'");
        sql.append(" AND ENTITY_NAME = '").append(entity).append("'");
        sql.append(" AND PROJECT_TYPE = '").append(type).append("'");
        System.out.println(sql);
        return sql.toString();
    }
    //时间段
    public  List<Date> findDates(Date dBegin, Date dEnd) {  
            List lDate = new ArrayList();  
            lDate.add(dBegin);  
            Calendar calBegin = Calendar.getInstance();  
            // 使用给定的 Date 设置此 Calendar 的时间    
            calBegin.setTime(dBegin);
            Calendar calEnd = Calendar.getInstance();  
            // 使用给定的 Date 设置此 Calendar 的时间    
            calEnd.setTime(dEnd);  
            // 测试此日期是否在指定日期之后   
            day1 = calBegin.getTime();
            int i = 1;
            while (dEnd.after(calBegin.getTime())) {  
                // 根据日历的规则，为给定的日历字段添加或减去指定的时间量 
                calBegin.add(Calendar.MONTH, 1); 
                if(i==1){
                    day2 = calBegin.getTime();
                }else if(i==2){
                    day3 = calBegin.getTime();
                }
                lDate.add(calBegin.getTime());
                i++;
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
    
    //其他下拉列表
    private List<SelectItem> queryValues(String source,String col){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "";
        if(this.curUser.getId().equals("10000")){
            sql = "SELECT DISTINCT P."+col+" FROM "+source+" P WHERE P.PROJECT_NAME IN (" + 
                "SELECT T.PRO_CODE||'-'||T.PRO_DESC FROM SAP_DMS_PROJECT_Privilege T " +
                "WHERE T.ATTRIBUTE4='admin' AND T.ATTRIBUTE3='"+this.TYPE_ZZX+"') "+
                "AND DATA_TYPE='"+this.TYPE_ZZX+"'";
        }else {
            sql = "SELECT DISTINCT P."+col+" FROM "+source+" P WHERE P.PROJECT_NAME IN (" + 
            "SELECT T.PRO_CODE||'-'||T.PRO_DESC FROM SAP_DMS_PROJECT_PRIVILEGE_V T WHERE ID = '"+this.curUser.getId()+"'"+
                ") AND  DATA_TYPE =\'"+this.TYPE_ZZX+"\'";
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
    
    public List<SelectItem> queryEntity(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //实体
        String sql = "SELECT VALUE_ID FROM DMS_USER_VALUE_V T WHERE T.VALUE_SET_ID = '53fdbb001ad14e16a40604c7bd3c6025' "
            + "AND T.USER_ID = '" + this.curUser.getId() + "'";
        List<SelectItem> entityList = new ArrayList<SelectItem>();
        
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                SelectItem sim = new SelectItem(rs.getString("VALUE_ID"),rs.getString("VALUE_ID"));
                entityList.add(sim);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entityList;
    }
    
    public List<SelectItem> queryType(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        //项目类型
        String sql = "SELECT VALUE_ID FROM DMS_USER_VALUE_V T WHERE T.VALUE_SET_ID = '00a2446792244432b48bfce722550630' "
            + "AND T.USER_ID = '" + this.curUser.getId() + "'";
        List<SelectItem> typeList = new ArrayList<SelectItem>();
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            while(rs.next()){
                SelectItem sim = new SelectItem(rs.getString("VALUE_ID"),rs.getString("VALUE_ID"));
                typeList.add(sim);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return typeList;
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
    
    //版本下拉框change
    public void versionChange(ValueChangeEvent valueChangeEvent) {
        version =(String) valueChangeEvent.getNewValue();
        if(year==null||version==null||entity==null||type==null){
            return;
        }else{
            this.setStartAndEndTime();
            this.createTableModel();
        }
    }
    
    //年下拉框change
    public void yearChange(ValueChangeEvent valueChangeEvent) {
        year = (String)valueChangeEvent.getNewValue();
        if(year==null||version==null||entity==null||type==null){
            return;
        }else{
            this.setStartAndEndTime();
            this.createTableModel();
        }
    }
    
    public void entityChange(ValueChangeEvent valueChangeEvent) {
        entity = (String)valueChangeEvent.getNewValue();
        if(year==null||version==null||entity==null||type==null){
            return;
        }else{
            this.setStartAndEndTime();
            this.createTableModel();
        }
    }

    public void typeChange(ValueChangeEvent valueChangeEvent) {
        type = (String)valueChangeEvent.getNewValue();
        if(year==null||version==null||entity==null||type==null){
            return;
        }else{
            this.setStartAndEndTime();
            this.createTableModel();
        }
    }
    
    public void setStartAndEndTime(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT MIN(T.PROJECT_START) AS PSTART,MAX(T.PROJECT_END) AS PEND FROM PRO_PLAN_ACC_TOTAL_V T WHERE T.HLS_YEAR = '"
            + year + "' AND T.VERSION = '" + version + "'";
        ResultSet rs;
        try {
            rs = stat.executeQuery(sql);
            if(rs.next()){
                this.pStart = rs.getString("PSTART");
                this.pEnd = rs.getString("PEND");
            }
            rs.close();
            stat.close();
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
    
    public void reset(ActionEvent actionEvent) {
        DmsUtils.getDmsApplicationModule().getTransaction().rollback();
        this.createTableModel();
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

    public void setIsBlock(String isBlock) {
        this.isBlock = isBlock;
    }

    public String getIsBlock() {
        return isBlock;
    }

    public void setIsManager(boolean isManager) {
        this.isManager = isManager;
    }

    public boolean isIsManager() {
        return isManager;
    }

    public void setDay1(Date day1) {
        this.day1 = day1;
    }

    public Date getDay1() {
        return day1;
    }

    public void setDay2(Date day2) {
        this.day2 = day2;
    }

    public Date getDay2() {
        return day2;
    }

    public void setDay3(Date day3) {
        this.day3 = day3;
    }

    public Date getDay3() {
        return day3;
    }

    public void importData(FacesContext facesContext,
                           OutputStream outputStream) {
        try {

            XmdbExcel2007Writer writer = new XmdbExcel2007Writer(this.pcColsDef,(List<Map>)this.dataModel.getWrappedData(),this.getLabelMap());
            writer.process(outputStream, "项目大表");
            outputStream.flush();
        } catch (Exception e) {
            this._logger.severe(e);
        } 
        DmsLog dmsLog = new DmsLog();
        dmsLog.operationLog(this.curUser.getAcc(),this.connectId,this.exportName,"EXPORT");
    }

    public void setExportName(String exportName) {
        this.exportName = exportName;
    }

    public String getExportName() {
        if(version == null || year == null){
            return "项目大表.xlsx";    
        }else{
            for( SelectItem sim : yearList){
                if(sim.getValue().equals(year)){
                    exportName = "项目大表_"+sim.getLabel()+"_";
                }    
            }
            for( SelectItem sim : versionList){
                if(sim.getValue().equals(version)){
                    exportName = exportName + sim.getLabel() + ".xlsx";  
                }    
            }
        }
        return exportName;
    }

    public void setEntityList(List<SelectItem> entityList) {
        this.entityList = entityList;
    }

    public List<SelectItem> getEntityList() {
        return entityList;
    }

    public void setTypeList(List<SelectItem> typeList) {
        this.typeList = typeList;
    }

    public List<SelectItem> getTypeList() {
        return typeList;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
