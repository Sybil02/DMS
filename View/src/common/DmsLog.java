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
    private String localname;
    private String localip;
    private String macadd;
    public void loginMsg(String curUser,String newId){
        InetAddress ia=null;
        try {
            ia=ia.getLocalHost();
            localname=ia.getHostName();
            localip=ia.getHostAddress();
            macadd = getLocalMac(ia);
            DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
            Statement stat = trans.createStatement(DBTransaction.DEFAULT);
            String sql = "INSERT INTO HLS_LOGIN_LOGOUT_LOG(LOGIN_ID,LOGIN_MAN,LOGIN_TIME,LOGIN_HOSTNAME,LOGIN_HOSTIP,LOGIN_MACADD) " +
                "VALUES ('"+newId+"','"+curUser+"',SYSDATE,'"+localname+
                "','"+localip+"','"+macadd+"')";
            stat.execute(sql);
            trans.commit();
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    
    public String getLocalMac(InetAddress ia) throws SocketException {
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
    
    public void logoutMsg(String newId){
        DBTransaction trans = (DBTransaction)DmsUtils.getDmsApplicationModule().getTransaction();
        Statement stat = trans.createStatement(DBTransaction.DEFAULT);
        String sql = "UPDATE HLS_LOGIN_LOGOUT_LOG SET (LOGOUT_TIME)= SYSDATE WHERE LOGIN_ID='"+newId+"'";
        int i = -22;
        try {
            i = stat.executeUpdate(sql);
            trans.commit();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void operationLog(String curUser,String temId,String comMsg,String operation){
        
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
