package dgpt;

import common.ADFUtils;
import common.DmsUtils;
import common.JSFUtils;

import dcm.DcmDataDisplayBean;
import dcm.PcColumnDef;

import dcm.PcDataTableModel;

import dcm.PcExcel2003WriterImpl;
import dcm.PcExcel2007WriterImpl;

import dms.login.Person;

import java.io.OutputStream;

import java.math.BigDecimal;

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

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.output.RichPanelCollection;

import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

public class ProZzxOutBean {
    private Person curUser;
    private RichPanelCollection panelaCollection;
    private CollectionModel dataModel;
    private List<PcColumnDef> pcColsDef = new ArrayList<PcColumnDef>();

    //日志
    private static ADFLogger _logger =ADFLogger.createADFLogger(DcmDataDisplayBean.class);

    public ProZzxOutBean() {
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
    public static final String TYPE_ZZX="ZX";
    private String isBlock;
    private boolean isManager;
    private Date day1;
    private Date day2;
    private Date day3;
    
    private void initList(){
        this.yearList = queryYears("HLS_YEAR_C");
        this.pnameList = queryValues("PRO_PLAN_COST_HEADER","PROJECT_NAME");
        this.versionList = queryValues1("PRO_PLAN_COST_HEADER","VERSION");
    }
    
    private LinkedHashMap<String,String> getLabelMap(){
        LinkedHashMap<String,String> labelMap = new LinkedHashMap<String,String>();
        if(pStart == null || pEnd == null){
            return  labelMap;    
        }
        labelMap.put("TOTAL", "TOTAL");
        labelMap.put("FCST_COST","FCST_COST");
        labelMap.put("WBS","WBS");
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
        int i =0;
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            PcColumnDef newCol = null;
            if(i<23){
                newCol = new PcColumnDef(list.get(i),map.getValue(),isReadonly);
            }else{
                newCol = new PcColumnDef(map.getValue(),map.getValue(),isReadonly);
            }
            i++;
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
        if(number.startsWith(".")){
            number = "0" + number;    
        }
        //显示时16.0不用去除.0
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
        sql.append("DATA_TYPE FROM PRO_PLAN_COST_BODY_VIEW WHERE CONNECT_ID = '").append(connectId).append("'");
        sql.append(" AND DATA_TYPE = '").append(this.TYPE_ZZX).append("'");
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
//            "       SELECT T.PRO_CODE||'-'||T.PRO_DESC FROM SAP_DMS_PROJECT_Privilege T " +
//                "WHERE T.ATTRIBUTE3 = \'"+this.TYPE_ZZX+"\'" + 
//                "AND T.PRO_MANAGER = '"+this.curUser.getAcc()+"' OR T.PRO_DIRECTOR='"+this.curUser.getAcc()+"'" +
//                "UNION " +
//                "   SELECT T1.PRO_CODE||'-'||T1.PRO_DESC FROM SAP_DMS_PROJECT_Privilege T1,DMS_USER_GROUP P " +
//                "WHERE T1.ATTRIBUTE3 = \'"+this.TYPE_ZZX+"\'"+
//                "AND P.GROUP_ID IN (SELECT GROUP_ID FROM DMS_USER_GROUP WHERE USER_ID='"+this.curUser.getId()+"')"+
//                "AND (T1.ATTRIBUTE6=P.GROUP_ID OR T1.ATTRIBUTE5=P.GROUP_ID)" +
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
            String sql = "SELECT DISTINCT VERSION,VERSION_NAME FROM PRO_PLAN_COST_HEADER WHERE DATA_TYPE =\'"+this.TYPE_ZZX+"\'"+
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
            this.queryData();
            this.createTableModel();
        }
    }
    
    //年下拉框change
    public void yearChange(ValueChangeEvent valueChangeEvent) {
        year = (String)valueChangeEvent.getNewValue();
        if(year==null||version==null||pname==null){
            return;
        }else{
            this.queryData();
            this.createTableModel();
        }
    }
    public void queryData(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT ENTITY_NAME,PRODUCT_LINE,PROJECT_TYPE,INDUSTRY_LINE,BUSINESS_LINE,CONNECT_ID,PROJECT_START,PROJECT_END,"
                    + "IS_BLOCK FROM PRO_PLAN_COST_HEADER WHERE VERSION = \'"+version+"\'";
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
            connectId="";
            isBlock = "false";
            while(rs.next()){
                entity = rs.getString("ENTITY_NAME");
                pLine = rs.getString("PRODUCT_LINE");
                proType = rs.getString("PROJECT_TYPE");
                hLine = rs.getString("INDUSTRY_LINE");
                yLine = rs.getString("BUSINESS_LINE");
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
        this.queryData();
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
}
