package dgpt;

import common.DmsUtils;

import common.JSFUtils;

import dms.login.Person;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.Types;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import oracle.adf.share.ADFContext;

import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.server.DBTransaction;

import oracle.jbo.server.ViewRowImpl;

import org.apache.commons.lang.ObjectUtils;

import team.epm.module.DmsModuleImpl;

public class BhBean {
    private Person curUser;
    public BhBean() {
        super();
        this.curUser = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
        this.initList();
        ViewObject vo = DmsUtils.getDmsApplicationModule().getBhXqysVO();
        vo.setWhereClause("1=2");
        vo.executeQuery();
    }
    private String year;
    private List<SelectItem> yearList;
    private String entity;
    private List<SelectItem> entityList;
    private String proCode;
    private List<SelectItem> proCodeList;
    private String version;
    private List<SelectItem> versionList;
    private String hLine;
    private List<SelectItem> hlList;
    private String yLine;
    private List<SelectItem> ylList;
    private String proType;
    private List<SelectItem> proTypeList;
    private String accType;
    private List<SelectItem> accTypeList;
    
    private void initList(){
        this.yearList = queryValues("HLS_YEAR");
        this.entityList = queryValues("HLS_ENTITY_C");
        this.proCodeList = queryProCode("BH_USER_PRO_V");
        this.versionList = queryValues("HLS_MODEL");
        this.proTypeList = queryValues("HLS_BEIHE_PROJECT_C2");
        this.hlList = queryValues("HLS_INDUSTRY_LINE");
        this.ylList = queryValues("HLS_STAFFING_LOB");
        this.accTypeList = queryValues("HLS_PROJECT_ACCOUNT_C");
    }
    
    private List<SelectItem> queryProCode(String source){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT CODE,MEANING FROM " + source + " WHERE USER_ID ='" + this.curUser.getId() + "'";
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
    
    private List<SelectItem> queryValues(String source){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "SELECT CODE,MEANING FROM " + source;
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
    
    public void saveData(ActionEvent actionEvent) {
        ViewObject vo = DmsUtils.getDmsApplicationModule().getBhXqysVO();
        Row [] rows = vo.getAllRowsInRange();
        for(Row row : rows){
            ViewRowImpl vRow = (ViewRowImpl)row;
            if(vRow.getEntities()[0].getEntityState() == 0){
                vRow.setAttribute("HlsYear", year);
                vRow.setAttribute("EntityName", entity);
                vRow.setAttribute("ProjectCode", proCode);
                vRow.setAttribute("Version", version);
                vRow.setAttribute("IndustryLine", hLine);
                vRow.setAttribute("BusinessLine", yLine);
                vRow.setAttribute("ProjectType", proType);
            }
        }
        //数据到临时表
        this.copyToTemp();
        //校验
        if(this.validation()){
            vo.getApplicationModule().getTransaction().commit();   
        }else{
            JSFUtils.addFacesErrorMessage("数据校验不通过！");
        }
        //善后程序
        this.afterPro();
        
    }
    
    public void afterPro(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_SYSTEM.BH_AFTER_PRO(?,?,?,?,?,?,?)}", 0);
        try {
            cs.setString(1, year);
            cs.setString(2, entity);
            cs.setString(3, proCode);
            cs.setString(4, version);
            cs.setString(5, hLine);
            cs.setString(6, yLine);
            cs.setString(7, proType);
            cs.execute();
            trans.commit();
            cs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean validation(){
        boolean flag = true;
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getDBTransaction();
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_SYSTEM.BH_VALIDATION(?,?)}", 0);
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
    
    public void copyToTemp(){
        DBTransaction trans = (DBTransaction)DmsUtils.getDcmApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        ViewObject vo = DmsUtils.getDmsApplicationModule().getBhXqysVO();
        Row [] rows = vo.getAllRowsInRange();
        
        //删除临时表上次数据
        String dSql = "DELETE FROM BH_XQYS_TEMP T WHERE T.CREATED_BY = '" + this.curUser.getId() + "'";
        
        try{
        stat.executeUpdate(dSql);
        trans.commit();
        //COPY DATA 
        for(Row row : rows){
            ViewRowImpl vRow = (ViewRowImpl)row;
            if(vRow.getEntities()[0].getEntityState() == 2){
                String sql = "INSERT INTO BH_XQYS_TEMP (HLS_YEAR, ENTITY_NAME, PROJECT_CODE, VERSION, INDUSTRY_LINE, BUSINESS_LINE, PROJECT_TYPE, ACC_TYPE, " +
                    "UNIT, QUNTITY, UNIT_COST, AMOUNT, AMOUNT_ADJ, AFT_AMOUNT_ADJ, LAST_NOV, CUR_JAN, CUR_FEB, CUR_MAR, CUR_APR, CUR_MAY, " +
                    "CUR_JUN, CUR_JUL, CUR_AUG, CUR_SEP, CUR_OCT, CUR_NOV, CUR_DEC, NEXT_JAN, NEXT_FEB, NEXT_MAR, NEXT_APR, NEXT_MAY, NEXT_JUN, AFTER_JULY, " +
                    "CREATED_BY, ROW_ID) VALUES (";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("HlsYear")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("EntityName")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("ProjectCode")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Version")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("IndustryLine")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("BusinessLine")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("ProjectType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AccType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Unit")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Quntity")) + "',";      
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("UnitCost")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Amount")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AmountAdj")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AftAmountAdj")) + "',";
                
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("LastNov")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurJan")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurFeb")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurMar")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurApr")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurMay")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurJun")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurJul")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurAug")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurSep")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurOct")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurNov")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurDec")) + "',";
                
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextJan")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextFeb")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextMar")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextApr")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextMay")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextJun")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AfterJuly")) + "',";
                sql = sql + "'" + this.curUser.getId() + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("RowID")) + "')";
                stat.addBatch(sql);
            }else if(vRow.getEntities()[0].getEntityState() == 0){
                String sql = "INSERT INTO BH_XQYS_TEMP (HLS_YEAR, ENTITY_NAME, PROJECT_CODE, VERSION, INDUSTRY_LINE, BUSINESS_LINE, PROJECT_TYPE, ACC_TYPE, " +
                    "UNIT, QUNTITY, UNIT_COST, AMOUNT, AMOUNT_ADJ, AFT_AMOUNT_ADJ, LAST_NOV, CUR_JAN, CUR_FEB, CUR_MAR, CUR_APR, CUR_MAY, " +
                    "CUR_JUN, CUR_JUL, CUR_AUG, CUR_SEP, CUR_OCT, CUR_NOV, CUR_DEC, NEXT_JAN, NEXT_FEB, NEXT_MAR, NEXT_APR, NEXT_MAY, NEXT_JUN, AFTER_JULY, " +
                    "CREATED_BY, ROW_ID) VALUES (";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("HlsYear")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("EntityName")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("ProjectCode")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Version")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("IndustryLine")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("BusinessLine")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("ProjectType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AccType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Unit")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Quntity")) + "',";      
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("UnitCost")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Amount")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AmountAdj")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AftAmountAdj")) + "',";
                
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("LastNov")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurJan")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurFeb")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurMar")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurApr")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurMay")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurJun")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurJul")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurAug")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurSep")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurOct")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurNov")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("CurDec")) + "',";
                
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextJan")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextFeb")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextMar")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextApr")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextMay")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextJun")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AfterJuly")) + "',";
                sql = sql + "'" + this.curUser.getId() + "',";
                sql = sql + "'')" ;
                stat.addBatch(sql);
            }

        }
            stat.executeBatch();
            trans.commit();
            stat.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    /*HLS_YEAR   '年';ENTITY_NAME  '实体';PROJECT_CODE '项目编号';VERSION '版本';
    INDUSTRY_LINE '行业线';BUSINESS_LINE '业务线';PROJECT_TYPE '项目类型';ACC_TYPE '科目类';
    DETAIL_ACC_TYPE '科目物料明细';*/
    public void queryData(ActionEvent actionEvent) {
        if(year == null || entity == null || proCode == null 
           || version == null || hLine == null || yLine == null || proType == null ){
            JSFUtils.addFacesInformationMessage("表头条件不能为空");
            return;    
        }

        ViewObject vo = DmsUtils.getDmsApplicationModule().getBhXqysVO();
        String whereClause = "HLS_YEAR = '" + year + "' AND ENTITY_NAME = '" + entity + "' AND PROJECT_CODE ='"
            + proCode +"' AND VERSION = '" + version + "' AND INDUSTRY_LINE ='" + hLine + "' AND BUSINESS_LINE ='"
            + yLine + "' AND PROJECT_TYPE ='" + proType + "'";
        vo.setWhereClause(whereClause);
        vo.executeQuery();
    }

    public void setYearList(List<SelectItem> yearList) {
        this.yearList = yearList;
    }

    public List<SelectItem> getYearList() {
        return yearList;
    }

    public void setEntityList(List<SelectItem> entityList) {
        this.entityList = entityList;
    }

    public List<SelectItem> getEntityList() {
        return entityList;
    }

    public void setproCodeList(List<SelectItem> proCodeList) {
        this.proCodeList = proCodeList;
    }

    public List<SelectItem> getproCodeList() {
        return proCodeList;
    }

    public void setVersionList(List<SelectItem> versionList) {
        this.versionList = versionList;
    }

    public List<SelectItem> getVersionList() {
        return versionList;
    }

    public void setProTypeList(List<SelectItem> proTypeList) {
        this.proTypeList = proTypeList;
    }

    public List<SelectItem> getProTypeList() {
        return proTypeList;
    }

    public void setHlList(List<SelectItem> hlList) {
        this.hlList = hlList;
    }

    public List<SelectItem> getHlList() {
        return hlList;
    }

    public void setAccTypeList(List<SelectItem> accTypeList) {
        this.accTypeList = accTypeList;
    }

    public List<SelectItem> getAccTypeList() {
        return accTypeList;
    }

    public void setYlList(List<SelectItem> ylList) {
        this.ylList = ylList;
    }

    public List<SelectItem> getYlList() {
        return ylList;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getYear() {
        return year;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }

    public void setProCode(String proCode) {
        this.proCode = proCode;
    }

    public String getProCode() {
        return proCode;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
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

    public void setProType(String proType) {
        this.proType = proType;
    }

    public String getProType() {
        return proType;
    }

    public void setAccType(String accType) {
        this.accType = accType;
    }

    public String getAccType() {
        return accType;
    }

    public void reset(ActionEvent actionEvent) {
        DmsUtils.getDmsApplicationModule().getTransaction().rollback();
        this.queryData(actionEvent);
    }

}
