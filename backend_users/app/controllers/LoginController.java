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
        final String pwd = jsonNode.get("password").asText();


        if (null == userName) {
            return badRequest("Missing userName");
        }

        if (null == pwd) {
            return badRequest("Missing password");
        }

        String str = "SELECT u"+ " FROM Users u WHERE u.userName = :name";
        TypedQuery<Users> query = jpaApi.em().createQuery(str, Users.class);
        query.setParameter("name", userName);

        List<Users> result = query.getResultList();
        final JsonNode json = Json.toJson(result);

        if( result.size() == 1 ) {

            Users user = new Users();

            if ( json.findValue("password").asText().equals(pwd))  {

                user.setToken();

                return ok("login successful!! " + " Token: " + user.getToken().toString());
            }

            return unauthorized("incorrect password" );
        }



        return unauthorized("login unsuccessful!!");



    }
}

