package dgpt;

import common.ADFUtils;
import common.DmsUtils;

import common.JSFUtils;

import dcm.DcmDataDisplayBean;
import dcm.DcmDataTableModel;
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

public class ProjectZxBean {
    private Person curUser;
    private RichPanelCollection panelaCollection;
    private CollectionModel dataModel;
    private List<PcColumnDef> pcColsDef = new ArrayList<PcColumnDef>();

    //日志
    private static ADFLogger _logger =ADFLogger.createADFLogger(DcmDataDisplayBean.class);

    //是否是2007及以上格式
    private boolean isXlsx = true;
    private RichPopup dataExportWnd;
    private RichPopup errorWindow;

    public ProjectZxBean() {
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
    
    private void initList(){
        this.yearList = queryYears("HLS_YEAR_C");
        this.pnameList = queryValues("PRO_PLAN_COST_HEADER","PROJECT_NAME");
        this.versionList = queryValues("PRO_PLAN_COST_HEADER","VERSION");
    }
    
    private LinkedHashMap<String,String> getLabelMap(){
        LinkedHashMap<String,String> labelMap = new LinkedHashMap<String,String>();
        
        if(pStart == null || pEnd == null){
            return  labelMap;    
        }
        
        labelMap.put("WBS", "WBS");
        labelMap.put("WORK","WORK");
        labelMap.put("TERM","TERM");
        labelMap.put("CENTER","CENTER");
        labelMap.put("WORK_TYPE","WORK_TYPE");
        labelMap.put("BOM_CODE","BOM_CODE");
        labelMap.put("UNIT","UNIT");
        labelMap.put("PLAN_COST","PLAN_COST");
        labelMap.put("OCCURRED", "OCCURRED");
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
        labelMap.put("LGF_NUM", "LGF_NUM");
        labelMap.put("LGF_TYPE", "LGF_TYPE");
        //构造列导出
        boolean isReadonly = true;
        this.pcColsDef.clear();
        List<String> list = new ArrayList<String>();
        list.add("WBS");
        list.add("作业活动");
        list.add("预算项");
        list.add("工作中心");
        list.add("作业类型");
        list.add("物料编码");
        list.add("单位");
        list.add("计划成本");
        list.add("已发生（截至上年9月）");
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
        list.add("上年1月");
        list.add("上年2月");
        list.add("上年3月");
        list.add("上年4月");
        list.add("上年5月");
        list.add("上年6月");
        list.add("下年7月以后");
        int i =0;
        for(Map.Entry<String,String> map:labelMap.entrySet()){
            if(i<31){
                PcColumnDef newCol = new PcColumnDef(list.get(i),map.getValue(),isReadonly);
                this.pcColsDef.add(newCol);
            }else{
                break;
            }
            i++;
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
            number = number.substring(0,number.length()-1);    
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
        sql.append("ROWID AS ROW_ID,LGF_NUM,LGF_TYPE FROM PRO_PLAN_COST_BODY WHERE CONNECT_ID = '").append(connectId).append("'");
        sql.append(" AND DATA_TYPE = '").append(this.TYPE_ZZX).append("' ORDER BY WBS");
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
    
    //保存操作
    public void operation_save() {
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
        StringBuffer sql = new StringBuffer();
        StringBuffer sql_value = new StringBuffer();
        sql_value.append(" VALUES(");
        sql.append("INSERT INTO PRO_PLAN_COST_BODY_TEMP(") ;
        LinkedHashMap<String,String> map = this.getLabelMap();
        for(Map.Entry<String,String> entry : map.entrySet()){
            sql.append(entry.getValue()+",");
            sql_value.append("?,");
        }
        sql.append("CONNECT_ID,CREATED_BY,DATA_TYPE,ROW_NO,ROW_ID)");
        sql_value.append("\'"+connectId+"\',\'"+this.curUser.getId()+"\',\'"+this.TYPE_ZZX+"\',?,?)");
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
                    rowNum++;
                    stmt.addBatch();
                    stmt.executeBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }        
        //}
        trans.commit();
        
        //计算
        String sqlUpdate = this.updateSql();
        Statement stat = trans.createStatement(1);
        int flag =-1;
        //料工
        try {
            flag = stat.executeUpdate(sqlUpdate);
            //if(flag!=-1){
                trans.commit();
                stat.close();
            //}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //费
        String sqlFUpdate = this.sqlFUpdate();
        Statement stat1 = trans.createStatement(2);
        int flag1 = -1;
        try {
            flag1 = stat1.executeUpdate(sqlFUpdate);
            //if(flag1!=-1){
                trans.commit();
                stat1.close();
            //}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //jiaoyan
        
        if(this.validation()){
            this.inputPro();
            for(Map<String,String> rowdata : modelData){
                if("UPDATE".equals(rowdata.get("OPERATION"))){
                    rowdata.put("OPERATION", null);
                }
            }
        }else{
            this.showErrorPop();
        }
    }
    
    private String updateSql(){
        String sql = "UPDATE PRO_PLAN_COST_BODY_TEMP T SET " + 
        "      (T.R2010_01,  T.R2010_02,  T.R2010_03,  T.R2010_04,  T.R2010_05,  T.R2010_06,  T.R2010_07,  T.R2010_08,  T.R2010_09,  T.R2010_10,  T.R2010_11,  T.R2010_12," + 
        "      T.R2011_01,  T.R2011_02,  T.R2011_03,  T.R2011_04,  T.R2011_05,  T.R2011_06,  T.R2011_07,  T.R2011_08,  T.R2011_09,  T.R2011_10,  T.R2011_11,  T.R2011_12," + 
        "      T.R2012_01,  T.R2012_02,  T.R2012_03,  T.R2012_04,  T.R2012_05,  T.R2012_06,  T.R2012_07,  T.R2012_08,  T.R2012_09,  T.R2012_10,  T.R2012_11,  T.R2012_12," + 
        "      T.R2013_01,  T.R2013_02,  T.R2013_03,  T.R2013_04,  T.R2013_05,  T.R2013_06,  T.R2013_07,  T.R2013_08,  T.R2013_09,  T.R2013_10,  T.R2013_11,  T.R2013_12," + 
        "      T.R2014_01,  T.R2014_02,  T.R2014_03,  T.R2014_04,  T.R2014_05,  T.R2014_06,  T.R2014_07,  T.R2014_08,  T.R2014_09,  T.R2014_10,  T.R2014_11,  T.R2014_12," + 
        "      T.R2015_01,  T.R2015_02,  T.R2015_03,  T.R2015_04,  T.R2015_05,  T.R2015_06,  T.R2015_07,  T.R2015_08,  T.R2015_09,  T.R2015_10,  T.R2015_11,  T.R2015_12," + 
        "      T.R2016_01,  T.R2016_02,  T.R2016_03,  T.R2016_04,  T.R2016_05,  T.R2016_06,  T.R2016_07,  T.R2016_08,  T.R2016_09,  T.R2016_10,  T.R2016_11,  T.R2016_12," + 
        "      T.R2017_01,  T.R2017_02,  T.R2017_03,  T.R2017_04,  T.R2017_05,  T.R2017_06,  T.R2017_07,  T.R2017_08,  T.R2017_09,  T.R2017_10,  T.R2017_11,  T.R2017_12," + 
        "      T.R2018_01,  T.R2018_02,  T.R2018_03,  T.R2018_04,  T.R2018_05,  T.R2018_06,  T.R2018_07,  T.R2018_08,  T.R2018_09,  T.R2018_10,  T.R2018_11,  T.R2018_12," + 
        "      T.R2019_01,  T.R2019_02,  T.R2019_03,  T.R2019_04,  T.R2019_05,  T.R2019_06,  T.R2019_07,  T.R2019_08,  T.R2019_09,  T.R2019_10,  T.R2019_11,  T.R2019_12," + 
        "      T.R2020_01,  T.R2020_02,  T.R2020_03,  T.R2020_04,  T.R2020_05,  T.R2020_06,  T.R2020_07,  T.R2020_08,  T.R2020_09,  T.R2020_10,  T.R2020_11,  T.R2020_12," + 
        "      T.R2021_01,  T.R2021_02,  T.R2021_03,  T.R2021_04,  T.R2021_05,  T.R2021_06,  T.R2021_07,  T.R2021_08,  T.R2021_09,  T.R2021_10,  T.R2021_11,  T.R2021_12," + 
        "      T.R2022_01,  T.R2022_02,  T.R2022_03,  T.R2022_04,  T.R2022_05,  T.R2022_06,  T.R2022_07,  T.R2022_08,  T.R2022_09,  T.R2022_10,  T.R2022_11,  T.R2022_12," + 
        "      T.R2023_01,  T.R2023_02,  T.R2023_03,  T.R2023_04,  T.R2023_05,  T.R2023_06,  T.R2023_07,  T.R2023_08,  T.R2023_09,  T.R2023_10,  T.R2023_11,  T.R2023_12," + 
        "      T.R2024_01,  T.R2024_02,  T.R2024_03,  T.R2024_04,  T.R2024_05,  T.R2024_06,  T.R2024_07,  T.R2024_08,  T.R2024_09,  T.R2024_10,  T.R2024_11,  T.R2024_12," + 
        "      T.R2025_01,  T.R2025_02,  T.R2025_03,  T.R2025_04,  T.R2025_05,  T.R2025_06,  T.R2025_07,  T.R2025_08,  T.R2025_09,  T.R2025_10,  T.R2025_11,  T.R2025_12," + 
        "      T.R2026_01,  T.R2026_02,  T.R2026_03,  T.R2026_04,  T.R2026_05,  T.R2026_06,  T.R2026_07,  T.R2026_08,  T.R2026_09,  T.R2026_10,  T.R2026_11,  T.R2026_12," + 
        "      T.R2027_01,  T.R2027_02,  T.R2027_03,  T.R2027_04,  T.R2027_05,  T.R2027_06,  T.R2027_07,  T.R2027_08,  T.R2027_09,  T.R2027_10,  T.R2027_11,  T.R2027_12," + 
        "      T.R2028_01,  T.R2028_02,  T.R2028_03,  T.R2028_04,  T.R2028_05,  T.R2028_06,  T.R2028_07,  T.R2028_08,  T.R2028_09,  T.R2028_10,  T.R2028_11,  T.R2028_12," + 
        "      T.R2029_01,  T.R2029_02,  T.R2029_03,  T.R2029_04,  T.R2029_05,  T.R2029_06,  T.R2029_07,  T.R2029_08,  T.R2029_09,  T.R2029_10,  T.R2029_11,  T.R2029_12," + 
        "      T.R2030_01,  T.R2030_02,  T.R2030_03,  T.R2030_04,  T.R2030_05,  T.R2030_06,  T.R2030_07,  T.R2030_08,  T.R2030_09,  T.R2030_10,  T.R2030_11,  T.R2030_12)" + 
        "  =(SELECT " + 
        " NVL(P.Y2010_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2011_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2011_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2012_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2012_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2013_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2013_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2014_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2014_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2015_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2015_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2016_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2016_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2017_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2017_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2018_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2018_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2019_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2019_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2020_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2010_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_10,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2020_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2021_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_02,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_05,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2021_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2021_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2022_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2022_02,0)*NVL(LGF_NUM,0),	NVL(P.Y2022_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2022_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2022_05,0)*NVL(LGF_NUM,0),	 NVL(P.Y2022_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2022_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2022_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2022_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2022_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2022_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2022_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2023_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2023_02,0)*NVL(LGF_NUM,0),	NVL(P.Y2023_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2023_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2023_05,0)*NVL(LGF_NUM,0),	 NVL(P.Y2023_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2023_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2023_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2023_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2023_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2023_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2023_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2024_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2024_02,0)*NVL(LGF_NUM,0),	NVL(P.Y2024_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2024_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2024_05,0)*NVL(LGF_NUM,0),	 NVL(P.Y2024_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2024_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2024_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2024_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2024_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2024_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2024_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2025_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2025_02,0)*NVL(LGF_NUM,0),	NVL(P.Y2025_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2025_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2025_05,0)*NVL(LGF_NUM,0),	 NVL(P.Y2025_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2025_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2025_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2025_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2025_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2025_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2025_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2026_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2026_02,0)*NVL(LGF_NUM,0),	NVL(P.Y2026_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2026_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2026_05,0)*NVL(LGF_NUM,0),	 NVL(P.Y2026_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2026_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2026_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2026_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2026_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2026_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2026_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2027_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2027_02,0)*NVL(LGF_NUM,0),	NVL(P.Y2027_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2027_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2027_05,0)*NVL(LGF_NUM,0),	 NVL(P.Y2027_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2027_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2027_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2027_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2027_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2027_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2027_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2028_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2028_02,0)*NVL(LGF_NUM,0),	NVL(P.Y2028_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2028_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2028_05,0)*NVL(LGF_NUM,0),	 NVL(P.Y2028_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2028_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2028_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2028_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2028_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2028_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2028_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2029_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2029_02,0)*NVL(LGF_NUM,0),	NVL(P.Y2029_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2029_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2029_05,0)*NVL(LGF_NUM,0),	 NVL(P.Y2029_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2029_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2029_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2029_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2029_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2029_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2029_12,0)*NVL(LGF_NUM,0)," + 
        " NVL(P.Y2030_01,0)*NVL(LGF_NUM,0),  NVL(P.Y2030_02,0)*NVL(LGF_NUM,0),	NVL(P.Y2030_03,0)*NVL(LGF_NUM,0),  NVL(P.Y2030_04,0)*NVL(LGF_NUM,0),  NVL(P.Y2030_05,0)*NVL(LGF_NUM,0),	 NVL(P.Y2030_06,0)*NVL(LGF_NUM,0),  NVL(P.Y2030_07,0)*NVL(LGF_NUM,0),  NVL(P.Y2030_08,0)*NVL(LGF_NUM,0),  NVL(P.Y2030_09,0)*NVL(LGF_NUM,0),  NVL(P.Y2030_10,0)*NVL(LGF_NUM,0),	NVL(P.Y2030_11,0)*NVL(LGF_NUM,0),  NVL(P.Y2030_12,0)*NVL(LGF_NUM,0)" + 
        " FROM PRO_PLAN_COST_BODY_TEMP P WHERE (P.LGF_TYPE =\'L\' OR P.LGF_TYPE =\'G\') AND P.ROW_ID = T.ROW_ID AND P.DATA_TYPE=\'" +this.TYPE_ZZX+"\')"+ 
        " WHERE EXISTS (SELECT 1 FROM PRO_PLAN_COST_BODY_TEMP P1 WHERE P1.ROW_ID = T.ROW_ID AND P1.DATA_TYPE=\'" +this.TYPE_ZZX+"\'"+
            " AND P1.LGF_TYPE =\'L\' OR P1.LGF_TYPE =\'G\')";
        
        return sql;
    }
    
    private String sqlFUpdate(){
        String sql = "UPDATE PRO_PLAN_COST_BODY_TEMP T SET " + 
        "      (T.R2010_01,  T.R2010_02,  T.R2010_03,  T.R2010_04,  T.R2010_05,  T.R2010_06,  T.R2010_07,  T.R2010_08,  T.R2010_09,  T.R2010_10,  T.R2010_11,  T.R2010_12," + 
        "      T.R2011_01,  T.R2011_02,  T.R2011_03,  T.R2011_04,  T.R2011_05,  T.R2011_06,  T.R2011_07,  T.R2011_08,  T.R2011_09,  T.R2011_10,  T.R2011_11,  T.R2011_12," + 
        "      T.R2012_01,  T.R2012_02,  T.R2012_03,  T.R2012_04,  T.R2012_05,  T.R2012_06,  T.R2012_07,  T.R2012_08,  T.R2012_09,  T.R2012_10,  T.R2012_11,  T.R2012_12," + 
        "      T.R2013_01,  T.R2013_02,  T.R2013_03,  T.R2013_04,  T.R2013_05,  T.R2013_06,  T.R2013_07,  T.R2013_08,  T.R2013_09,  T.R2013_10,  T.R2013_11,  T.R2013_12," + 
        "      T.R2014_01,  T.R2014_02,  T.R2014_03,  T.R2014_04,  T.R2014_05,  T.R2014_06,  T.R2014_07,  T.R2014_08,  T.R2014_09,  T.R2014_10,  T.R2014_11,  T.R2014_12," + 
        "      T.R2015_01,  T.R2015_02,  T.R2015_03,  T.R2015_04,  T.R2015_05,  T.R2015_06,  T.R2015_07,  T.R2015_08,  T.R2015_09,  T.R2015_10,  T.R2015_11,  T.R2015_12," + 
        "      T.R2016_01,  T.R2016_02,  T.R2016_03,  T.R2016_04,  T.R2016_05,  T.R2016_06,  T.R2016_07,  T.R2016_08,  T.R2016_09,  T.R2016_10,  T.R2016_11,  T.R2016_12," + 
        "      T.R2017_01,  T.R2017_02,  T.R2017_03,  T.R2017_04,  T.R2017_05,  T.R2017_06,  T.R2017_07,  T.R2017_08,  T.R2017_09,  T.R2017_10,  T.R2017_11,  T.R2017_12," + 
        "      T.R2018_01,  T.R2018_02,  T.R2018_03,  T.R2018_04,  T.R2018_05,  T.R2018_06,  T.R2018_07,  T.R2018_08,  T.R2018_09,  T.R2018_10,  T.R2018_11,  T.R2018_12," + 
        "      T.R2019_01,  T.R2019_02,  T.R2019_03,  T.R2019_04,  T.R2019_05,  T.R2019_06,  T.R2019_07,  T.R2019_08,  T.R2019_09,  T.R2019_10,  T.R2019_11,  T.R2019_12," + 
        "      T.R2020_01,  T.R2020_02,  T.R2020_03,  T.R2020_04,  T.R2020_05,  T.R2020_06,  T.R2020_07,  T.R2020_08,  T.R2020_09,  T.R2020_10,  T.R2020_11,  T.R2020_12," + 
        "      T.R2021_01,  T.R2021_02,  T.R2021_03,  T.R2021_04,  T.R2021_05,  T.R2021_06,  T.R2021_07,  T.R2021_08,  T.R2021_09,  T.R2021_10,  T.R2021_11,  T.R2021_12," + 
        "      T.R2022_01,  T.R2022_02,  T.R2022_03,  T.R2022_04,  T.R2022_05,  T.R2022_06,  T.R2022_07,  T.R2022_08,  T.R2022_09,  T.R2022_10,  T.R2022_11,  T.R2022_12," + 
        "      T.R2023_01,  T.R2023_02,  T.R2023_03,  T.R2023_04,  T.R2023_05,  T.R2023_06,  T.R2023_07,  T.R2023_08,  T.R2023_09,  T.R2023_10,  T.R2023_11,  T.R2023_12," + 
        "      T.R2024_01,  T.R2024_02,  T.R2024_03,  T.R2024_04,  T.R2024_05,  T.R2024_06,  T.R2024_07,  T.R2024_08,  T.R2024_09,  T.R2024_10,  T.R2024_11,  T.R2024_12," + 
        "      T.R2025_01,  T.R2025_02,  T.R2025_03,  T.R2025_04,  T.R2025_05,  T.R2025_06,  T.R2025_07,  T.R2025_08,  T.R2025_09,  T.R2025_10,  T.R2025_11,  T.R2025_12," + 
        "      T.R2026_01,  T.R2026_02,  T.R2026_03,  T.R2026_04,  T.R2026_05,  T.R2026_06,  T.R2026_07,  T.R2026_08,  T.R2026_09,  T.R2026_10,  T.R2026_11,  T.R2026_12," + 
        "      T.R2027_01,  T.R2027_02,  T.R2027_03,  T.R2027_04,  T.R2027_05,  T.R2027_06,  T.R2027_07,  T.R2027_08,  T.R2027_09,  T.R2027_10,  T.R2027_11,  T.R2027_12," + 
        "      T.R2028_01,  T.R2028_02,  T.R2028_03,  T.R2028_04,  T.R2028_05,  T.R2028_06,  T.R2028_07,  T.R2028_08,  T.R2028_09,  T.R2028_10,  T.R2028_11,  T.R2028_12," + 
        "      T.R2029_01,  T.R2029_02,  T.R2029_03,  T.R2029_04,  T.R2029_05,  T.R2029_06,  T.R2029_07,  T.R2029_08,  T.R2029_09,  T.R2029_10,  T.R2029_11,  T.R2029_12," + 
        "      T.R2030_01,  T.R2030_02,  T.R2030_03,  T.R2030_04,  T.R2030_05,  T.R2030_06,  T.R2030_07,  T.R2030_08,  T.R2030_09,  T.R2030_10,  T.R2030_11,  T.R2030_12)" + 
        "  =(SELECT " + 
        " NVL(P.Y2010_01,0),	NVL(P.Y2010_02,0),	NVL(P.Y2010_03,0),	NVL(P.Y2010_04,0),	NVL(P.Y2010_05,0),	NVL(P.Y2010_06,0),	NVL(P.Y2010_07,0),	NVL(P.Y2010_08,0),	NVL(P.Y2010_09,0),	NVL(P.Y2010_10,0),	NVL(P.Y2010_11,0),	NVL(P.Y2010_12,0)," + 
        "NVL(P.Y2011_01,0),	NVL(P.Y2011_02,0),	NVL(P.Y2011_03,0),	NVL(P.Y2011_04,0),	NVL(P.Y2011_05,0),	NVL(P.Y2011_06,0),	NVL(P.Y2011_07,0),	NVL(P.Y2011_08,0),	NVL(P.Y2011_09,0),	NVL(P.Y2011_10,0),	NVL(P.Y2011_11,0),	NVL(P.Y2011_12,0)," + 
        "NVL(P.Y2012_01,0),	NVL(P.Y2012_02,0),	NVL(P.Y2012_03,0),	NVL(P.Y2012_04,0),	NVL(P.Y2012_05,0),	NVL(P.Y2012_06,0),	NVL(P.Y2012_07,0),	NVL(P.Y2012_08,0),	NVL(P.Y2012_09,0),	NVL(P.Y2012_10,0),	NVL(P.Y2012_11,0),	NVL(P.Y2012_12,0)," + 
        "NVL(P.Y2013_01,0),	NVL(P.Y2013_02,0),	NVL(P.Y2013_03,0),	NVL(P.Y2013_04,0),	NVL(P.Y2013_05,0),	NVL(P.Y2013_06,0),	NVL(P.Y2013_07,0),	NVL(P.Y2013_08,0),	NVL(P.Y2013_09,0),	NVL(P.Y2013_10,0),	NVL(P.Y2013_11,0),	NVL(P.Y2013_12,0)," + 
        "NVL(P.Y2014_01,0),	NVL(P.Y2014_02,0),	NVL(P.Y2014_03,0),	NVL(P.Y2014_04,0),	NVL(P.Y2014_05,0),	NVL(P.Y2014_06,0),	NVL(P.Y2014_07,0),	NVL(P.Y2014_08,0),	NVL(P.Y2014_09,0),	NVL(P.Y2014_10,0),	NVL(P.Y2014_11,0),	NVL(P.Y2014_12,0)," + 
        "NVL(P.Y2015_01,0),	NVL(P.Y2015_02,0),	NVL(P.Y2015_03,0),	NVL(P.Y2015_04,0),	NVL(P.Y2015_05,0),	NVL(P.Y2015_06,0),	NVL(P.Y2015_07,0),	NVL(P.Y2015_08,0),	NVL(P.Y2015_09,0),	NVL(P.Y2015_10,0),	NVL(P.Y2015_11,0),	NVL(P.Y2015_12,0)," + 
        "NVL(P.Y2016_01,0),	NVL(P.Y2016_02,0),	NVL(P.Y2016_03,0),	NVL(P.Y2016_04,0),	NVL(P.Y2016_05,0),	NVL(P.Y2016_06,0),	NVL(P.Y2016_07,0),	NVL(P.Y2016_08,0),	NVL(P.Y2016_09,0),	NVL(P.Y2016_10,0),	NVL(P.Y2016_11,0),	NVL(P.Y2016_12,0)," + 
        "NVL(P.Y2017_01,0),	NVL(P.Y2017_02,0),	NVL(P.Y2017_03,0),	NVL(P.Y2017_04,0),	NVL(P.Y2017_05,0),	NVL(P.Y2017_06,0),	NVL(P.Y2017_07,0),	NVL(P.Y2017_08,0),	NVL(P.Y2017_09,0),	NVL(P.Y2017_10,0),	NVL(P.Y2017_11,0),	NVL(P.Y2017_12,0)," + 
        "NVL(P.Y2018_01,0),	NVL(P.Y2018_02,0),	NVL(P.Y2018_03,0),	NVL(P.Y2018_04,0),	NVL(P.Y2018_05,0),	NVL(P.Y2018_06,0),	NVL(P.Y2018_07,0),	NVL(P.Y2018_08,0),	NVL(P.Y2018_09,0),	NVL(P.Y2018_10,0),	NVL(P.Y2018_11,0),	NVL(P.Y2018_12,0)," + 
        "NVL(P.Y2019_01,0),	NVL(P.Y2019_02,0),	NVL(P.Y2019_03,0),	NVL(P.Y2019_04,0),	NVL(P.Y2019_05,0),	NVL(P.Y2019_06,0),	NVL(P.Y2019_07,0),	NVL(P.Y2019_08,0),	NVL(P.Y2019_09,0),	NVL(P.Y2019_10,0),	NVL(P.Y2019_11,0),	NVL(P.Y2019_12,0)," + 
        "NVL(P.Y2020_01,0),	NVL(P.Y2020_02,0),	NVL(P.Y2020_03,0),	NVL(P.Y2010_04,0),	NVL(P.Y2020_05,0),	NVL(P.Y2020_06,0),	NVL(P.Y2020_07,0),	NVL(P.Y2020_08,0),	NVL(P.Y2020_09,0),	NVL(P.Y2020_10,0),	NVL(P.Y2020_11,0),	NVL(P.Y2020_12,0)," + 
        "NVL(P.Y2021_01,0),	NVL(P.Y2021_02,0),	NVL(P.Y2021_03,0),	NVL(P.Y2021_04,0),	NVL(P.Y2021_05,0),	NVL(P.Y2021_06,0),	NVL(P.Y2021_07,0),	NVL(P.Y2021_08,0),	NVL(P.Y2021_09,0),	NVL(P.Y2021_10,0),	NVL(P.Y2021_11,0),	NVL(P.Y2021_12,0)," + 
        "NVL(P.Y2022_01,0),	NVL(P.Y2022_02,0),	NVL(P.Y2022_03,0),	NVL(P.Y2022_04,0),	NVL(P.Y2022_05,0),	NVL(P.Y2022_06,0),	NVL(P.Y2022_07,0),	NVL(P.Y2022_08,0),	NVL(P.Y2022_09,0),	NVL(P.Y2022_10,0),	NVL(P.Y2022_11,0),	NVL(P.Y2022_12,0)," + 
        "NVL(P.Y2023_01,0),	NVL(P.Y2023_02,0),	NVL(P.Y2023_03,0),	NVL(P.Y2023_04,0),	NVL(P.Y2023_05,0),	NVL(P.Y2023_06,0),	NVL(P.Y2023_07,0),	NVL(P.Y2023_08,0),	NVL(P.Y2023_09,0),	NVL(P.Y2023_10,0),	NVL(P.Y2023_11,0),	NVL(P.Y2023_12,0)," + 
        "NVL(P.Y2024_01,0),	NVL(P.Y2024_02,0),	NVL(P.Y2024_03,0),	NVL(P.Y2024_04,0),	NVL(P.Y2024_05,0),	NVL(P.Y2024_06,0),	NVL(P.Y2024_07,0),	NVL(P.Y2024_08,0),	NVL(P.Y2024_09,0),	NVL(P.Y2024_10,0),	NVL(P.Y2024_11,0),	NVL(P.Y2024_12,0)," + 
        "NVL(P.Y2025_01,0),	NVL(P.Y2025_02,0),	NVL(P.Y2025_03,0),	NVL(P.Y2025_04,0),	NVL(P.Y2025_05,0),	NVL(P.Y2025_06,0),	NVL(P.Y2025_07,0),	NVL(P.Y2025_08,0),	NVL(P.Y2025_09,0),	NVL(P.Y2025_10,0),	NVL(P.Y2025_11,0),	NVL(P.Y2025_12,0)," + 
        "NVL(P.Y2026_01,0),	NVL(P.Y2026_02,0),	NVL(P.Y2026_03,0),	NVL(P.Y2026_04,0),	NVL(P.Y2026_05,0),	NVL(P.Y2026_06,0),	NVL(P.Y2026_07,0),	NVL(P.Y2026_08,0),	NVL(P.Y2026_09,0),	NVL(P.Y2026_10,0),	NVL(P.Y2026_11,0),	NVL(P.Y2026_12,0)," + 
        "NVL(P.Y2027_01,0),	NVL(P.Y2027_02,0),	NVL(P.Y2027_03,0),	NVL(P.Y2027_04,0),	NVL(P.Y2027_05,0),	NVL(P.Y2027_06,0),	NVL(P.Y2027_07,0),	NVL(P.Y2027_08,0),	NVL(P.Y2027_09,0),	NVL(P.Y2027_10,0),	NVL(P.Y2027_11,0),	NVL(P.Y2027_12,0)," + 
        "NVL(P.Y2028_01,0),	NVL(P.Y2028_02,0),	NVL(P.Y2028_03,0),	NVL(P.Y2028_04,0),	NVL(P.Y2028_05,0),	NVL(P.Y2028_06,0),	NVL(P.Y2028_07,0),	NVL(P.Y2028_08,0),	NVL(P.Y2028_09,0),	NVL(P.Y2028_10,0),	NVL(P.Y2028_11,0),	NVL(P.Y2028_12,0)," + 
        "NVL(P.Y2029_01,0),	NVL(P.Y2029_02,0),	NVL(P.Y2029_03,0),	NVL(P.Y2029_04,0),	NVL(P.Y2029_05,0),	NVL(P.Y2029_06,0),	NVL(P.Y2029_07,0),	NVL(P.Y2029_08,0),	NVL(P.Y2029_09,0),	NVL(P.Y2029_10,0),	NVL(P.Y2029_11,0),	NVL(P.Y2029_12,0)," + 
        "NVL(P.Y2030_01,0),	NVL(P.Y2030_02,0),	NVL(P.Y2030_03,0),	NVL(P.Y2030_04,0),	NVL(P.Y2030_05,0),	NVL(P.Y2030_06,0),	NVL(P.Y2030_07,0),	NVL(P.Y2030_08,0),	NVL(P.Y2030_09,0),	NVL(P.Y2030_10,0),	NVL(P.Y2030_11,0),	NVL(P.Y2030_12,0)" + 
        " FROM PRO_PLAN_COST_BODY_TEMP P WHERE P.LGF_TYPE =\'F\' AND P.ROW_ID = T.ROW_ID AND P.DATA_TYPE=\'" +this.TYPE_ZZX+"\')"+ 
        " WHERE EXISTS (SELECT 1 FROM PRO_PLAN_COST_BODY_TEMP P1 WHERE P1.ROW_ID = T.ROW_ID AND P1.DATA_TYPE=\'" +this.TYPE_ZZX+"\'"+
            " AND P1.LGF_TYPE =\'F\')";
        return sql;
    }
    public void inputPro(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.ZZX_INPUTPRO(?)}", 0);
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
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_ZZX.ZZX_VALIDATION(?,?,?)}", 0);
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
                                                   "基准计划成本",
                                                    this.pcColsDef,
                                                    outputStream);
            
                writer.writeToFile();
            }else{
                PcExcel2007WriterImpl writer = new PcExcel2007WriterImpl(
                                                    this.querySql(this.getLabelMap()),
                                                    2,this.pcColsDef);
                writer.process(outputStream, "基准计划成本");
                outputStream.flush();
            }
        } catch (Exception e) {
            this._logger.severe(e);
        } 
    }
    
    //导出文件名
    public String getExportDataExcelName(){
        if(isXlsx){
            return "在执行项目.xlsx";
        }else{
            return "在执行项目.xls";
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

    public void beBlocked(ActionEvent actionEvent) {
        String sql = "UPDATE PRO_PLAN_COST_HEADER SET (IS_BLOCK) = 'true' WHERE HLS_YEAR = \'"+year;
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
    }
    //显示错误框
    public void showErrorPop(){
        ViewObject vo = ADFUtils.findIterator("ProPlanCostViewIterator").getViewObject();
        vo.setNamedWhereClauseParam("dataType", this.TYPE_ZZX);
        vo.executeQuery();
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        this.errorWindow.show(ph);
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

    public void outBlock(ActionEvent actionEvent) {
        String sql = "UPDATE PRO_PLAN_COST_HEADER SET (IS_BLOCK) = 'false' WHERE HLS_YEAR = \'"+year;
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
}
