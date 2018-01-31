    package controllers;

            import com.fasterxml.jackson.databind.JsonNode;
            import com.google.inject.Inject;
            import controllers.security.Authenticator;
            import dao.UserDao;
            import models.User;
            import play.Logger;
            import play.db.jpa.Transactional;
            import play.libs.Json;
            import play.mvc.Controller;
            import play.mvc.Result;

            import java.util.List;


    public class LoginController extends Controller {

        private final static Logger.ALogger LOGGER = Logger.of(LoginController.class);


        private UserDao userDao;

        @Inject
        public LoginController(UserDao userDao) {
            this.userDao = userDao;
        }


        @Transactional
        public Result login() {

            final JsonNode jsonNode = request().body().asJson();
            final String userName = jsonNode.get("name").asText();
            final String pwd = jsonNode.get("password").asText();

            if (null == userName) {
                return badRequest("Missing userName");
            }

            if (null == pwd) {
                return badRequest("Missing password");
            }


            final List<User> result = userDao.getUser(userName);
            LOGGER.debug("Got result");

            if( result.size() == 1 ) {

                User user = new User();
                final JsonNode json = Json.toJson(result);

                if ( json.findValue("password").asText().equals(pwd))  {

                    String token = userDao.generateToken(userName);
                    user.setToken(token);


                    return ok("login successful!! " + " Token: " + user.getToken().toString());
                }

                return unauthorized("incorrect password" );
            }



            return unauthorized("login unsuccessful!!");

        }

        @Authenticator
        public Result getCurrentUser() {

            LOGGER.debug("Get current user");

            final User user = (User) ctx().args.get("user");

            LOGGER.debug("User: {}", user);

            final JsonNode json = Json.toJson(user);

            return ok(json);
        }

    }

