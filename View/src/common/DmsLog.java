package common;

import java.net.InetAddress;

import java.net.NetworkInterface;
import java.net.SocketException;

import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.Date;

import oracle.jbo.server.DBTransaction;

public class DmsLog {
    public DmsLog() {
        super();
    }
    private static String loginTime ;
    private static String localname;
    private static String localip;
    private static String macadd;
    public static void loginMsg(String curUser){
        Date loginDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        loginTime = sdf.format(loginDate);
        InetAddress ia=null;
        try {
            ia=ia.getLocalHost();
            localname=ia.getHostName();
            localip=ia.getHostAddress();
            macadd = getLocalMac(ia);
            DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            String sql = "INSERT INTO HLS_LOGIN_LOGOUT_LOG(LOGIN_MAN,LOGIN_TIME,LOGIN_HOSTNAME,LOGIN_HOSTIP,LOGIN_MACADD) " +
                "VALUES ('"+curUser+"',TO_DATE('"+sdf.format(loginDate)+"','yyyy/mm/dd hh24:mi:ss'),'"+localname+
                "','"+localip+"','"+macadd+"')";
            stat.execute(sql);
            trans.commit();
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    
    public static String getLocalMac(InetAddress ia) throws SocketException {
                    //获取网卡，获取地址
                    byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
                    StringBuffer sb = new StringBuffer("");
                    for(int i=0; i<mac.length; i++) {
                            if(i!=0) {
                                    sb.append("-");
                            }
                            //字节转换为整数
                            int temp = mac[i]&0xff;
                            String str = Integer.toHexString(temp);
                            if(str.length()==1) {
                                    sb.append("0"+str);
                            }else {
                                    sb.append(str);
                            }
                    }
                    return sb.toString().toUpperCase();
    }
    
    public static void logoutMsg(String curUser){
        Date logoutDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "UPDATE HLS_LOGIN_LOGOUT_LOG SET (LOGOUT_TIME)= " +
            "(TO_DATE('"+sdf.format(logoutDate)+"','yyyy/mm/dd hh24:mi:ss')) "+
            "WHERE LOGIN_TIME=TO_DATE('"+loginTime+"','yyyy/mm/dd hh24:mi:ss')"+
            " AND LOGIN_HOSTNAME = '"+localname+"' AND LOGIN_MAN ='"+curUser+"'";
        int i = -22;
        try {
            i = stat.executeUpdate(sql);
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void operationLog(String curUser,String temId,String comMsg,String operation){
        
        Date updateDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "INSERT INTO HLS_OPERATION_LOG (UPDATE_BY,TEMPLATE_ID,COMRECORD_MSG,OPERATION,UPDATE_TIME) "+
            "VALUES ('"+curUser+"','"+temId+"','"+comMsg+"','"+operation+"',TO_DATE('"+sdf.format(updateDate)+"','yyyy/mm/dd hh24:mi:ss'))";
        
        try {
            stat.execute(sql);
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
