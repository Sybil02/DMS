package dgpt;

import common.DmsUtils;

import common.JSFUtils;

import dms.login.Person;

import java.sql.CallableStatement;
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

public class HhBean {
    private Person curUser;
    public HhBean() {
        super();
        this.curUser = (Person)(ADFContext.getCurrent().getSessionScope().get("cur_user"));
        this.initList();
        ViewObject vo = DmsUtils.getDmsApplicationModule().getHhXqysVO();
        vo.setWhereClause("1=2");
        vo.executeQuery();
    }
    private String year;
    private List<SelectItem> yearList;
    private String entity;
    private List<SelectItem> entityList;
    private String pLine;
    private List<SelectItem> pLineList;
    private String version;
    private List<SelectItem> versionList;
    private String hLine;
    private List<SelectItem> hlList;
    private String proType;
    private List<SelectItem> proTypeList;
    private String accType;
    private List<SelectItem> accTypeList;
    
    private void initList(){
        this.yearList = queryValues("HLS_YEAR");
        this.entityList = queryValues("HLS_ENTITY_C");
        this.pLineList = queryValues("HLS_PRODUCT_LINE");
        this.versionList = queryValues("HLS_MODEL");
        this.proTypeList = queryValues("HLS_PROJECT_TYPE_C");
        this.hlList = queryValues("HLS_INDUSTRY_LINE");
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
        ViewObject vo = DmsUtils.getDmsApplicationModule().getHhXqysVO();
        Row [] rows = vo.getAllRowsInRange();
        for(Row row : rows){
            ViewRowImpl vRow = (ViewRowImpl)row;
            if(vRow.getEntities()[0].getEntityState() == 0){
                vRow.setAttribute("HlsYear", year);
                vRow.setAttribute("EntityName", entity);
                vRow.setAttribute("ProductLine", pLine);
                vRow.setAttribute("Version", version);
                vRow.setAttribute("ProjectType", proType);
                vRow.setAttribute("IndustryLine", hLine);
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
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_SYSTEM.HH_AFTER_PRO(?,?,?,?,?,?)}", 0);
        try {
            cs.setString(1, year);
            cs.setString(2, entity);
            cs.setString(3, pLine);
            cs.setString(4, version);
            cs.setString(5, proType);
            cs.setString(6, hLine);
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
        CallableStatement cs = trans.createCallableStatement("{CALl DMS_SYSTEM.HH_VALIDATION(?,?)}", 0);
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
        ViewObject vo = DmsUtils.getDmsApplicationModule().getHhXqysVO();
        Row [] rows = vo.getAllRowsInRange();
        
        //删除临时表上次数据
        String dSql = "DELETE FROM HH_XQYS_TEMP T WHERE T.CREATED_BY = '" + this.curUser.getId() + "'";
        
        try{
        stat.executeUpdate(dSql);
        trans.commit();
        //COPY DATA 
        for(Row row : rows){
            ViewRowImpl vRow = (ViewRowImpl)row;
            if(vRow.getEntities()[0].getEntityState() == 2){
                String sql = "INSERT INTO HH_XQYS_TEMP (HLS_YEAR, ENTITY_NAME, PRODUCT_LINE, VERSION, PROJECT_TYPE, INDUSTRY_LINE, ACC_TYPE," + 
                "DETAIL_ACC_TYPE, QUNTITY, UNIT_COST, AMOUNT, LAST_NOV, JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV," + 
                "DEC, NEXT_JAN, NEXT_FEB, NEXT_MAR, NEXT_APR, NEXT_MAY, NEXT_JUN, AFTER_JULY, CREATED_BY) VALUES (";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("HlsYear")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("EntityName")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("ProductLine")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Version")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("ProjectType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("IndustryLine")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AccType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("DetailAccType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Quntity")) + "',";      
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("UnitCost")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Amount")) + "',";
                
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("LastNov")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Jan")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Feb")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Mar")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Apr")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("May")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Jun")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Jul")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Aug")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Sep")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Oct")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Nov")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Dec")) + "',";
                
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextJan")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextFeb")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextMar")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextApr")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextMay")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextJun")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AfterJuly")) + "',";
                sql = sql + "'" + this.curUser.getId() + "')";
                stat.addBatch(sql);
            }else if(vRow.getEntities()[0].getEntityState() == 0){
                String sql = "INSERT INTO HH_XQYS_TEMP (HLS_YEAR, ENTITY_NAME, PRODUCT_LINE, VERSION, PROJECT_TYPE, INDUSTRY_LINE, ACC_TYPE," + 
                "DETAIL_ACC_TYPE, QUNTITY, UNIT_COST, AMOUNT, LAST_NOV, JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV," + 
                "DEC, NEXT_JAN, NEXT_FEB, NEXT_MAR, NEXT_APR, NEXT_MAY, NEXT_JUN, AFTER_JULY, CREATED_BY) VALUES (";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("HlsYear")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("EntityName")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("ProductLine")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Version")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("ProjectType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("IndustryLine")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AccType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("DetailAccType")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Quntity")) + "',";      
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("UnitCost")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Amount")) + "',";
                
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("LastNov")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Jan")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Feb")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Mar")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Apr")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("May")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Jun")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Jul")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Aug")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Sep")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Oct")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Nov")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("Dec")) + "',";
                
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextJan")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextFeb")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextMar")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextApr")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextMay")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("NextJun")) + "',";
                sql = sql + "'" + ObjectUtils.toString(vRow.getAttribute("AfterJuly")) + "',";
                sql = sql + "'" + this.curUser.getId() + "')";
                //System.out.println(sql);
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
    
    /*HlsYear, year   EntityName, entity
        ProductLine, pLine   Version, version
        ProjectType, proType   IndustryLine, hLine*/
    public void queryData(ActionEvent actionEvent) {
        if(year == null || entity == null || pLine == null 
           || version == null || proType == null || hLine == null ){
            JSFUtils.addFacesInformationMessage("表头条件不能为空");
            return;    
        }

        ViewObject vo = DmsUtils.getDmsApplicationModule().getHhXqysVO();
        String whereClause = "HLS_YEAR = '" + year + "' AND ENTITY_NAME = '" + entity + "' AND PRODUCT_LINE ='"
            + pLine +"' AND VERSION = '" + version + "' AND PROJECT_TYPE ='" + proType + "' AND INDUSTRY_LINE ='"
            + hLine +  "'";
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

    public void setPLine(String pLine) {
        this.pLine = pLine;
    }

    public String getPLine() {
        return pLine;
    }

    public void setPLineList(List<SelectItem> pLineList) {
        this.pLineList = pLineList;
    }

    public List<SelectItem> getPLineList() {
        return pLineList;
    }
}
