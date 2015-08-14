package common;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class MailTest {

    public static void main(String[] args) {
        MailSender sender=new MailSender(
                              "mail.hand-china.com","25","xiangjia.he@hand-china.com","h339449!");
               try {
            
            sender.send("qiyu.xia@hand-china.com", "Test", "Hi");
            
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
