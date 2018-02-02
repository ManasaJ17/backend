package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import dao.UserDao;
import models.User;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class SignUpController extends Controller {

    private UserDao userDao;

    @Inject
    public SignUpController(UserDao userDao) {
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
                    user.setRole(2);
                    break;

                case "User":
                    user.setRole(1);
                    break;

                case "Admin":
                    user.setRole(0);
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
    public Result getAllUsers() {

        final List<User> users = userDao.findAll();

        final JsonNode jsonNode1 = Json.toJson(users);

        return ok(jsonNode1);
    }
}




