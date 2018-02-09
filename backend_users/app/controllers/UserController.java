package controllers;

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
        final String role = jsonNode.get("role").asText();


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

        switch (role) {

            case "Client":
                user.setRole(User.Role.Client);
                break;

            case "User":
                user.setRole(User.Role.User);
                break;

            case "Admin":
                user.setRole(User.Role.Admin);
                break;

            default:
                return badRequest("missing role");

        }

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


        final JsonNode user = userDao.getUser(userName);
        LOGGER.debug("Got result");


        if( null != user) {

            String salt = user.findValue("salt").asText();

            String hashedPassword = userDao.hashedPassword(pwd, salt, 30);

            if (user.findValue("password").asText().equals(hashedPassword))  {

                userDao.generateToken(userName, 1);

                return ok("login successful!! ");
            }

            return unauthorized("incorrect password" );
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

        JsonNode user = userDao.findRefreshToken(refreshToken);

        if( user.findValue("refresh_token").asText().equals(refreshToken) ){

            User user1 = userDao.generateToken(user.findValue("userName").asText(), 0);
            JsonNode json  = Json.toJson(user1);

            return ok(json);

        }

        return badRequest();
    }
}
