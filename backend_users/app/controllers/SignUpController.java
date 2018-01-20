package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import dao.SignUpDaoImpl;
import models.Users;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;

public class SignUpController extends Controller {

    private SignUpDaoImpl signUpDao;

    @Inject
    public SignUpController(SignUpDaoImpl signUpDao) {
        this.signUpDao = signUpDao;
    }

    @Transactional
    public Result createUser() {

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

        if (null == password ) {
            return badRequest("Missing password");
        }



        Users user = new Users();
        user.setUserName(userName);
        user.setPassword(password);
        user.setEmail(email);

        switch (role) {

            case "Client" :
                    user.setRole(2);
                    break;

            case "User" :
                user.setRole(1);
                break;

            case "Admin" :
                user.setRole(0);
                break;

            default: return badRequest("misssing role");

        }


        user = signUpDao.persist(user);


        return created(user.getUserName().toString());
    }

    @Transactional
    public Result getAllUsers() {

        final List<Users> users = signUpDao.findAll();

        final JsonNode jsonNode = Json.toJson(users);

        return ok(jsonNode);
    }




}