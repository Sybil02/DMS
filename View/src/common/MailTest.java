package common;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class MailTest {

    public static void main(String[] args) {
        MailSender sender=new MailSender(
                              "mail.hand-china.com","xiangjia.he@hand-china.com","h339449!");
        try {
            for(int i=0;i<100;i++){
            sender.send("zhenyi.yang@hand-china.com", "Test", "Hi");
            }
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
