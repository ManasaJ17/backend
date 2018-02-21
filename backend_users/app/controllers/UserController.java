    package controllers;


    import com.fasterxml.jackson.core.JsonProcessingException;
    import com.fasterxml.jackson.databind.JsonNode;
    import controllers.security.Authenticator;
    import dao.UserDao;
    import models.User;
    import play.Logger;
    import play.db.jpa.Transactional;
    import play.libs.Json;
    import play.mvc.Controller;
    import play.mvc.Result;

    import javax.inject.Inject;
    import java.security.NoSuchAlgorithmException;
    import java.util.List;



    public class UserController extends Controller {

        private final static Logger.ALogger LOGGER = Logger.of(UserController.class);

        private UserDao userDao;

        @Inject
        public UserController(UserDao userDao) {
            this.userDao = userDao;
        }

        @Transactional
        public Result createUser() throws NoSuchAlgorithmException {

            final JsonNode jsonNode = request().body().asJson();
            final String userName = jsonNode.get("name").asText();
            final String email = jsonNode.get("email").asText();
            final String password = jsonNode.get("password").asText();
            //final String role = jsonNode.get("role").asText();

            if (null == userName) {
                return badRequest("Missing userName");
            }

            if (null == email) {
                return badRequest("Missing email");
            }

            if (null == password) {
                return badRequest("Missing password");
            }

            User user = new User();
            user.setUserName(userName);
            user.setEmail(email);
            user.setRole(User.Role.User);

            String salt = userDao.generateSalt();
            user.setSalt(salt);

            String hashedPassword = userDao.hashedPassword(password, salt, 30);
            user.setPassword(hashedPassword);

            user = userDao.persist(user);

            return created(user.getUserName().toString());
        }


        @Transactional
        public Result login() throws NoSuchAlgorithmException {

            final JsonNode jsonNode = request().body().asJson();
            final String userName = jsonNode.get("name").asText();
            final String pwd = jsonNode.get("password").asText();

            if (null == userName) {
                return badRequest("Missing userName");
            }

            if (null == pwd) {
                return badRequest("Missing password");
            }

            final User user = userDao.getUser(userName);
            LOGGER.debug("Got result");

            LOGGER.debug(String.valueOf(user));

            if (null != user) {

                String salt = user.getSalt().toString();

                String hashedPassword = userDao.hashedPassword(pwd, salt, 30);

                if (user.getPassword().equals(hashedPassword)) {

                    String accessToken = userDao.generateToken();
                    user.setToken(accessToken);

                    String refreshToken = userDao.generateRefreshToken();
                    user.setRefreshToken(refreshToken);

                    Long expiry = userDao.generateExpiryTime();
                    user.setTokenExpire(expiry);

                    userDao.persist(user);


                    com.fasterxml.jackson.databind.node.ObjectNode result = Json.newObject();
                    result.put("access_token" , accessToken);
                    result.put("token_expiry" , expiry);
                    result.put("refresh_token" , refreshToken);

                    return ok("login successfull!!" + result );
                }

                return unauthorized("incorrect password");
            }

            return unauthorized("login unsuccessful!!");

        }


        @Transactional
        @Authenticator
        public Result getCurrentUser() {

            LOGGER.debug("Get current user");

            final JsonNode user = (JsonNode) ctx().args.get("user");

            LOGGER.debug("User: {}", user);

            return ok(user);
        }

        @Transactional
        public Result getAllUsers() {

            final List<User> users = userDao.findAll();

            final JsonNode jsonNode1 = Json.toJson(users);

            return ok(jsonNode1);
        }

        /*@Transactional
        public Result resetPassword() throws NoSuchAlgorithmException {

            final JsonNode jsonNode = request().body().asJson();
            final String email = jsonNode.get("email").asText();
            final String newPassword = jsonNode.get("password").asText();

            if (null == email) {
                return badRequest("Missing email");
            }

            if (null == newPassword) {
                return badRequest("Missing password");
            }

            final JsonNode user = userDao.getUser(email);

            if (null != user) {

                String salt = user.findValue("salt").asText();
                String hashedPassword = userDao.hashedPassword(newPassword, salt, 30);

                userDao.updatePassword(hashedPassword, email);

                return ok("Changed the password");

            }

            return badRequest();
        }*/

        @Transactional
        public Result resetAccessToken(){

            final JsonNode jsonNode = request().body().asJson();
            final String refreshToken = jsonNode.get("refresh_token").asText();

            User user = userDao.findRefreshToken(refreshToken);

            if( user.getRefreshToken().equals(refreshToken) ){

                String accessToken = userDao.generateToken();
                user.setToken(accessToken);

                JsonNode json = Json.toJson(userDao.persist(user));

                return ok(json);

            }

            return badRequest();
        }

        @Transactional
        @Authenticator
        public Result updateRole(){

            final JsonNode user = (JsonNode) ctx().args.get("user");



            return ok();
        }
    }
