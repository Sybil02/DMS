package datamanager.ws;

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
    
    public String insertSapBom(SapBomEntity sbe){
        //Connection conn = DBConnUtils.getJNDIConnectionByContainer("jdbc/DMSConnDS");
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        String sql = "INSERT INTO HPDW.SAP_BOM_MATERIAL_DATA (CUSTOM, MCODE, MDESC, FACTORY, MTYPE, MEINS, MTYPEDESC, MRPCONTROL, MCATORY, STPRICE, PRICEUNIT, PLANPRICE, D_FLAG, NOTE1, NOTE2, " +
            "NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG)" + 
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stat = null;
        try {
            stat = conn.prepareStatement(sql);
            stat.setString(1, sbe.getCustom());
            stat.setString(2, sbe.getMcode());
            stat.setString(3, sbe.getMdesc());
            stat.setString(4, sbe.getFactory());
            stat.setString(5, sbe.getMtype());
            stat.setString(6, sbe.getMeins());
            stat.setString(7, sbe.getMtypedesc());
            stat.setString(8, sbe.getMrpcontrol());
            stat.setString(9, sbe.getMcatory());
            stat.setString(10, sbe.getStprice());
            stat.setString(11, sbe.getPriceunit());
            stat.setString(12, sbe.getPlanprice());
            stat.setString(13, sbe.getD_flag());
            stat.setString(14, sbe.getNote1());
            stat.setString(15, sbe.getNote2());
            stat.setString(16, sbe.getNote3());
            stat.setString(17, sbe.getNote4());
            stat.setString(18, sbe.getNote5());
            stat.setString(19, sbe.getNote6());
            stat.setString(20, sbe.getNote7());
            stat.setString(21, sbe.getNote8());
            stat.setString(22, sbe.getNote9());
            stat.setString(23, sbe.getNote10());
            stat.setString(24, sbe.getNote11());
            stat.setString(25, sbe.getNote12());
            stat.setString(26, sbe.getNote13());
            stat.setString(27, sbe.getNote14());
            stat.setString(28, sbe.getNote15());
            stat.setString(29, sbe.getIfflg());
            stat.setString(30, sbe.getIfmsg());
            stat.executeUpdate();
            conn.commit();
            stat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage();
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
        return "SUCCESSED";    
    }
    
    public String test(String custom,String mcode,String mdesc,String factory,String mtype,
                       String meins,String mtypedesc,String mrpcontrol,String mcatory,String stprice){
        //Connection conn = DBConnUtils.getJNDIConnectionByContainer("jdbc/DMSConnDS");
        Connection conn = DBConnUtils.getJNDIConnection("jdbc/DMSConnDS");
        String sql = "INSERT INTO HPDW.SAP_BOM_MATERIAL_DATA (CUSTOM, MCODE, MDESC, FACTORY, MTYPE, MEINS, MTYPEDESC, MRPCONTROL, MCATORY, STPRICE, PRICEUNIT, PLANPRICE, D_FLAG, NOTE1, NOTE2, " +
            "NOTE3, NOTE4, NOTE5, NOTE6, NOTE7, NOTE8, NOTE9, NOTE10, NOTE11, NOTE12, NOTE13, NOTE14, NOTE15, IFFLG, IFMSG)" + 
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stat = null;
        try {
            stat = conn.prepareStatement(sql);
            stat.setString(1, custom);
            stat.setString(2, mcode);
            stat.setString(3, mdesc);
            stat.setString(4, factory);
            stat.setString(5, mtype);
            stat.setString(6, meins);
            stat.setString(7, mtypedesc);
            stat.setString(8, mrpcontrol);
            stat.setString(9, mcatory);
            stat.setString(10, stprice);
            stat.setString(11, "1");
            stat.setString(12, "1");
            stat.setString(13, "1");
            stat.setString(14, "1");
            stat.setString(15, "1");
            stat.setString(16, "1");
            stat.setString(17, "1");
            stat.setString(18, "1");
            stat.setString(19, "1");
            stat.setString(20, "1");
            stat.setString(21, "1");
            stat.setString(22, "1");
            stat.setString(23, "1");
            stat.setString(24, "1");
            stat.setString(25, "1");
            stat.setString(26, "1");
            stat.setString(27, "1");
            stat.setString(28, "1");
            stat.setString(29, "1");
            stat.setString(30, "1");
            stat.executeUpdate();
            conn.commit();
            stat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage();
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
        return "SUCCESSED";    
    }
    
}
