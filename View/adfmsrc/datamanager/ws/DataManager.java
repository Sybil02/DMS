package datamanager.ws;

import datamanager.entity.CostCenter;
import datamanager.entity.GlAccount;
import datamanager.entity.HlsBomEntity;

import datamanager.entity.Orginaze;
import datamanager.entity.PositionEntity;
import datamanager.entity.PsEntity;

import datamanager.entity.PsJob;
import datamanager.entity.PsMaster;

import datamanager.entity.PsMilepost;

import datamanager.entity.PsNetwork;

import datamanager.entity.PsOrg;

import datamanager.entity.PsWbs;

import datamanager.entity.PsWbsMaster;

import datamanager.entity.Staff;

import datamanager.entity.StaffEntity;

import datamanager.entity.StaffFp;

import dms.quartz.utils.DBConnUtils;

import java.sql.CallableStatement;
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
     * 人员主数据
     * @param staffList
     * @param msgId
     * @return
     */
    public List<Staff> SyncStaff(List<StaffEntity> staffList,String msgId){
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        List<Staff> result = new ArrayList<Staff>();

        for (StaffEntity staffEntity : staffList) {
            try {
                this.insertStaff(staffEntity.getStaffs(), conn, msgId);
                this.insertStaffFp(staffEntity.getStaffFps(), conn);
                for(int i=0;i<staffEntity.getStaffs().size();i++){
                    staffEntity.getStaffs().get(i).setIfflg("S");
                    result.add(staffEntity.getStaffs().get(i)); 
                }
            } catch (SQLException e) {
                e.printStackTrace();
                for(int i=0;i<staffEntity.getStaffs().size();i++){
                    staffEntity.getStaffs().get(i).setIfflg("E");
                    if (e.getMessage().length() > 50) {
                        staffEntity.getStaffs().get(i).setIfmsg(e.getMessage().substring(0, 49));
                    } else {
                        staffEntity.getStaffs().get(i).setIfmsg(e.getMessage());
                    }
                    result.add(staffEntity.getStaffs().get(i));
                }

            }

        }
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 会计科目
     * @param accList
     * @return
     */
    public List<GlAccount> SyncAccount(List<GlAccount> accList){
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        String sql =
            "INSERT INTO DMS_GL_ACCOUNT (BUKRS, SAKNR, TXT20, KTOKS, XSPEB, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5, " +
            "NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG, MSGID,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_GL_ACCOUNT SET BUKRS=?, SAKNR=?, TXT20=?, KTOKS=?, XSPEB=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, " +
            "NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?, MSGID=? ,INSERT_DATE=SYSDATE " +
            "WHERE NVL(BUKRS,0)=NVL(?,0) AND NVL(SAKNR,0) = NVL(?,0)";
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
        for (GlAccount acc : accList) {
            try {
                Map<String, String> keyValue = new HashMap<String, String>();
                keyValue.put("BUKRS", acc.getBukrs());
                keyValue.put("SAKNR", acc.getSaknr());
                if (!this.pkValidate(statExs, "DMS_GL_ACCOUNT", keyValue)) {
                    stat.setString(1, acc.getBukrs());
                    stat.setString(2, acc.getSaknr());
                    stat.setString(3, acc.getTxt20());
                    stat.setString(4, acc.getKtoks());
                    stat.setString(5, acc.getXspeb());
                    stat.setString(6, acc.getNote1());
                    stat.setString(7, acc.getNote2());
                    stat.setString(8, acc.getNote3());
                    stat.setString(9, acc.getNote4());
                    stat.setString(10, acc.getNote5());
                    stat.setString(11, acc.getNote6());
                    stat.setString(12, acc.getNote7());
                    stat.setString(13, acc.getNote8());
                    stat.setString(14, acc.getNote9());
                    stat.setString(15, acc.getNote10());
                    stat.setString(16, acc.getNote11());
                    stat.setString(17, acc.getNote12());
                    stat.setString(18, acc.getNote13());
                    stat.setString(19, acc.getNote14());
                    stat.setString(20, acc.getNote15());
                    stat.setString(21, acc.getIfflg());
                    stat.setString(22, acc.getIfmsg());
                    stat.setString(23, acc.getMsgid());
                    stat.executeUpdate();
                } else {
                    statUp.setString(1, acc.getBukrs());
                    statUp.setString(2, acc.getSaknr());
                    statUp.setString(3, acc.getTxt20());
                    statUp.setString(4, acc.getKtoks());
                    statUp.setString(5, acc.getXspeb());
                    statUp.setString(6, acc.getNote1());
                    statUp.setString(7, acc.getNote2());
                    statUp.setString(8, acc.getNote3());
                    statUp.setString(9, acc.getNote4());
                    statUp.setString(10, acc.getNote5());
                    statUp.setString(11, acc.getNote6());
                    statUp.setString(12, acc.getNote7());
                    statUp.setString(13, acc.getNote8());
                    statUp.setString(14, acc.getNote9());
                    statUp.setString(15, acc.getNote10());
                    statUp.setString(16, acc.getNote11());
                    statUp.setString(17, acc.getNote12());
                    statUp.setString(18, acc.getNote13());
                    statUp.setString(19, acc.getNote14());
                    statUp.setString(20, acc.getNote15());
                    statUp.setString(21, acc.getIfflg());
                    statUp.setString(22, acc.getIfmsg());
                    statUp.setString(23, acc.getMsgid());
                    statUp.setString(24, acc.getBukrs());
                    statUp.setString(25, acc.getSaknr());
                    statUp.executeUpdate();
                }
                conn.commit();
                acc.setIfflg("S");
            } catch (SQLException e) {
                e.printStackTrace();
                acc.setIfflg("E");
                if (e.getMessage().length() > 50) {
                    acc.setIfmsg(e.getMessage().substring(0, 49));
                } else {
                    acc.setIfmsg(e.getMessage());
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
        return accList;
    }

    /**
     * 成本中心主数据
     * @param ccList
     * @return
     */
    public List<CostCenter> SyncCostCenter(List<CostCenter> ccList){
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        String sql =
            "INSERT INTO DMS_COST_CENTER (KOSTL, DATBI, DATAB, BKZKP, ZZBMBM, ZZBMMC, BUKRS, ABTEI, KHINR, KTEXT, ZZTXMC, ZZYWXMC, ZZHYXMC, NOTE1, " +
            "NOTE2, NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG, MSGID,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_COST_CENTER SET KOSTL=?, DATBI=?, DATAB=?, BKZKP=?, ZZBMBM=?, ZZBMMC=?, BUKRS=?, ABTEI=?, KHINR=?, KTEXT=?, ZZTXMC=?, ZZYWXMC=?, ZZHYXMC=?, NOTE1=?, " +
            "NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?, MSGID=? ,INSERT_DATE=SYSDATE " +
            "WHERE NVL(KOSTL,0)=NVL(?,0) AND NVL(DATBI,0) = NVL(?,0) AND NVL(ABTEI,0) = NVL(?,0)";
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
        for (CostCenter cost : ccList) {
            try {
                Map<String, String> keyValue = new HashMap<String, String>();
                keyValue.put("KOSTL", cost.getKostl());
                keyValue.put("DATBI", cost.getDatbi());
                keyValue.put("ABTEI", cost.getAbtei());
                if (!this.pkValidate(statExs, "DMS_COST_CENTER", keyValue)) {
                    stat.setString(1, cost.getKostl());
                    stat.setString(2, cost.getDatbi());
                    stat.setString(3, cost.getDatab());
                    stat.setString(4, cost.getBkzkp());
                    stat.setString(5, cost.getZzbmbm());
                    stat.setString(6, cost.getZzbmmc());
                    stat.setString(7, cost.getBukrs());
                    stat.setString(8, cost.getAbtei());
                    stat.setString(9, cost.getKhinr());
                    stat.setString(10, cost.getKtext());
                    stat.setString(11, cost.getZztxmc());
                    stat.setString(12, cost.getZzywxmc());
                    stat.setString(13, cost.getZzhyxmc());
                    stat.setString(14, cost.getNote1());
                    stat.setString(15, cost.getNote2());
                    stat.setString(16, cost.getNote3());
                    stat.setString(17, cost.getNote4());
                    stat.setString(18, cost.getNote5());
                    stat.setString(19, cost.getNote6());
                    stat.setString(20, cost.getNote7());
                    stat.setString(21, cost.getNote8());
                    stat.setString(22, cost.getNote9());
                    stat.setString(23, cost.getNote10());
                    stat.setString(24, cost.getNote11());
                    stat.setString(25, cost.getNote12());
                    stat.setString(26, cost.getNote13());
                    stat.setString(27, cost.getNote14());
                    stat.setString(28, cost.getNote15());
                    stat.setString(29, cost.getIfflg());
                    stat.setString(30, cost.getIfmsg());
                    stat.setString(31, cost.getMsgid());
                    stat.executeUpdate();
                } else {
                    statUp.setString(1, cost.getKostl());
                    statUp.setString(2, cost.getDatbi());
                    statUp.setString(3, cost.getDatab());
                    statUp.setString(4, cost.getBkzkp());
                    statUp.setString(5, cost.getZzbmbm());
                    statUp.setString(6, cost.getZzbmmc());
                    statUp.setString(7, cost.getBukrs());
                    statUp.setString(8, cost.getAbtei());
                    statUp.setString(9, cost.getKhinr());
                    statUp.setString(10, cost.getKtext());
                    statUp.setString(11, cost.getZztxmc());
                    statUp.setString(12, cost.getZzywxmc());
                    statUp.setString(13, cost.getZzhyxmc());
                    statUp.setString(14, cost.getNote1());
                    statUp.setString(15, cost.getNote2());
                    statUp.setString(16, cost.getNote3());
                    statUp.setString(17, cost.getNote4());
                    statUp.setString(18, cost.getNote5());
                    statUp.setString(19, cost.getNote6());
                    statUp.setString(20, cost.getNote7());
                    statUp.setString(21, cost.getNote8());
                    statUp.setString(22, cost.getNote9());
                    statUp.setString(23, cost.getNote10());
                    statUp.setString(24, cost.getNote11());
                    statUp.setString(25, cost.getNote12());
                    statUp.setString(26, cost.getNote13());
                    statUp.setString(27, cost.getNote14());
                    statUp.setString(28, cost.getNote15());
                    statUp.setString(29, cost.getIfflg());
                    statUp.setString(30, cost.getIfmsg());
                    statUp.setString(31, cost.getMsgid());
                    statUp.setString(32, cost.getKostl());
                    statUp.setString(33, cost.getDatbi());
                    statUp.setString(34, cost.getAbtei());
                    statUp.executeUpdate();
                }
                conn.commit();
                cost.setIfflg("S");
            } catch (SQLException e) {
                e.printStackTrace();
                cost.setIfflg("E");
                if (e.getMessage().length() > 50) {
                    cost.setIfmsg(e.getMessage().substring(0, 49));
                } else {
                    cost.setIfmsg(e.getMessage());
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
        return ccList;
    }

    /**
     * 部门主数据
     * @param orgs
     * @return
     */
    public List<Orginaze> SyncOrg(List<Orginaze> orgs){
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        String sql =
            "INSERT INTO DMS_HR_ORG (ZJGDM, ZBMDM, ZBMMS, ZZRZX, ZSJBM, ZZGGW, ZQYBZ, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5, " +
            "NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG, MSGID,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_HR_ORG SET ZJGDM=?, ZBMDM=?, ZBMMS=?, ZZRZX=?, ZSJBM=?, ZZGGW=?, ZQYBZ=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, " +
            "NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?, MSGID=? ,INSERT_DATE=SYSDATE " +
            "WHERE NVL(ZJGDM,0)=NVL(?,0) AND NVL(ZBMDM,0)=NVL(?,0) AND NVL(ZZRZX,0) =NVL(?,0) AND NVL(ZSJBM,0) =NVL(?,0) AND NVL(ZZGGW,0)=NVL(?,0)";
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
        for (Orginaze org : orgs) {
            try {
                Map<String, String> keyValue = new HashMap<String, String>();
                keyValue.put("ZJGDM", org.getZjgdm());
                keyValue.put("ZBMDM", org.getZbmdm());
                keyValue.put("ZZRZX", org.getZzrzx());
                keyValue.put("ZSJBM", org.getZsjbm());
                keyValue.put("ZZGGW", org.getZzggw());
                if (!this.pkValidate(statExs, "DMS_HR_ORG", keyValue)) {
                    stat.setString(1, org.getZjgdm());
                    stat.setString(2, org.getZbmdm());
                    stat.setString(3, org.getZbmms());
                    stat.setString(4, org.getZzrzx());
                    stat.setString(5, org.getZsjbm());
                    stat.setString(6, org.getZzggw());
                    stat.setString(7, org.getZqybz());
                    stat.setString(8, org.getNote1());
                    stat.setString(9, org.getNote2());
                    stat.setString(10, org.getNote3());
                    stat.setString(11, org.getNote4());
                    stat.setString(12, org.getNote5());
                    stat.setString(13, org.getNote6());
                    stat.setString(14, org.getNote7());
                    stat.setString(15, org.getNote8());
                    stat.setString(16, org.getNote9());
                    stat.setString(17, org.getNote10());
                    stat.setString(18, org.getNote11());
                    stat.setString(19, org.getNote12());
                    stat.setString(20, org.getNote13());
                    stat.setString(21, org.getNote14());
                    stat.setString(22, org.getNote15());
                    stat.setString(23, org.getIfflg());
                    stat.setString(24, org.getIfmsg());
                    stat.setString(25, org.getMsgid());
                    stat.executeUpdate();
                } else {
                    statUp.setString(1, org.getZjgdm());
                    statUp.setString(2, org.getZbmdm());
                    statUp.setString(3, org.getZbmms());
                    statUp.setString(4, org.getZzrzx());
                    statUp.setString(5, org.getZsjbm());
                    statUp.setString(6, org.getZzggw());
                    statUp.setString(7, org.getZqybz());
                    statUp.setString(8, org.getNote1());
                    statUp.setString(9, org.getNote2());
                    statUp.setString(10, org.getNote3());
                    statUp.setString(11, org.getNote4());
                    statUp.setString(12, org.getNote5());
                    statUp.setString(13, org.getNote6());
                    statUp.setString(14, org.getNote7());
                    statUp.setString(15, org.getNote8());
                    statUp.setString(16, org.getNote9());
                    statUp.setString(17, org.getNote10());
                    statUp.setString(18, org.getNote11());
                    statUp.setString(19, org.getNote12());
                    statUp.setString(20, org.getNote13());
                    statUp.setString(21, org.getNote14());
                    statUp.setString(22, org.getNote15());
                    statUp.setString(23, org.getIfflg());
                    statUp.setString(24, org.getIfmsg());
                    statUp.setString(25, org.getMsgid());
                    statUp.setString(26, org.getZjgdm());
                    statUp.setString(27, org.getZbmdm());
                    statUp.setString(28, org.getZzrzx());
                    statUp.setString(29, org.getZsjbm());
                    statUp.setString(30, org.getZzggw());
                    statUp.executeUpdate();
                }
                conn.commit();
                org.setIfflg("S");
            } catch (SQLException e) {
                e.printStackTrace();
                org.setIfflg("E");
                if (e.getMessage().length() > 50) {
                    org.setIfmsg(e.getMessage().substring(0, 49));
                } else {
                    org.setIfmsg(e.getMessage());
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
        return orgs;
    }

    /**
     * 岗位主数据
     * @param positionList
     * @return
     */
    public List<PositionEntity> SyncPosition(List<PositionEntity> positionList) {
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        String sql =
            "INSERT INTO DMS_HR_POSITION (ZJGDM, ZBMDM, ZGWDM, ZGWMS, ZSJGW, ZQYBZ, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5," +
            "NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG, MSGID, NEW_GW,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_HR_POSITION SET ZJGDM=?, ZBMDM=?, ZGWDM=?, ZGWMS=?, ZSJGW=?, ZQYBZ=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, " +
            " NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?, MSGID=? , NEW_GW=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(ZJGDM,0)=NVL(?,0) AND NVL(ZBMDM,0)=NVL(?,0) AND NVL(ZGWDM,0)=NVL(?,0) AND NVL(ZGWMS,0)=NVL(?,0) AND NVL(ZSJGW,0)=NVL(?,0) ";
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
                Map<String, String> keyValue = new HashMap<String, String>();
                keyValue.put("ZJGDM", position.getZjgdm());
                keyValue.put("ZBMDM", position.getZbmdm());
                keyValue.put("ZGWDM", position.getZgwdm());
                keyValue.put("ZGWMS", position.getZgwms());
                keyValue.put("ZSJGW", position.getZsjgw());
                if (!this.pkValidate(statExs, "DMS_HR_POSITION", keyValue)) {
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
                } else {
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

    public List<PsMaster> SyncPs(List<PsEntity> psEntityList, String msgId) {
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        List<PsMaster> result = new ArrayList<PsMaster>();

        for (PsEntity psEntity : psEntityList) {
            try {
                this.insertPsJob(psEntity.getPsJob(), conn);
                this.insertPsMilepost(psEntity.getPsMilepost(),conn);
                this.insertPsNetwork(psEntity.getPsNetwork(),conn);
                this.insertPsOrg(psEntity.getPsOrg(), conn);
                this.insertPsWbs(psEntity.getPsWbs(),conn);
                this.insertPsWbsMaster(psEntity.getPsWbsMaster(),conn);
                this.insertPsMaster(psEntity.getPsMaster(), conn, msgId);
                for(int i=0;i<psEntity.getPsMaster().size();i++){
                    psEntity.getPsMaster().get(i).setIfflg("S");
                    result.add(psEntity.getPsMaster().get(i));
                }
                CallableStatement call = conn.prepareCall("{call delect_repeat_masterdata.calc_ps_data}");
                call.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                for(int i=0;i<psEntity.getPsMaster().size();i++){
                    psEntity.getPsMaster().get(i).setIfflg("E");
                    if (e.getMessage().length() > 50) {
                        psEntity.getPsMaster().get(i).setIfmsg(e.getMessage().substring(0, 49));
                    } else {
                        psEntity.getPsMaster().get(i).setIfmsg(e.getMessage());
                    }
                    result.add(psEntity.getPsMaster().get(i));
                }
            }

        }
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
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
            "LVORM, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG, MSGID,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_HLS_BOM SET MATNR=?, MAKTX=?, WERKS=?, MTART=?, MEINS=?, MTBEZ=?, DISPO=?, ZGOOD=?, STPRS=?, PEINH=?, ZPLP2=?,LVORM=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, NOTE6=?," +
            " NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?, MSGID=?,INSERT_DATE=SYSDATE WHERE NVL(MATNR,0)=NVL(?,0) AND NVL(WERKS,0)=NVL(?,0)";
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
                if (!this.bomIsExists(statExs, bom)) {
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
                } else {
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
    
    private void insertStaffFp(List<StaffFp> staffFps,Connection conn) throws SQLException {
        if(staffFps == null || staffFps.size() == 0){
            return;    
        }
        
        String sql =
            "INSERT INTO DMS_HR_STAFF_FP (BUKRS, PERNR, PLANS, KST01, STELL, ZLEVEL, SOBID, BTRTL, BTEXT, ZQYBZ, NOTE1, " +
            "NOTE2, NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_HR_STAFF_FP SET BUKRS=?, PERNR=?, PLANS=?, KST01=?, STELL=?, ZLEVEL=?, SOBID=?, BTRTL=?, BTEXT=?, ZQYBZ=?, NOTE1=?, " +
            "NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(PERNR,0)=NVL(?,0) AND NVL(PLANS,0)=NVL(?,0)";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;

        stat = conn.prepareStatement(sql);
        statUp = conn.prepareStatement(sqlUp);
        statExs = conn.createStatement();

        for (StaffFp af : staffFps) {

            Map<String, String> keyValue = new HashMap<String, String>();
            keyValue.put("PERNR", af.getPernr());
            keyValue.put("PLANS", af.getPlans());
            if (!this.pkValidate(statExs, "DMS_HR_STAFF_FP", keyValue)) {
                stat.setString(1, af.getBukrs());
                stat.setString(2, af.getPernr());
                stat.setString(3, af.getPlans());
                stat.setString(4, af.getKst01());
                stat.setString(5, af.getStell());
                stat.setString(6, af.getZlevel());
                stat.setString(7, af.getSobid());
                stat.setString(8, af.getBtrtl());
                stat.setString(9, af.getBtext());
                stat.setString(10, af.getZqybz());
                stat.setString(11, af.getNote1());
                stat.setString(12, af.getNote2());
                stat.setString(13, af.getNote3());
                stat.setString(14, af.getNote4());
                stat.setString(15, af.getNote5());
                stat.setString(16, af.getNote6());
                stat.setString(17, af.getNote7());
                stat.setString(18, af.getNote8());
                stat.setString(19, af.getNote9());
                stat.setString(20, af.getNote10());
                stat.setString(21, af.getNote11());
                stat.setString(22, af.getNote12());
                stat.setString(23, af.getNote13());
                stat.setString(24, af.getNote14());
                stat.setString(25, af.getNote15());
                stat.setString(26, af.getIfflg());
                stat.setString(27, af.getIfmsg());
                stat.executeUpdate();
            } else {
                statUp.setString(1, af.getBukrs());
                statUp.setString(2, af.getPernr());
                statUp.setString(3, af.getPlans());
                statUp.setString(4, af.getKst01());
                statUp.setString(5, af.getStell());
                statUp.setString(6, af.getZlevel());
                statUp.setString(7, af.getSobid());
                statUp.setString(8, af.getBtrtl());
                statUp.setString(9, af.getBtext());
                statUp.setString(10, af.getZqybz());
                statUp.setString(11, af.getNote1());
                statUp.setString(12, af.getNote2());
                statUp.setString(13, af.getNote3());
                statUp.setString(14, af.getNote4());
                statUp.setString(15, af.getNote5());
                statUp.setString(16, af.getNote6());
                statUp.setString(17, af.getNote7());
                statUp.setString(18, af.getNote8());
                statUp.setString(19, af.getNote9());
                statUp.setString(20, af.getNote10());
                statUp.setString(21, af.getNote11());
                statUp.setString(22, af.getNote12());
                statUp.setString(23, af.getNote13());
                statUp.setString(24, af.getNote14());
                statUp.setString(25, af.getNote15());
                statUp.setString(26, af.getIfflg());
                statUp.setString(27, af.getIfmsg());
                statUp.setString(28, af.getPernr());
                statUp.setString(29, af.getPlans());
                statUp.executeUpdate();
            }
            conn.commit();
        }
        stat.close();
        statExs.close();
        statUp.close();
    }
    
    private void insertStaff(List<Staff> staffs,Connection conn,String msgId) throws SQLException {
        if(staffs == null || staffs.size() == 0){
            return;    
        }
        
        String sql =
            "INSERT INTO DMS_HR_STAFF (BUKRS, PERNR, SNAME, ZMAIL, ZMOBIL, ZPHONE, ICNUM, STAT2, NOTE1, NOTE2, NOTE3, " +
            "NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG, MSGID,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_HR_STAFF SET BUKRS=?, PERNR=?, SNAME=?, ZMAIL=?, ZMOBIL=?, ZPHONE=?, ICNUM=?, STAT2=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, " +
            "NOTE5=?, NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?, MSGID=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(PERNR,0)=NVL(?,0)";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;

        stat = conn.prepareStatement(sql);
        statUp = conn.prepareStatement(sqlUp);
        statExs = conn.createStatement();

        for (Staff af : staffs) {

            Map<String, String> keyValue = new HashMap<String, String>();
            keyValue.put("PERNR", af.getPernr());
            if (!this.pkValidate(statExs, "DMS_HR_STAFF", keyValue)) {
                stat.setString(1, af.getBukrs());
                stat.setString(2, af.getPernr());
                stat.setString(3, af.getSname());
                stat.setString(4, af.getZmail());
                stat.setString(5, af.getZmobil());
                stat.setString(6, af.getZphone());
                stat.setString(7, af.getIcnum());
                stat.setString(8, af.getStat2());
                stat.setString(9, af.getNote1());
                stat.setString(10, af.getNote2());
                stat.setString(11, af.getNote3());
                stat.setString(12, af.getNote4());
                stat.setString(13, af.getNote5());
                stat.setString(14, af.getNote6());
                stat.setString(15, af.getNote7());
                stat.setString(16, af.getNote8());
                stat.setString(17, af.getNote9());
                stat.setString(18, af.getNote10());
                stat.setString(19, af.getNote11());
                stat.setString(20, af.getNote12());
                stat.setString(21, af.getNote13());
                stat.setString(22, af.getNote14());
                stat.setString(23, af.getNote15());
                stat.setString(24, af.getIfflg());
                stat.setString(25, af.getIfmsg());
                stat.setString(26, msgId);
                stat.executeUpdate();
            } else {
                statUp.setString(1, af.getBukrs());
                statUp.setString(2, af.getPernr());
                statUp.setString(3, af.getSname());
                statUp.setString(4, af.getZmail());
                statUp.setString(5, af.getZmobil());
                statUp.setString(6, af.getZphone());
                statUp.setString(7, af.getIcnum());
                statUp.setString(8, af.getStat2());
                statUp.setString(9, af.getNote1());
                statUp.setString(10, af.getNote2());
                statUp.setString(11, af.getNote3());
                statUp.setString(12, af.getNote4());
                statUp.setString(13, af.getNote5());
                statUp.setString(14, af.getNote6());
                statUp.setString(15, af.getNote7());
                statUp.setString(16, af.getNote8());
                statUp.setString(17, af.getNote9());
                statUp.setString(18, af.getNote10());
                statUp.setString(19, af.getNote11());
                statUp.setString(20, af.getNote12());
                statUp.setString(21, af.getNote13());
                statUp.setString(22, af.getNote14());
                statUp.setString(23, af.getNote15());
                statUp.setString(24, af.getIfflg());
                statUp.setString(25, af.getIfmsg());
                statUp.setString(26, msgId);
                statUp.setString(27, af.getPernr());
                statUp.executeUpdate();
            }
            conn.commit();
        }
        stat.close();
        statExs.close();
        statUp.close();
    }

    private void insertPsJob(List<PsJob> psJob,
                             Connection conn) throws SQLException {
        if(psJob == null || psJob.size() == 0){
            return;    
        }
        
        String sql =
            "INSERT INTO DMS_PS_JOBS (MANDT, AUFPL, APLZL, SUMNR, ACTVT, PSPHI, LTXA1, OBJNR, AUFNR, PROJN, BUKRS, SAKTO, WAERS, FSAVD, SSEDD, ERNAM, " +
            "ERDAT, AENAM, AEDAT, VERNR, VERNA, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_PS_JOBS SET MANDT=?, AUFPL=?, APLZL=?, SUMNR=?, ACTVT=?, PSPHI=?, LTXA1=?, OBJNR=?, AUFNR=?, PROJN=?, BUKRS=?, SAKTO=?, WAERS=?, FSAVD=?, SSEDD=?, ERNAM=?,  " +
            " ERDAT=?, AENAM=?, AEDAT=?, VERNR=?, VERNA=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(AUFPL,0)=NVL(?,0) AND NVL(APLZL,0)=NVL(?,0) AND NVL(PSPHI,0)=NVL(?,0) AND NVL(PROJN,0)=NVL(?,0)";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;

        stat = conn.prepareStatement(sql);
        statUp = conn.prepareStatement(sqlUp);
        statExs = conn.createStatement();

        for (PsJob job : psJob) {

            Map<String, String> keyValue = new HashMap<String, String>();
            keyValue.put("AUFPL", job.getAufpl());
            keyValue.put("APLZL", job.getAplzl());
            keyValue.put("PSPHI", job.getPsphi());
            keyValue.put("PROJN", job.getProjn());
            if (true) {
                stat.setString(1, job.getMandt());
                stat.setString(2, job.getAufpl());
                stat.setString(3, job.getAplzl());
                stat.setString(4, job.getSumnr());
                stat.setString(5, job.getActvt());
                stat.setString(6, job.getPsphi());
                stat.setString(7, job.getLtxa1());
                stat.setString(8, job.getObjnr());
                stat.setString(9, job.getAufnr());
                stat.setString(10, job.getProjn());
                stat.setString(11, job.getBukrs());
                stat.setString(12, job.getSakto());
                stat.setString(13, job.getWaers());
                stat.setString(14, job.getFsavd());
                stat.setString(15, job.getSsedd());
                stat.setString(16, job.getErnam());
                stat.setString(17, job.getErdat());
                stat.setString(18, job.getAenam());
                stat.setString(19, job.getAedat());
                stat.setString(20, job.getVernr());
                stat.setString(21, job.getVerna());
                stat.setString(22, job.getNote1());
                stat.setString(23, job.getNote2());
                stat.setString(24, job.getNote3());
                stat.setString(25, job.getNote4());
                stat.setString(26, job.getNote5());
                stat.setString(27, job.getNote6());
                stat.setString(28, job.getNote7());
                stat.setString(29, job.getNote8());
                stat.setString(30, job.getNote9());
                stat.setString(31, job.getNote10());
                stat.setString(32, job.getNote11());
                stat.setString(33, job.getNote12());
                stat.setString(34, job.getNote13());
                stat.setString(35, job.getNote14());
                stat.setString(36, job.getNote15());
                stat.setString(37, job.getIfflg());
                stat.setString(38, job.getIfmsg());
                stat.executeUpdate();
            } else {
//                statUp.setString(1, job.getMandt());
//                statUp.setString(2, job.getAufpl());
//                statUp.setString(3, job.getAplzl());
//                statUp.setString(4, job.getSumnr());
//                statUp.setString(5, job.getActvt());
//                statUp.setString(6, job.getPsphi());
//                statUp.setString(7, job.getLtxa1());
//                statUp.setString(8, job.getObjnr());
//                statUp.setString(9, job.getAufnr());
//                statUp.setString(10, job.getProjn());
//                statUp.setString(11, job.getBukrs());
//                statUp.setString(12, job.getSakto());
//                statUp.setString(13, job.getWaers());
//                statUp.setString(14, job.getFsavd());
//                statUp.setString(15, job.getSsedd());
//                statUp.setString(16, job.getErnam());
//                statUp.setString(17, job.getErdat());
//                statUp.setString(18, job.getAenam());
//                statUp.setString(19, job.getAedat());
//                statUp.setString(20, job.getVernr());
//                statUp.setString(21, job.getVerna());
//                statUp.setString(22, job.getNote1());
//                statUp.setString(23, job.getNote2());
//                statUp.setString(24, job.getNote3());
//                statUp.setString(25, job.getNote4());
//                statUp.setString(26, job.getNote5());
//                statUp.setString(27, job.getNote6());
//                statUp.setString(28, job.getNote7());
//                statUp.setString(29, job.getNote8());
//                statUp.setString(30, job.getNote9());
//                statUp.setString(31, job.getNote10());
//                statUp.setString(32, job.getNote11());
//                statUp.setString(33, job.getNote12());
//                statUp.setString(34, job.getNote13());
//                statUp.setString(35, job.getNote14());
//                statUp.setString(36, job.getNote15());
//                statUp.setString(37, job.getIfflg());
//                statUp.setString(38, job.getIfmsg());
//                statUp.setString(39, job.getAufpl());
//                statUp.setString(40, job.getAplzl());
//                statUp.setString(41, job.getPsphi());
//                statUp.setString(42, job.getProjn());
//                statUp.executeUpdate();
            }
            conn.commit();
        }
        stat.close();
        statExs.close();
        statUp.close();
    }

    private void insertPsMaster(List<PsMaster> psMaster, Connection conn, String msgId) throws SQLException {
        if(psMaster == null || psMaster.size() == 0){
            return;    
        }
        
        String sql =
            "INSERT INTO DMS_PS_MASTER (MANDT, PSPNR, POST1, OBJNR, ERNAM, ERDAT, AENAM, AEDAT, VBUKR, PWHIE, PLFAZ, PLSEZ, PROFL, ZPS005, ZPS007, ZSD022," +
            "ZSD023, ZSD029, PROFI_TXT, ZZFXXMLX, STAT, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG,MSGID,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_PS_MASTER SET MANDT=?, PSPNR=?, POST1=?, OBJNR=?, ERNAM=?, ERDAT=?, AENAM=?, AEDAT=?, VBUKR=?, PWHIE=?, PLFAZ=?, PLSEZ=?, PROFL=?, ZPS005=?, ZPS007=?, ZSD022=?," +
            " ZSD023=?, ZSD029=?, PROFI_TXT=?, ZZFXXMLX=?, STAT=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?, MSGID=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(PSPNR,0)=NVL(?,0) AND NVL(VBUKR,0)=NVL(?,0)";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;

        stat = conn.prepareStatement(sql);
        statUp = conn.prepareStatement(sqlUp);
        statExs = conn.createStatement();

        for (PsMaster master : psMaster) {

            Map<String, String> keyValue = new HashMap<String, String>();
            keyValue.put("PSPNR", master.getPspnr());
            keyValue.put("VBUKR", master.getVbukr());
            if (true) {
                stat.setString(1, master.getMandt());
                stat.setString(2, master.getPspnr());
                stat.setString(3, master.getPost1());
                stat.setString(4, master.getObjnr());
                stat.setString(5, master.getErnam());
                stat.setString(6, master.getErdat());
                stat.setString(7, master.getAenam());
                stat.setString(8, master.getAedat());
                stat.setString(9, master.getVbukr());
                stat.setString(10, master.getPwhie());
                stat.setString(11, master.getPlfaz());
                stat.setString(12, master.getPlsez());
                stat.setString(13, master.getProfl());
                stat.setString(14, master.getZps005());
                stat.setString(15, master.getZps007());
                stat.setString(16, master.getZsd022());
                stat.setString(17, master.getZsd023());
                stat.setString(18, master.getZsd029());
                stat.setString(19, master.getProfi_txt());
                stat.setString(20, master.getZzfxxmlx());
                stat.setString(21, master.getStat());
                stat.setString(22, master.getNote1());
                stat.setString(23, master.getNote2());
                stat.setString(24, master.getNote3());
                stat.setString(25, master.getNote4());
                stat.setString(26, master.getNote5());
                stat.setString(27, master.getNote6());
                stat.setString(28, master.getNote7());
                stat.setString(29, master.getNote8());
                stat.setString(30, master.getNote9());
                stat.setString(31, master.getNote10());
                stat.setString(32, master.getNote11());
                stat.setString(33, master.getNote12());
                stat.setString(34, master.getNote13());
                stat.setString(35, master.getNote14());
                stat.setString(36, master.getNote15());
                stat.setString(37, master.getIfflg());
                stat.setString(38, master.getIfmsg());
                stat.setString(39, msgId);
                stat.executeUpdate();
            } else {
//                statUp.setString(1, master.getMandt());
//                statUp.setString(2, master.getPspnr());
//                statUp.setString(3, master.getPost1());
//                statUp.setString(4, master.getObjnr());
//                statUp.setString(5, master.getErnam());
//                statUp.setString(6, master.getErdat());
//                statUp.setString(7, master.getAenam());
//                statUp.setString(8, master.getAedat());
//                statUp.setString(9, master.getVbukr());
//                statUp.setString(10, master.getPwhie());
//                statUp.setString(11, master.getPlfaz());
//                statUp.setString(12, master.getPlsez());
//                statUp.setString(13, master.getProfl());
//                statUp.setString(14, master.getZps005());
//                statUp.setString(15, master.getZps007());
//                statUp.setString(16, master.getZsd022());
//                statUp.setString(17, master.getZsd023());
//                statUp.setString(18, master.getZsd029());
//                statUp.setString(19, master.getProfi_txt());
//                statUp.setString(20, master.getZzfxxmlx());
//                statUp.setString(21, master.getStat());
//                statUp.setString(22, master.getNote1());
//                statUp.setString(23, master.getNote2());
//                statUp.setString(24, master.getNote3());
//                statUp.setString(25, master.getNote4());
//                statUp.setString(26, master.getNote5());
//                statUp.setString(27, master.getNote6());
//                statUp.setString(28, master.getNote7());
//                statUp.setString(29, master.getNote8());
//                statUp.setString(30, master.getNote9());
//                statUp.setString(31, master.getNote10());
//                statUp.setString(32, master.getNote11());
//                statUp.setString(33, master.getNote12());
//                statUp.setString(34, master.getNote13());
//                statUp.setString(35, master.getNote14());
//                statUp.setString(36, master.getNote15());
//                statUp.setString(37, master.getIfflg());
//                statUp.setString(38, master.getIfmsg());
//                statUp.setString(39, msgId);
//                statUp.setString(40, master.getPspnr());
//                statUp.setString(41, master.getVbukr());
//                statUp.executeUpdate();
            }
            conn.commit();
        }
        stat.close();
        statExs.close();
        statUp.close();
    }

    private void insertPsMilepost(List<PsMilepost> psMilepost,
                                  Connection conn) throws SQLException {
        if(psMilepost == null || psMilepost.size() == 0){
            return;    
        }
        
        String sql =
            "INSERT INTO DMS_PS_MILEPOST (MANDT, ZAEHL, KTEXT, VORNR, AUFNR, PSPNR, PSPHI, BUKRS, TEDAT, LST_ACTDT, FPROZ, NOTE1, NOTE2, " +
            "NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_PS_MILEPOST SET MANDT=?, ZAEHL=?, KTEXT=?, VORNR=?, AUFNR=?, PSPNR=?, PSPHI=?, BUKRS=?, TEDAT=?, LST_ACTDT=?, FPROZ=?, NOTE1=?, NOTE2=?, " +
            " NOTE3=?, NOTE4=?, NOTE5=?, NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(ZAEHL,0)=NVL(?,0) AND NVL(PSPNR,0)=NVL(?,0) AND NVL(PSPHI,0)=NVL(?,0)";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;

        stat = conn.prepareStatement(sql);
        statUp = conn.prepareStatement(sqlUp);
        statExs = conn.createStatement();

        for (PsMilepost mp : psMilepost) {

            Map<String, String> keyValue = new HashMap<String, String>();
            keyValue.put("ZAEHL", mp.getZaehl());
            keyValue.put("PSPNR", mp.getPspnr());
            keyValue.put("PSPHI", mp.getPsphi());
            if (true) {
                stat.setString(1, mp.getMandt());
                stat.setString(2, mp.getZaehl());
                stat.setString(3, mp.getKtext());
                stat.setString(4, mp.getVornr());
                stat.setString(5, mp.getAufnr());
                stat.setString(6, mp.getPspnr());
                stat.setString(7, mp.getPsphi());
                stat.setString(8, mp.getBukrs());
                stat.setString(9, mp.getTedat());
                stat.setString(10, mp.getLst_actdt());
                stat.setString(11, mp.getFproz());
                stat.setString(12, mp.getNote1());
                stat.setString(13, mp.getNote2());
                stat.setString(14, mp.getNote3());
                stat.setString(15, mp.getNote4());
                stat.setString(16, mp.getNote5());
                stat.setString(17, mp.getNote6());
                stat.setString(18, mp.getNote7());
                stat.setString(19, mp.getNote8());
                stat.setString(20, mp.getNote9());
                stat.setString(21, mp.getNote10());
                stat.setString(22, mp.getNote11());
                stat.setString(23, mp.getNote12());
                stat.setString(24, mp.getNote13());
                stat.setString(25, mp.getNote14());
                stat.setString(26, mp.getNote15());
                stat.setString(27, mp.getIfflg());
                stat.setString(28, mp.getIfmsg());
                stat.executeUpdate();
            } else {
//                statUp.setString(1, mp.getMandt());
//                statUp.setString(2, mp.getZaehl());
//                statUp.setString(3, mp.getKtext());
//                statUp.setString(4, mp.getVornr());
//                statUp.setString(5, mp.getAufnr());
//                statUp.setString(6, mp.getPspnr());
//                statUp.setString(7, mp.getPsphi());
//                statUp.setString(8, mp.getBukrs());
//                statUp.setString(9, mp.getTedat());
//                statUp.setString(10, mp.getLst_actdt());
//                statUp.setString(11, mp.getFproz());
//                statUp.setString(12, mp.getNote1());
//                statUp.setString(13, mp.getNote2());
//                statUp.setString(14, mp.getNote3());
//                statUp.setString(15, mp.getNote4());
//                statUp.setString(16, mp.getNote5());
//                statUp.setString(17, mp.getNote6());
//                statUp.setString(18, mp.getNote7());
//                statUp.setString(19, mp.getNote8());
//                statUp.setString(20, mp.getNote9());
//                statUp.setString(21, mp.getNote10());
//                statUp.setString(22, mp.getNote11());
//                statUp.setString(23, mp.getNote12());
//                statUp.setString(24, mp.getNote13());
//                statUp.setString(25, mp.getNote14());
//                statUp.setString(26, mp.getNote15());
//                statUp.setString(27, mp.getIfflg());
//                statUp.setString(28, mp.getIfmsg());
//                statUp.setString(29, mp.getZaehl());
//                statUp.setString(30, mp.getPspnr());
//                statUp.setString(31, mp.getPsphi());
//                statUp.executeUpdate();
            }
            conn.commit();
        }
        stat.close();
        statExs.close();
        statUp.close();
    }

    private void insertPsNetwork(List<PsNetwork> psNetwork ,Connection conn) throws SQLException {
        if(psNetwork == null || psNetwork.size() == 0){
            return;    
        }
        
        String sql =
            "INSERT INTO DMS_PS_NETWORK (MANDT, AUFNR, KTEXT, OBJNR, PSPEL, PSPHI, BUKRS, GSTRS, GLTRS, ERDAT, ERNAM, NOTE1, NOTE2, NOTE3, NOTE4, " +
            "NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_PS_NETWORK SET MANDT=?, AUFNR=?, KTEXT=?, OBJNR=?, PSPEL=?, PSPHI=?, BUKRS=?, GSTRS=?, GLTRS=?, ERDAT=?, ERNAM=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, " +
            " NOTE5=?, NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(AUFNR,0)=NVL(?,0) AND NVL(PSPEL,0)=NVL(?,0) AND NVL(PSPHI,0)=NVL(?,0)";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;

        stat = conn.prepareStatement(sql);
        statUp = conn.prepareStatement(sqlUp);
        statExs = conn.createStatement();

        for (PsNetwork nw : psNetwork) {

            Map<String, String> keyValue = new HashMap<String, String>();
            keyValue.put("AUFNR", nw.getAufnr());
            keyValue.put("PSPEL", nw.getPspel());
            keyValue.put("PSPHI", nw.getPsphi());
            if (true) {
                stat.setString(1, nw.getMandt());
                stat.setString(2, nw.getAufnr());
                stat.setString(3, nw.getKtext());
                stat.setString(4, nw.getObjnr());
                stat.setString(5, nw.getPspel());
                stat.setString(6, nw.getPsphi());
                stat.setString(7, nw.getBukrs());
                stat.setString(8, nw.getGstrs());
                stat.setString(9, nw.getGltrs());
                stat.setString(10, nw.getErdat());
                stat.setString(11, nw.getErnam());
                stat.setString(12, nw.getNote1());
                stat.setString(13, nw.getNote2());
                stat.setString(14, nw.getNote3());
                stat.setString(15, nw.getNote4());
                stat.setString(16, nw.getNote5());
                stat.setString(17, nw.getNote6());
                stat.setString(18, nw.getNote7());
                stat.setString(19, nw.getNote8());
                stat.setString(20, nw.getNote9());
                stat.setString(21, nw.getNote10());
                stat.setString(22, nw.getNote11());
                stat.setString(23, nw.getNote12());
                stat.setString(24, nw.getNote13());
                stat.setString(25, nw.getNote14());
                stat.setString(26, nw.getNote15());
                stat.setString(27, nw.getIfflg());
                stat.setString(28, nw.getIfmsg());
                stat.executeUpdate();
            } else {
//                statUp.setString(1, nw.getMandt());
//                statUp.setString(2, nw.getAufnr());
//                statUp.setString(3, nw.getKtext());
//                statUp.setString(4, nw.getObjnr());
//                statUp.setString(5, nw.getPspel());
//                statUp.setString(6, nw.getPsphi());
//                statUp.setString(7, nw.getBukrs());
//                statUp.setString(8, nw.getGstrs());
//                statUp.setString(9, nw.getGltrs());
//                statUp.setString(10, nw.getErdat());
//                statUp.setString(11, nw.getErnam());
//                statUp.setString(12, nw.getNote1());
//                statUp.setString(13, nw.getNote2());
//                statUp.setString(14, nw.getNote3());
//                statUp.setString(15, nw.getNote4());
//                statUp.setString(16, nw.getNote5());
//                statUp.setString(17, nw.getNote6());
//                statUp.setString(18, nw.getNote7());
//                statUp.setString(19, nw.getNote8());
//                statUp.setString(20, nw.getNote9());
//                statUp.setString(21, nw.getNote10());
//                statUp.setString(22, nw.getNote11());
//                statUp.setString(23, nw.getNote12());
//                statUp.setString(24, nw.getNote13());
//                statUp.setString(25, nw.getNote14());
//                statUp.setString(26, nw.getNote15());
//                statUp.setString(27, nw.getIfflg());
//                statUp.setString(28, nw.getIfmsg());
//                statUp.setString(29, nw.getAufnr());
//                statUp.setString(30, nw.getPspel());
//                statUp.setString(31, nw.getPsphi());
//                statUp.executeUpdate();
            }
            conn.commit();
        }
        stat.close();
        statExs.close();
        statUp.close();
    }

    private void insertPsOrg(List<PsOrg> psOrg,Connection conn) throws SQLException {
        if(psOrg == null || psOrg.size() == 0){
            return;    
        }
        
        String sql =
            "INSERT INTO DMS_PS_ORG (MANDT, PSPNR, ZROLE, VERNR, SNAME, PLFAZ, PLSEZ, ERNAM, ERDAT, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5, " +
            "NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_PS_ORG SET MANDT=?, PSPNR=?, ZROLE=?, VERNR=?, SNAME=?, PLFAZ=?, PLSEZ=?, ERNAM=?, ERDAT=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, " +
            " NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(PSPNR,0)=NVL(?,0) AND NVL(ZROLE,0)=NVL(?,0)";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;

        stat = conn.prepareStatement(sql);
        statUp = conn.prepareStatement(sqlUp);
        statExs = conn.createStatement();

        for (PsOrg po : psOrg) {

            Map<String, String> keyValue = new HashMap<String, String>();
            keyValue.put("PSPNR", po.getPspnr());
            keyValue.put("ZROLE", po.getZrole());
            if (true) {
                stat.setString(1, po.getMandt());
                    stat.setString(2, po.getPspnr());
                    stat.setString(3, po.getZrole());
                    stat.setString(4, po.getVernr());
                    stat.setString(5, po.getSname());
                    stat.setString(6, po.getPlfaz());
                    stat.setString(7, po.getPlsez());
                    stat.setString(8, po.getErnam());
                    stat.setString(9, po.getErdat());
                    stat.setString(10, po.getNote1());
                    stat.setString(11, po.getNote2());
                    stat.setString(12, po.getNote3());
                    stat.setString(13, po.getNote4());
                    stat.setString(14, po.getNote5());
                    stat.setString(15, po.getNote6());
                    stat.setString(16, po.getNote7());
                    stat.setString(17, po.getNote8());
                    stat.setString(18, po.getNote9());
                    stat.setString(19, po.getNote10());
                    stat.setString(20, po.getNote11());
                    stat.setString(21, po.getNote12());
                    stat.setString(22, po.getNote13());
                    stat.setString(23, po.getNote14());
                    stat.setString(24, po.getNote15());
                    stat.setString(25, po.getIfflg());
                    stat.setString(26, po.getIfmsg());
                stat.executeUpdate();
            } else {
//                statUp.setString(1, po.getMandt());
//                statUp.setString(2, po.getPspnr());
//                statUp.setString(3, po.getZrole());
//                statUp.setString(4, po.getVernr());
//                statUp.setString(5, po.getSname());
//                statUp.setString(6, po.getPlfaz());
//                statUp.setString(7, po.getPlsez());
//                statUp.setString(8, po.getErnam());
//                statUp.setString(9, po.getErdat());
//                statUp.setString(10, po.getNote1());
//                statUp.setString(11, po.getNote2());
//                statUp.setString(12, po.getNote3());
//                statUp.setString(13, po.getNote4());
//                statUp.setString(14, po.getNote5());
//                statUp.setString(15, po.getNote6());
//                statUp.setString(16, po.getNote7());
//                statUp.setString(17, po.getNote8());
//                statUp.setString(18, po.getNote9());
//                statUp.setString(19, po.getNote10());
//                statUp.setString(20, po.getNote11());
//                statUp.setString(21, po.getNote12());
//                statUp.setString(22, po.getNote13());
//                statUp.setString(23, po.getNote14());
//                statUp.setString(24, po.getNote15());
//                statUp.setString(25, po.getIfflg());
//                statUp.setString(26, po.getIfmsg());
//                statUp.setString(27, po.getPspnr());
//                statUp.setString(28, po.getZrole());
//                statUp.executeUpdate();
            }
            conn.commit();
        }
        stat.close();
        statExs.close();
        statUp.close();
    }

    private void insertPsWbs(List<PsWbs> psWbs,Connection conn) throws SQLException {
        if(psWbs == null || psWbs.size() == 0){
            return;    
        }
        String sql =
            "INSERT INTO DMS_PS_WBS (MANDT, PSPNR, POST1, OBJNR, PSPHI, PBUKR, PSTRM, PETRM, ERNAM, ERDAT, AENAM, AEDAT, VERNR, VERNA, " +
            "NOTE1, NOTE2, NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_PS_WBS SET MANDT=?, PSPNR=?, POST1=?, OBJNR=?, PSPHI=?, PBUKR=?, PSTRM=?, PETRM=?, ERNAM=?, ERDAT=?, AENAM=?, AEDAT=?, VERNR=?, VERNA=?, " +
            " NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, NOTE5=?, NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(PSPNR,0)=NVL(?,0) AND NVL(PSPHI,0)=NVL(?,0)";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;

        stat = conn.prepareStatement(sql);
        statUp = conn.prepareStatement(sqlUp);
        statExs = conn.createStatement();

        for (PsWbs wbs : psWbs) {

            Map<String, String> keyValue = new HashMap<String, String>();
            keyValue.put("PSPNR", wbs.getPspnr());
            keyValue.put("PSPHI", wbs.getPsphi());
            if (true) {
                stat.setString(1, wbs.getMandt());
                stat.setString(2, wbs.getPspnr());
                stat.setString(3, wbs.getPost1());
                stat.setString(4, wbs.getObjnr());
                stat.setString(5, wbs.getPsphi());
                stat.setString(6, wbs.getPbukr());
                stat.setString(7, wbs.getPstrm());
                stat.setString(8, wbs.getPetrm());
                stat.setString(9, wbs.getErnam());
                stat.setString(10, wbs.getErdat());
                stat.setString(11, wbs.getAenam());
                stat.setString(12, wbs.getAedat());
                stat.setString(13, wbs.getVernr());
                stat.setString(14, wbs.getVerna());
                stat.setString(15, wbs.getNote1());
                stat.setString(16, wbs.getNote2());
                stat.setString(17, wbs.getNote3());
                stat.setString(18, wbs.getNote4());
                stat.setString(19, wbs.getNote5());
                stat.setString(20, wbs.getNote6());
                stat.setString(21, wbs.getNote7());
                stat.setString(22, wbs.getNote8());
                stat.setString(23, wbs.getNote9());
                stat.setString(24, wbs.getNote10());
                stat.setString(25, wbs.getNote11());
                stat.setString(26, wbs.getNote12());
                stat.setString(27, wbs.getNote13());
                stat.setString(28, wbs.getNote14());
                stat.setString(29, wbs.getNote15());
                stat.setString(30, wbs.getIfflg());
                stat.setString(31, wbs.getIfmsg());
                stat.executeUpdate();
            } else {
//                statUp.setString(1, wbs.getMandt());
//                statUp.setString(2, wbs.getPspnr());
//                statUp.setString(3, wbs.getPost1());
//                statUp.setString(4, wbs.getObjnr());
//                statUp.setString(5, wbs.getPsphi());
//                statUp.setString(6, wbs.getPbukr());
//                statUp.setString(7, wbs.getPstrm());
//                statUp.setString(8, wbs.getPetrm());
//                statUp.setString(9, wbs.getErnam());
//                statUp.setString(10, wbs.getErdat());
//                statUp.setString(11, wbs.getAenam());
//                statUp.setString(12, wbs.getAedat());
//                statUp.setString(13, wbs.getVernr());
//                statUp.setString(14, wbs.getVerna());
//                statUp.setString(15, wbs.getNote1());
//                statUp.setString(16, wbs.getNote2());
//                statUp.setString(17, wbs.getNote3());
//                statUp.setString(18, wbs.getNote4());
//                statUp.setString(19, wbs.getNote5());
//                statUp.setString(20, wbs.getNote6());
//                statUp.setString(21, wbs.getNote7());
//                statUp.setString(22, wbs.getNote8());
//                statUp.setString(23, wbs.getNote9());
//                statUp.setString(24, wbs.getNote10());
//                statUp.setString(25, wbs.getNote11());
//                statUp.setString(26, wbs.getNote12());
//                statUp.setString(27, wbs.getNote13());
//                statUp.setString(28, wbs.getNote14());
//                statUp.setString(29, wbs.getNote15());
//                statUp.setString(30, wbs.getIfflg());
//                statUp.setString(31, wbs.getIfmsg());
//                statUp.setString(32, wbs.getPspnr());
//                statUp.setString(33, wbs.getPsphi());
//                statUp.executeUpdate();
            }
            conn.commit();
        }
        stat.close();
        statExs.close();
        statUp.close();
    }

    private void insertPsWbsMaster(List<PsWbsMaster> psWbsMaster,Connection conn) throws SQLException {
        if(psWbsMaster == null || psWbsMaster.size() == 0){
            return;    
        }
        String sql =
            "INSERT INTO DMS_PS_WBS_MASTER (MANDT, POSNR, PSPHI, UP, DOWN, LEFT, RIGHT, NOTE1, NOTE2, NOTE3, NOTE4, " +
            "NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG,INSERT_DATE) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,SYSDATE)";
        String sqlUp =
            "UPDATE DMS_PS_WBS_MASTER SET MANDT=?, POSNR=?, PSPHI=?, UP=?, DOWN=?, LEFT=?, RIGHT=?, NOTE1=?, NOTE2=?, NOTE3=?, NOTE4=?, " +
            " NOTE5=?, NOTE6=?, NOTE7=?, NOTE8=?, NOTE9=?, NOTE10=?, NOTE11=?, NOTE12=?, NOTE13=?, NOTE14=?, NOTE15=?, IFFLG=?, IFMSG=?,INSERT_DATE=SYSDATE " +
            "WHERE NVL(POSNR,0)=NVL(?,0) AND NVL(PSPHI,0)=NVL(?,0)";
        PreparedStatement stat = null;
        PreparedStatement statUp = null;
        Statement statExs = null;

        stat = conn.prepareStatement(sql);
        statUp = conn.prepareStatement(sqlUp);
        statExs = conn.createStatement();

        for (PsWbsMaster wbs : psWbsMaster) {

            Map<String, String> keyValue = new HashMap<String, String>();
            keyValue.put("POSNR", wbs.getPosnr());
            keyValue.put("PSPHI", wbs.getPsphi());
            if (true) {
                stat.setString(1, wbs.getMandt());
                    stat.setString(2, wbs.getPosnr());
                    stat.setString(3, wbs.getPsphi());
                    stat.setString(4, wbs.getUp());
                    stat.setString(5, wbs.getDown());
                    stat.setString(6, wbs.getLeft());
                    stat.setString(7, wbs.getRight());
                    stat.setString(8, wbs.getNote1());
                    stat.setString(9, wbs.getNote2());
                    stat.setString(10, wbs.getNote3());
                    stat.setString(11, wbs.getNote4());
                    stat.setString(12, wbs.getNote5());
                    stat.setString(13, wbs.getNote6());
                    stat.setString(14, wbs.getNote7());
                    stat.setString(15, wbs.getNote8());
                    stat.setString(16, wbs.getNote9());
                    stat.setString(17, wbs.getNote10());
                    stat.setString(18, wbs.getNote11());
                    stat.setString(19, wbs.getNote12());
                    stat.setString(20, wbs.getNote13());
                    stat.setString(21, wbs.getNote14());
                    stat.setString(22, wbs.getNote15());
                    stat.setString(23, wbs.getIfflg());
                    stat.setString(24, wbs.getIfmsg());
                stat.executeUpdate();
            } else {
//                statUp.setString(1, wbs.getMandt());
//                statUp.setString(2, wbs.getPosnr());
//                statUp.setString(3, wbs.getPsphi());
//                statUp.setString(4, wbs.getUp());
//                statUp.setString(5, wbs.getDown());
//                statUp.setString(6, wbs.getLeft());
//                statUp.setString(7, wbs.getRight());
//                statUp.setString(8, wbs.getNote1());
//                statUp.setString(9, wbs.getNote2());
//                statUp.setString(10, wbs.getNote3());
//                statUp.setString(11, wbs.getNote4());
//                statUp.setString(12, wbs.getNote5());
//                statUp.setString(13, wbs.getNote6());
//                statUp.setString(14, wbs.getNote7());
//                statUp.setString(15, wbs.getNote8());
//                statUp.setString(16, wbs.getNote9());
//                statUp.setString(17, wbs.getNote10());
//                statUp.setString(18, wbs.getNote11());
//                statUp.setString(19, wbs.getNote12());
//                statUp.setString(20, wbs.getNote13());
//                statUp.setString(21, wbs.getNote14());
//                statUp.setString(22, wbs.getNote15());
//                statUp.setString(23, wbs.getIfflg());
//                statUp.setString(24, wbs.getIfmsg());
//                statUp.setString(25, wbs.getPosnr());
//                statUp.setString(26, wbs.getPsphi());
//                statUp.executeUpdate();
            }
            conn.commit();
        }
        stat.close();
        statExs.close();
        statUp.close();
    }

    private boolean bomIsExists(Statement statExs, HlsBomEntity bom) {
        String sql =
            "SELECT 1 FROM DMS_HLS_BOM T WHERE NVL(T.MATNR,0) = NVL('" + bom.getMatnr() +
            "',0) AND NVL(T.WERKS,0) = NVL('" + bom.getWerks() + "',0)";
        ResultSet rs;
        try {
            rs = statExs.executeQuery(sql);
            if (rs.next()) {
                return true;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean pkValidate(Statement statExs, String table,
                               Map<String, String> keyValue) {
        String sql = "SELECT 1 FROM " + table + " T WHERE 1=1 ";

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            sql =
sql + "AND NVL(T." + entry.getKey() + ",0) = NVL('" + entry.getValue() + "',0) ";
        }

        ResultSet rs;
        try {
            rs = statExs.executeQuery(sql);
            if (rs.next()) {
                return true;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
