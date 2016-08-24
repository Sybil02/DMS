package dms.quartz.job;

import common.MailSender;

import dms.quartz.utils.DBConnUtils;

import java.io.Serializable;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TimerMailJob implements Job,Serializable{
    
    private static final long serialVersionUID = 1L;

    public TimerMailJob() {
        super();
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap dataMap = jobDetail.getJobDataMap();
        String jobName = dataMap.get("jobName").toString();
        String locale = dataMap.get("locale").toString();
        
        //查询tempId，content
        String sql = "SELECT T.TEMP_ID,T.MAIL_CONTENT FROM DMS_TEMPLATE_TIMER T WHERE T.JOB_NAME = '" + jobName + "' "
                     + "AND T.LOCALE = '" + locale + "'";
        
        DBConnUtils dbUtils = new DBConnUtils();
        Connection conn = dbUtils.getJNDIConnectionByContainer("jdbc/DMSConnDS");
        String tempId = "";
        String tempName = "";
        String content = "";
        List<String> mailList = new ArrayList<String>();
        
        try {
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            if(rs.next()){
                tempId = rs.getString("TEMP_ID");
                content = rs.getString("MAIL_CONTENT");
            }
            rs.close();
            
//            String msql = "SELECT T.NAME,DU.ACC,DU.MAIL FROM DCM_TEMPLATE T,DCM_ROLE_TEMPLATE DR,DMS_GROUP_ROLE GR,DMS_USER_GROUP UR,DMS_USER DU "
//                + "WHERE T.ID = DR.TEMPLATE_ID AND DR.ROLE_ID = GR.ROLE_ID AND GR.GROUP_ID = UR.GROUP_ID AND UR.USER_ID = DU.ID "
//                + "AND T.LOCALE = DU.LOCALE AND T.LOCALE = '" + locale + "' AND DR.READ_ONLY = 'N' AND T.ID = '" + tempId + "'";
            
            String msql2 = "SELECT T.TEMPLATE_ID AS NAME,D.ACC,D.MAIL FROM WC_TEMPLATE_USER T,DMS_USER D WHERE T.USER_ID = D.NAME";
            ResultSet rsm = stat.executeQuery(msql2);
            while(rsm.next()){
                String email = rsm.getString("MAIL");
                if(email != null){
                    mailList.add(email);
                }
                tempName = rsm.getString("NAME");
            }
            rsm.close();
            stat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if(locale.equals("zh_CN")){
             content = "<div align='center'><P>===========================请及时填写模板数据============================</P></div></br><P>备注：" + content 
                       + "</P></br><div align='center'><P>===========================系统邮件，请勿回复============================</P></div>";   
        }else{
             content = "<div align='center'><P>===========================Please finish in time============================</P></div></br><P>备注：" + content 
                      + "</P></br><div align='center'><P>===========================System mail do not reply============================</P></div>";  
        }
        this.sendEmail(tempName, content,"jdbc/DMSConnDS", mailList);
        
    }
    
    public void sendEmail(String tempName,String content,String jndiName,List<String> mailList){
        MailSender sender = new MailSender(jndiName);
        for(String email : mailList){
            try {
                sender.send(email, "[SYSTEM] : "+tempName, content);
                System.out.println(new Date() + ":" + "Email Send>>>>>>"+ email + ">>>>>>");
            } catch (AddressException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        } 
    }
    
}
