        package controllers;

        import com.fasterxml.jackson.databind.JsonNode;
        import com.sun.mail.smtp.SMTPMessage;
        import controllers.security.Authenticator;
        import dao.UserDao;
        import models.User;
        import play.Logger;
        import play.db.jpa.Transactional;
        import play.libs.Json;
        import play.mvc.Controller;
        import play.mvc.Result;
        import javax.inject.Inject;
        import javax.mail.*;
        import javax.mail.internet.InternetAddress;
        import java.security.NoSuchAlgorithmException;
        import java.util.List;
        import java.util.Properties;


        public class UserController extends Controller {

            private final static Logger.ALogger LOGGER = Logger.of(UserController.class);

            private UserDao userDao;

            @Inject
            public UserController(UserDao userDao) {
                this.userDao = userDao;
            }

            @Transactional
            public Result createUser() throws NoSuchAlgorithmException {

                LOGGER.debug("inside CreateUser method");

                final JsonNode jsonNode = request().body().asJson();
                final String userName = jsonNode.get("name").asText();
                final String email = jsonNode.get("email").asText();
                final String password = jsonNode.get("password").asText();
                final String likes = jsonNode.get("likes").asText();
                //final String imagePath = jsonNode.get("path").asText();

                if (null == userName) {
                    return badRequest("Missing userName");
                }

                if (null == email) {
                    return badRequest("Missing email");
                }

                if (null == password) {
                    return badRequest("Missing password");
                }

                if (null != userDao.getUserByName(userName) || null !=userDao.getUserByEmail(email)) {
                    LOGGER.debug("user already exists");
                    return badRequest("user already exists");
                }

                User user = new User();
                user.setUserName(userName);
                user.setEmail(email);
                user.setRole(User.Role.User);
                //user.setImagePath(imagePath);

                String salt = userDao.generateSalt();
                user.setSalt(salt);

                String hashedPassword = userDao.hashedPassword(password, salt, 30);
                user.setPassword(hashedPassword);
                user.setLikes(likes);

                user = userDao.persist(user);

                LOGGER.debug("User created");
                return created(user.getUserName().toString());
            }


            @Transactional
            public Result login() throws NoSuchAlgorithmException {

                LOGGER.debug("inside login method");

                final JsonNode jsonNode = request().body().asJson();
                final String userName = jsonNode.get("name").asText();
                final String pwd = jsonNode.get("password").asText();

                if (null == userName) {
                    return badRequest("Missing userName");
                }

                if (null == pwd) {
                    return badRequest("Missing password");
                }

                final User user = userDao.getUserByName(userName);
                LOGGER.debug("Got result in login");

                if (null != user) {

                    String salt = user.getSalt().toString();

                    String hashedPassword = userDao.hashedPassword(pwd, salt, 30);

                    if (user.getPassword().equals(hashedPassword)) {

                        String accessToken = userDao.generateAccessToken();
                        user.setToken(accessToken);

                        String refreshToken = userDao.generateRefreshToken();
                        user.setRefreshToken(refreshToken);

                        Long expiry = userDao.generateExpiryTime(12*60);
                        user.setTokenExpire(expiry);

                        userDao.persist(user);


                        com.fasterxml.jackson.databind.node.ObjectNode result = Json.newObject();
                        result.put("access_token" , accessToken);
                        result.put("token_expiry" , expiry);
                        result.put("refresh_token" , refreshToken);
                        result.put("role",user.getRole().toString());

                        LOGGER.debug("end of login successful");

                        return ok(result);
                    }

                    return unauthorized("incorrect password");
                }

                return unauthorized("login unsuccessful!!");

            }


            @Transactional
            @Authenticator
            public Result getCurrentUser() {

                LOGGER.debug("Get current user");

                final User user = (User) ctx().args.get("user");

                LOGGER.debug("User: {}", user);
                com.fasterxml.jackson.databind.node.ObjectNode json = Json.newObject();
                json.put("userName", user.getUserName());
                json.put("email", user.getEmail());
                json.put("likes",user.getLikes());
                json.put("role",user.getRole().toString());

                return ok(json);
            }

            @Transactional
            public Result getAllUsers() {

                final List<User> users = userDao.findAll();

                final JsonNode jsonNode1 = Json.toJson(users);

                return ok(jsonNode1);
            }

            @Transactional
            @Authenticator
            public Result updatePassword() throws NoSuchAlgorithmException {
                LOGGER.debug("Inside updatePassword ");

                final JsonNode jsonNode = request().body().asJson();
                LOGGER.debug("after Json");
                final String oldPassword = jsonNode.get("opassword").asText();
                final String newPassword = jsonNode.get("npassword").asText();

                LOGGER.debug("Taking Json");

                if (null == oldPassword) {
                    return badRequest("Missing current password");
                }

                if (null == newPassword) {
                    return badRequest("Missing new password");
                }

                LOGGER.debug("Got current user");

                final User user = (User) ctx().args.get("user");

                String salt = user.getSalt();
                String hashedPassword = userDao.hashedPassword(oldPassword, salt, 30);

                if (user.getPassword().equals(hashedPassword)) {
                    LOGGER.debug("passwords matched");

                    String newHashedPassword = userDao.hashedPassword(newPassword, salt, 30);
                    user.setPassword(newHashedPassword);
                    userDao.persist(user);

                    LOGGER.debug("before ok");

                    com.fasterxml.jackson.databind.node.ObjectNode result = Json.newObject();
                    result.put("userName" ,user.getUserName());
                    LOGGER.debug(String.valueOf(result));

                    return ok(result);
                }
                LOGGER.debug("bad request");

              return badRequest();

            }

            @Transactional
            public Result resetAccessToken(){

                final JsonNode jsonNode = request().body().asJson();
                final String refreshToken = jsonNode.get("refresh_token").asText();

                User user = userDao.findRefreshToken(refreshToken);

                if( user.getRefreshToken().equals(refreshToken) ){

                    String accessToken = userDao.generateAccessToken();
                    user.setToken(accessToken);

                    Long expiryTime = userDao.generateExpiryTime(12*60);
                    user.setTokenExpire(expiryTime);

                    userDao.persist(user);

                    com.fasterxml.jackson.databind.node.ObjectNode result = Json.newObject();
                    result.put("access_token" , accessToken);
                    result.put("token_expiry" , expiryTime);
                    result.put("refresh_token" , refreshToken);
                    result.put("role",user.getRole().toString());

                    return ok(result);

                }

                return badRequest();
            }

            @Transactional
            @Authenticator
            public Result updateProfile() {

                LOGGER.debug("inside updateProfile");

                User user = (User) ctx().args.get("user");

                final JsonNode jsonNode = request().body().asJson();
                final String likes =jsonNode.get("likes").asText();

                user.setLikes(likes);
                userDao.persist(user);

                LOGGER.debug("User: {}", user);
                com.fasterxml.jackson.databind.node.ObjectNode json = Json.newObject();
                json.put("userName", user.getUserName());
                json.put("email", user.getEmail());
                json.put("likes",user.getLikes());

                return ok(json);
            }

            @Transactional
            @Authenticator
            public Result logout(){

                LOGGER.debug("inside logout");
                final User user = (User) ctx().args.get("user");

               user.setToken(null);
               user.setTokenExpire(null);
               user.setRefreshToken(null);

               userDao.persist(user);

               LOGGER.debug("logout successfull!!");
               return ok("Logout successfull!!");
            }


            @Transactional
            @Authenticator
            public Result emailToAdmin(){

                LOGGER.debug("Inside mailer method");

                User user = (User) ctx().args.get("user");
                final String email = user.getEmail();

                JsonNode jsonNode = request().body().asJson();
                final String name = jsonNode.get("fname").asText();
                final long contact = jsonNode.get("contact").asLong();


                user.setContact(contact);

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class",
                        "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("platrovacare02@gmail.com","Platrova1234");
                    }
                });

                try {

                    LOGGER.debug("Inside mail try");

                    String url = "http://localhost:9000/user/role/";

                    SMTPMessage message = new SMTPMessage(session);
                    message.setFrom(new InternetAddress("platrovacare02@gmail.com"));
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse( "platrovaservice@gmail.com"));

                    message.setSubject("Received mail from" + email);
                    message.setText("Make " + name + " as Client after verifying.\n Details:\n Contact: " + contact + "\n emailid: " + email + "\nClick the link below to update the role after verifying the Client:\n "+ url + email);

                    message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

                    Transport.send(message);

                    System.out.println("sent");
                    userDao.persist(user);

                }

                catch (MessagingException e) {

                    LOGGER.debug("Inside mail catch");
                    throw new RuntimeException(e);
                }

                return ok("working");
            }


            @Transactional
            public Result updateRole(String email){

                User user = userDao.getUserByEmail(email);
                user.setRole(User.Role.Client);

                userDao.persist(user);
                LOGGER.debug(user.getRole().toString());

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class",
                        "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("platrovaservice@gmail.com","Platrova1234");
                    }
                });

                try {

                    LOGGER.debug("Inside mail try");

                    String url="http://localhost:3000/login";
                    SMTPMessage message = new SMTPMessage(session);
                    message.setFrom(new InternetAddress("platrovaservice@gmail.com"));
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(email));

                    message.setSubject("Approval Mail");
                    message.setText("Hey " + user.getUserName() +",\n Congratulations on becoming a Client.\n Click here to login:  " + url);

                    message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

                    Transport.send(message);

                    System.out.println("sent");

                }

                catch (MessagingException e) {

                    LOGGER.debug("Inside mail catch");
                    throw new RuntimeException(e);
                }

                LOGGER.debug("");

                return ok("Role updated & approval mail is sent to the Client");

            }




        }

