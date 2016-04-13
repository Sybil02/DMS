package common;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class MailTest {

    public static void main(String[] args) {
        MailSender sender=new MailSender(
                              "smtp.sina.com","dmshand@sina.com","handhand","25");
        try {
            for(int i=0;i<5;i++){
                sender.send("tiegang.wang@hand-china.com", "Test", "Hi");
            }
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
