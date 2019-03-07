import com.sun.mail.smtp.SMTPMessage;
import java.util.Formatter;
import java.util.Properties;
import java.util.Date;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MailServer {

    private String smtpHostServer = "smtp.yandex.ru";
    private String emailID = "";
    private String password = "";
    private  Session session;


    MailServer(){
        Properties props = new Properties();
        props = System.getProperties();
        props.put("mail.smtp.host", "smtp.yandex.ru"); //SMTP Host
        props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
        props.put("mail.smtp.port", "465"); //SMTP Port

        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailID, password);
            }
        };

        session = Session.getDefaultInstance(props, auth);
    }

    public void sendEmail(String login, String password, String email, String fName){
        try
        {
            Formatter f = new Formatter();
            String subject = "Спасибо за регистрацию в проекте";
            String body = ""; //тело письма(заполнить)
            SMTPMessage msg = new SMTPMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");


            msg.setFrom(new InternetAddress("kosticovproject@yandex.ru", "MyProject"));
            //msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false));

            msg.setSubject(subject, "UTF-8");
            msg.setText(f.format("Здравствуйте %s, Ваши регистрационные данные:%nЛогин: %s%nПароль: %s",fName,login,password).toString(), "UTF-8");

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
            //System.out.println("Message is ready");
            Transport.send(msg);

            //System.out.println("EMail Sent Successfully!!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
