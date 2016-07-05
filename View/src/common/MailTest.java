package common;

import com.bea.security.utils.DigestUtils;

import dms.quartz.utils.DBConnUtils;

import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class MailTest {

    public static void main(String[] args) {
//        MailSender sender=new MailSender(
//                              "mail.hand-china.com","xiangjia.he@hand-china.com","h339449!");
//        try {
//            for(int i=0;i<100;i++){
//            sender.send("zhenyi.yang@hand-china.com", "Test", "Hi");
//            }
//        } catch (AddressException e) {
//            e.printStackTrace();
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//        try {
//            //encyptPwd();
//            System.out.println(DigestUtils.digestSHA1("1449609IcFw6YkU3Ufli8Eli6UChISBrw="));
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

    }
    
//    private static void encyptPwd() throws NoSuchAlgorithmException,
//                                    UnsupportedEncodingException {
//        Connection conn = DBConnUtils.getJDBCConnection();
//        //查询出所有明文密码的用户
//        String sql = "SELECT T.ID,T.ACC,T.PWD FROM DMS_USER T WHERE T.ID LIKE 'U%'";
//        Statement stat;
//        Statement statUp;
//        try {
//            stat = conn.createStatement();
//            statUp = conn.createStatement();
//            ResultSet rs = stat.executeQuery(sql);
//            while(rs.next()){
//                String ecPwd = DigestUtils.digestSHA1(rs.getString("ACC") + rs.getString("PWD"));   
//                //将加密后的密码更新回去
//                String uSql = "UPDATE DMS_USER T SET T.PWD = '" + ecPwd + "' WHERE T.ID = '" + rs.getString("ID") + "'";
//                System.out.println("将用户【"+rs.getString("ACC")+"】密码【"+rs.getString("PWD")+"】加密为【"+ecPwd+"】");
//                statUp.executeUpdate(uSql);
//                conn.commit();
//            }
//            stat.close();
//            statUp.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
}
