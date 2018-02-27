package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.sun.mail.smtp.SMTPMessage;
import dao.UserDao;
import models.TemporaryStorage;
import models.User;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.*;
import javax.mail.internet.InternetAddress;

public class MailerService extends Controller {


    private final static Logger.ALogger LOGGER = Logger.of(MailerService.class);

    private UserDao userDao;

     TemporaryStorage map = new TemporaryStorage();

    @Inject
    public MailerService(UserDao userDao) {
        this.userDao=userDao;
    }

    @Transactional
    public Result forgotPassword(String email) {

        LOGGER.debug("Inside forgotPassword method");

        User user  = userDao.getUserByEmail(email);
        String recoveryToken  = userDao.generateAccessToken();
        Long timeStamp  = userDao.generateExpiryTime();

        F.Tuple<User, Long> tuple = new F.Tuple(user, timeStamp);

        map.addMap(recoveryToken, tuple);

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("platrovaservice@gmail.com","Platrova1234");
            }
        });

        try {

            LOGGER.debug("Inside mail try");

            String url="https://localhost/forgotPassword";
            SMTPMessage message = new SMTPMessage(session);
            message.setFrom(new InternetAddress("platrovaservice@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse( email));

            message.setSubject("Forgot Password");
            message.setText("To reset password, click here:\n"+url+"/reset?token=" + recoveryToken);

            message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

            Transport.send(message);

            System.out.println("sent");

        }

        catch (MessagingException e) {

            LOGGER.debug("Inside mail catch");
            throw new RuntimeException(e);
        }

        LOGGER.debug(map.toString());

        return ok("working");
    }


   @Transactional
    public Result resetPassword() throws NoSuchAlgorithmException {

        LOGGER.debug("Inside resetPassword");

        final JsonNode jsonNode = request().body().asJson();
        final String newPassword = jsonNode.get("newPassword").asText();
        final String userToken = jsonNode.get("token").asText();

        LOGGER.debug("this is token:" + userToken);

        ConcurrentHashMap<String, F.Tuple<User, Long>> result = map.getMap();

        F.Tuple tuple = result.get(userToken);
        LOGGER.debug(String.valueOf(tuple));


        if (null == tuple) {
            return forbidden();
        }

        User user = (User) tuple._1;
        Long timestamp = (Long) tuple._2;

        Long currentTime = new Timestamp(System.currentTimeMillis()).getTime();

        if(currentTime > timestamp){
            return badRequest("the link is no longer valid");

        }
        LOGGER.debug("before salt");

        String salt = user.getSalt();

        String hashedPassword = userDao.hashedPassword(newPassword, salt, 30);

            user.setPassword(hashedPassword);
            LOGGER.debug("before persist");
            userDao.updatePassword(hashedPassword,user.getUserName());
            LOGGER.debug("persisted");
        return  ok("Successfully reset the password");


    }
}
