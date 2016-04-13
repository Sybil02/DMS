package common;

import dms.quartz.utils.DBConnUtils;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.ObjectUtils;

public class MailSender {
    private MailAuthenticator authenticator;
    private Session session;
    private String smtpHostName;
    private String port;
    public MailSender(){
        Map map = DmsUtils.getSystemProperty();
        String smtpHost=ObjectUtils.toString(map.get("mail.host"));
        String userName=ObjectUtils.toString(map.get("mail.account"));
        String password=ObjectUtils.toString(map.get("mail.password"));
        String port=ObjectUtils.toString(map.get("mail.port"));
        this.authenticator = new MailAuthenticator(userName, password);
        this.smtpHostName = smtpHost;
        this.port=port;
        init();
    }
    
    public MailSender(String jndiName){
        Map map = this.getEmailProperty(jndiName);
        String smtpHost=ObjectUtils.toString(map.get("mail.host"));
        String userName=ObjectUtils.toString(map.get("mail.account"));
        String password=ObjectUtils.toString(map.get("mail.password"));
        String port=ObjectUtils.toString(map.get("mail.port"));
        this.authenticator = new MailAuthenticator(userName, password);
        this.smtpHostName = smtpHost;
        this.port=port;
        init();
    }
    
    public Map getEmailProperty(String jndiName){
        Map<String,String> map = new HashMap<String,String>();
        
        DBConnUtils dbUtils = new DBConnUtils();
        Connection conn = dbUtils.getJNDIConnectionByContainer(jndiName);
        String sql = "SELECT T.CKEY,T.CVALUE FROM DMS_PROPERTY T WHERE T.ENABLE_FLAG = 'Y'";
        Statement stat;
        try {
            stat = conn.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            while(rs.next()){
                map.put(rs.getString("CKEY"), rs.getString("CVALUE"));
            }
            rs.close();
            stat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
    
    public MailSender(String smtpHostName, String username, String password,String port) {
        this.authenticator = new MailAuthenticator(username, password);
        this.smtpHostName = smtpHostName;
        this.port = port;
        init();
    }

    private void init() {
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.host", this.smtpHostName);
        props.setProperty("mail.smtp.port", this.port);
        this.session = Session.getInstance(props, this.authenticator);
    }

    public void send(String recipient, String subject,
                     Object content) throws AddressException,
                                            MessagingException {
        // 创建mime类型邮件
        final MimeMessage message = new MimeMessage(session);
        // 设置发信人
        message.setFrom(new InternetAddress(authenticator.getUserName()));
        // 设置收件人
        message.setRecipient(MimeMessage.RecipientType.TO,
                             new InternetAddress(recipient));
        // 设置主题
        message.setSubject(subject);
        // 设置邮件内容
        message.setContent(content.toString(), "text/html;charset=utf-8");
        // 发送
        Transport.send(message);
    }

    public void send(List<String> recipients, String subject,
                     Object content) throws AddressException,
                                            MessagingException {
        // 创建mime类型邮件
        final MimeMessage message = new MimeMessage(session);
        // 设置发信人
        message.setFrom(new InternetAddress(authenticator.getUserName()));
        // 设置收件人们
        final int num = recipients.size();
        InternetAddress[] addresses = new InternetAddress[num];
        for (int i = 0; i < num; i++) {
            addresses[i] = new InternetAddress(recipients.get(i));
        }
        message.setRecipients(MimeMessage.RecipientType.TO, addresses);
        // 设置主题
        message.setSubject(subject);
        // 设置邮件内容
        message.setContent(content.toString(), "text/html;charset=utf-8");
        // 发送
        Transport.send(message);
    }

    public void setAuthenticator(MailAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public MailAuthenticator getAuthenticator() {
        return authenticator;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void setSmtpHostName(String smtpHostName) {
        this.smtpHostName = smtpHostName;
    }

    public String getSmtpHostName() {
        return smtpHostName;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }
}
