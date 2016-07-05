package datamanager.ws;

import datamanager.entity.HlsBomEntity;

import datamanager.entity.PositionEntity;
import datamanager.entity.PsEntity;

import datamanager.entity.PsJob;
import datamanager.entity.PsMaster;

import datamanager.entity.PsMilepost;

import datamanager.entity.PsNetwork;

import datamanager.entity.PsOrg;

import datamanager.entity.PsWbs;

import datamanager.entity.PsWbsMaster;

import dms.quartz.utils.DBConnUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.jws.WebService;

@WebService
public class DataManager {
    public DataManager() {
        super();
    }

    /**
     * 岗位主数据
     * @param positionList
     * @return
     */
    public List<PositionEntity> SyncPosition(List<PositionEntity> positionList){
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        String sql =
            "INSERT INTO DMS_HR_POSITION (ZJGDM, ZBMDM, ZGWDM, ZGWMS, ZSJGW, ZQYBZ, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5," +
            "NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG, MSGID, NEW_GW) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlUp = "UPDATE DMS_HR_POSITION SET ZJGDM=?, ZBMDM=?, ZGWDM=?, ZGWMS=?, ZSJGW=?, ZQYBZ=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, "
            + " NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?, MSGID=? , NEW_GW=? "
            + "WHERE ZJGDM=? AND ZBMDM=? AND ZGWDM=? AND ZGWMS=? AND ZSJGW=? ";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;
        try {
            stat = conn.prepareStatement(sql);
            statUp = conn.prepareStatement(sqlUp);
            statExs = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (PositionEntity position : positionList) {
            try {
                Map<String,String> keyValue = new HashMap<String,String>();
                keyValue.put("ZJGDM", position.getZjgdm());
                keyValue.put("ZBMDM", position.getZbmdm());
                keyValue.put("ZGWDM", position.getZgwdm());
                keyValue.put("ZGWMS", position.getZgwms());
                keyValue.put("ZSJGW", position.getZsjgw());
                if(!this.pkValidate(statExs, "DMS_HR_POSITION", keyValue)){
                    stat.setString(1, position.getZjgdm());
                    stat.setString(2, position.getZbmdm());
                    stat.setString(3, position.getZgwdm());
                    stat.setString(4, position.getZgwms());
                    stat.setString(5, position.getZsjgw());
                    stat.setString(6, position.getZqybz());
                    stat.setString(7, position.getNote1());
                    stat.setString(8, position.getNote2());
                    stat.setString(9, position.getNote3());
                    stat.setString(10, position.getNote4());
                    stat.setString(11, position.getNote5());
                    stat.setString(12, position.getNote6());
                    stat.setString(13, position.getNote7());
                    stat.setString(14, position.getNote8());
                    stat.setString(15, position.getNote9());
                    stat.setString(16, position.getNote10());
                    stat.setString(17, position.getNote11());
                    stat.setString(18, position.getNote12());
                    stat.setString(19, position.getNote13());
                    stat.setString(20, position.getNote14());
                    stat.setString(21, position.getNote15());
                    stat.setString(22, position.getIfflg());
                    stat.setString(23, position.getIfmsg());
                    stat.setString(24, position.getMsgid());
                    stat.setString(25, position.getNew_gw());
                    stat.executeUpdate();
                }else{
                    statUp.setString(1, position.getZjgdm());
                    statUp.setString(2, position.getZbmdm());
                    statUp.setString(3, position.getZgwdm());
                    statUp.setString(4, position.getZgwms());
                    statUp.setString(5, position.getZsjgw());
                    statUp.setString(6, position.getZqybz());
                    statUp.setString(7, position.getNote1());
                    statUp.setString(8, position.getNote2());
                    statUp.setString(9, position.getNote3());
                    statUp.setString(10, position.getNote4());
                    statUp.setString(11, position.getNote5());
                    statUp.setString(12, position.getNote6());
                    statUp.setString(13, position.getNote7());
                    statUp.setString(14, position.getNote8());
                    statUp.setString(15, position.getNote9());
                    statUp.setString(16, position.getNote10());
                    statUp.setString(17, position.getNote11());
                    statUp.setString(18, position.getNote12());
                    statUp.setString(19, position.getNote13());
                    statUp.setString(20, position.getNote14());
                    statUp.setString(21, position.getNote15());
                    statUp.setString(22, position.getIfflg());
                    statUp.setString(23, position.getIfmsg());
                    statUp.setString(24, position.getMsgid());
                    statUp.setString(25, position.getNew_gw());
                    statUp.setString(26, position.getZjgdm());
                    statUp.setString(27, position.getZbmdm());
                    statUp.setString(28, position.getZgwdm());
                    statUp.setString(29, position.getZgwms());
                    statUp.setString(30, position.getZsjgw());
                    statUp.executeUpdate();
                }
                conn.commit();
                position.setIfflg("S");
            } catch (SQLException e) {
                e.printStackTrace();
                position.setIfflg("E");
                if (e.getMessage().length() > 50) {
                    position.setIfmsg(e.getMessage().substring(0, 49));
                } else {
                    position.setIfmsg(e.getMessage());
                }
            }
        }
        try {
            stat.close();
            statExs.close();
            statUp.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return positionList;
    }
        
    /**
     * 项目主数据
     * @param psEntityList
     * @param msgId
     * @return
     */

    public List<PsMaster> SyncPs(List<PsEntity> psEntityList,String msgId){
        
        boolean flag = true;
        for(PsEntity psEntity : psEntityList){
            if(!this.insertPsJob(psEntity.getPsJob())) flag = false;
            if(!this.insertPsMaster(psEntity.getPsMaster())) flag = false;
            if(!this.insertPsMilepost(psEntity.getPsMilepost())) flag = false;
            if(!this.insertPsNetwork(psEntity.getPsNetwork())) flag = false;
            if(!this.insertPsOrg(psEntity.getPsOrg())) flag = false;
            if(!this.insertPsWbs(psEntity.getPsWbs())) flag = false;
            if(!this.insertPsWbsMaster(psEntity.getPsWbsMaster())) flag = false;
            
            if(flag){
                
            }else{
                
            }
        }
        
        List<PsMaster> list = new ArrayList<PsMaster>();
        return list;
    }
        
    /**
     * 物料主数据
     * @param hlsBomList
     * @return
     */
    public List<HlsBomEntity> SyncWuliao(List<HlsBomEntity> hlsBomList) {
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        String sql =
            "INSERT INTO DMS_HLS_BOM (MATNR, MAKTX, WERKS, MTART, MEINS, MTBEZ, DISPO, ZGOOD, STPRS, PEINH, ZPLP2, " +
            "LVORM, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG, MSGID) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlUp = "UPDATE DMS_HLS_BOM SET MATNR=?, MAKTX=?, WERKS=?, MTART=?, MEINS=?, MTBEZ=?, DISPO=?, ZGOOD=?, STPRS=?, PEINH=?, ZPLP2=?,LVORM=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, NOTE6=?,"
            + " NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?, MSGID=? WHERE MATNR=? AND WERKS=?";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;
        try {
            stat = conn.prepareStatement(sql);
            statUp = conn.prepareStatement(sqlUp);
            statExs = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (HlsBomEntity bom : hlsBomList) {
            try {
                if(!this.bomIsExists(statExs, bom)){
                    stat.setString(1, bom.getMatnr());
                    stat.setString(2, bom.getMaktx());
                    stat.setString(3, bom.getWerks());
                    stat.setString(4, bom.getMtart());
                    stat.setString(5, bom.getMeins());
                    stat.setString(6, bom.getMtbez());
                    stat.setString(7, bom.getDispo());
                    stat.setString(8, bom.getZgood());
                    stat.setString(9, bom.getStprs());
                    stat.setString(10, bom.getPeinh());
                    stat.setString(11, bom.getZplp2());
                    stat.setString(12, bom.getLvorm());
                    stat.setString(13, bom.getNote1());
                    stat.setString(14, bom.getNote2());
                    stat.setString(15, bom.getNote3());
                    stat.setString(16, bom.getNote4());
                    stat.setString(17, bom.getNote5());
                    stat.setString(18, bom.getNote6());
                    stat.setString(19, bom.getNote7());
                    stat.setString(20, bom.getNote8());
                    stat.setString(21, bom.getNote9());
                    stat.setString(22, bom.getNote10());
                    stat.setString(23, bom.getNote11());
                    stat.setString(24, bom.getNote12());
                    stat.setString(25, bom.getNote13());
                    stat.setString(26, bom.getNote14());
                    stat.setString(27, bom.getNote15());
                    stat.setString(28, bom.getIfflg());
                    stat.setString(29, bom.getIfmsg());
                    stat.setString(30, bom.getMsgid());
                    stat.executeUpdate();
                }else{
                    statUp.setString(1, bom.getMatnr());
                    statUp.setString(2, bom.getMaktx());
                    statUp.setString(3, bom.getWerks());
                    statUp.setString(4, bom.getMtart());
                    statUp.setString(5, bom.getMeins());
                    statUp.setString(6, bom.getMtbez());
                    statUp.setString(7, bom.getDispo());
                    statUp.setString(8, bom.getZgood());
                    statUp.setString(9, bom.getStprs());
                    statUp.setString(10, bom.getPeinh());
                    statUp.setString(11, bom.getZplp2());
                    statUp.setString(12, bom.getLvorm());
                    statUp.setString(13, bom.getNote1());
                    statUp.setString(14, bom.getNote2());
                    statUp.setString(15, bom.getNote3());
                    statUp.setString(16, bom.getNote4());
                    statUp.setString(17, bom.getNote5());
                    statUp.setString(18, bom.getNote6());
                    statUp.setString(19, bom.getNote7());
                    statUp.setString(20, bom.getNote8());
                    statUp.setString(21, bom.getNote9());
                    statUp.setString(22, bom.getNote10());
                    statUp.setString(23, bom.getNote11());
                    statUp.setString(24, bom.getNote12());
                    statUp.setString(25, bom.getNote13());
                    statUp.setString(26, bom.getNote14());
                    statUp.setString(27, bom.getNote15());
                    statUp.setString(28, bom.getIfflg());
                    statUp.setString(29, bom.getIfmsg());
                    statUp.setString(30, bom.getMsgid());
                    statUp.setString(31, bom.getMatnr());
                    statUp.setString(32, bom.getWerks());
                    statUp.executeUpdate();
                }
                conn.commit();
                bom.setIfflg("S");
            } catch (SQLException e) {
                e.printStackTrace();
                bom.setIfflg("E");
                if (e.getMessage().length() > 50) {
                    bom.setIfmsg(e.getMessage().substring(0, 49));
                } else {
                    bom.setIfmsg(e.getMessage());
                }
            }
        }
        try {
            stat.close();
            statExs.close();
            statUp.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hlsBomList;
    }
    
    private boolean insertPsJob(List<PsJob> psJob){
        return true;    
    }
    private boolean insertPsMaster(List<PsMaster> psMaster){
        return true;    
    }
    private boolean insertPsMilepost(List<PsMilepost> psMilepost){
        return true;    
    }
    private boolean insertPsNetwork(List<PsNetwork> psNetwork){
        return true;    
    }
    private boolean insertPsOrg(List<PsOrg> psOrg){
        return true;    
    }
    private boolean insertPsWbs(List<PsWbs> psWbs){
        return true;    
    }
    private boolean insertPsWbsMaster(List<PsWbsMaster> psWbsMaster){
        return true;    
    }
    
    private boolean bomIsExists(Statement statExs,HlsBomEntity bom){
        String sql = "SELECT 1 FROM DMS_HLS_BOM T WHERE T.MATNR = '" + bom.getMatnr() + "' AND T.WERKS = '" + bom.getWerks() + "'";
        ResultSet rs;
        try {
            rs = statExs.executeQuery(sql); 
            if(rs.next()){
                return true;    
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;    
    }
    
    private boolean pkValidate(Statement statExs,String table,Map<String,String> keyValue){
        String sql = "SELECT 1 FROM " + table + " T WHERE 1=1 ";
        
        for(Map.Entry<String,String> entry : keyValue.entrySet()){
            sql = sql + "AND T." + entry.getKey() + " = '" + entry.getValue() + "' ";
        }
        
        ResultSet rs;
        try {
            rs = statExs.executeQuery(sql); 
            if(rs.next()){
                return true;    
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;        
    }

}
