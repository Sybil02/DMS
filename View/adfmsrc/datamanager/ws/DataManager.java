package datamanager.ws;

import datamanager.entity.HlsBomEntity;

import dms.quartz.utils.DBConnUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.jws.WebService;

@WebService
public class DataManager {
    public DataManager() {
        super();
    }
    
    public HlsBomEntity SyncWuliao(HlsBomEntity bom){
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        String sql = "INSERT INTO DMS_HLS_BOM (MATNR, MAKTX, WERKS, MTART, MEINS, MTBEZ, DISPO, ZGOOD, STPRS, PEINH, ZPLP2, " +
            "LVORM, NOTE1, NOTE2, NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG, MSGID) " + 
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stat = null;
        try {
            stat = conn.prepareStatement(sql);
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
            conn.commit();
            stat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            bom.setIfflg("E");
            if(e.getMessage().length() > 50){
                bom.setIfmsg(e.getMessage().substring(0, 49));
            }else{
                bom.setIfmsg(e.getMessage());
            }
            return bom;
        }finally{
            try{
                if(stat != null){
                    stat.close();
                }
                if(conn != null)
                    conn.close();
            }catch(Exception e){
                e.printStackTrace();    
            }
        }
        bom.setIfflg("S");
        return bom;    
    }
    
}
