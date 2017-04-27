package com.appspot.vcdiss.utils;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Properties;

/**
 * Utility class to avoid repeating email boilerplate.
 */
public class EmailUtils {

    public static void sendEmail(String messageBody, String subject, String email) throws MalformedURLException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props);

        Message msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress("tomthornton.123@gmail.com", "vc-diss"));

            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

            msg.setSubject(subject);
            msg.setText(messageBody);
            msg.setSentDate(new Date());

            Transport.send(msg);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR::::::" + e.getStackTrace());
            System.out.println("ERROR::::::" + e.getMessage());
            System.out.println("ERROR::::::" + e.getLocalizedMessage());
        }



    }
}
