package controllers;

        import com.fasterxml.jackson.databind.JsonNode;
        import com.google.inject.Inject;
        import models.Users;
        import play.db.jpa.JPAApi;
        import play.db.jpa.Transactional;
        import play.libs.Json;
        import play.mvc.Controller;
        import play.mvc.Result;
        import javax.persistence.TypedQuery;
        import java.util.List;


public class LoginController extends Controller {

    private JPAApi jpaApi;
    @Inject
    public LoginController(JPAApi jpaApi) { this.jpaApi = jpaApi; }

    @Transactional
    public Result login() {

        final JsonNode jsonNode = request().body().asJson();
        final String userName = jsonNode.get("name").asText();
        final String password = jsonNode.get("password").asText();


        if (null == userName) {
            return badRequest("Missing userName");
        }

        if (null == password) {
            return badRequest("Missing password");
        }

        String str = "SELECT NEW models.Users(u.userName, u.password)" + "FROM Users AS u WHERE u.userName = :name AND u.password = :password";

        TypedQuery<Users> query = jpaApi.em().createQuery(str, Users.class);
        query.setParameter("name", userName);
        query.setParameter("password", password);

          if( query.getResultList().size() == 1 ) {

              return ok("login successful!!");
          }

             return badRequest("login unsuccessful!!");



    }
}

