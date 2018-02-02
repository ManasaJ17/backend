    package controllers.security;

    import com.fasterxml.jackson.databind.JsonNode;
    import com.google.inject.Inject;
    import dao.UserDao;
    import models.User;
    import play.Logger;
    import play.db.jpa.JPAApi;
    import play.db.jpa.Transactional;
    import play.libs.Json;
    import play.mvc.Action;
    import play.mvc.Http;
    import play.mvc.Result;

    import javax.persistence.Basic;
    import javax.persistence.Query;
    import javax.persistence.TypedQuery;
    import java.sql.Timestamp;
    import java.util.List;
    import java.util.Optional;
    import java.util.concurrent.CompletableFuture;
    import java.util.concurrent.CompletionStage;

    public class AuthenticatorAction extends Action.Simple {

        private final static Logger.ALogger LOGGER = Logger.of(AuthenticatorAction.class);

        private UserDao userDao;

        @Inject
        public AuthenticatorAction(UserDao userDao) { this.userDao = userDao; }


        public CompletionStage<Result> call(Http.Context ctx) {

            LOGGER.debug("AuthenticatorAction");


            final Optional<String> header = ctx.request().header("Authorization");
            LOGGER.debug("Header: {}", header);
            if (!header.isPresent()) {
                return CompletableFuture.completedFuture(unauthorized());
            }


            if (!header.get().startsWith("Bearer ")) {
                return CompletableFuture.completedFuture(unauthorized());
            }

            final String accessToken = header.get().substring(7);
            if (accessToken.isEmpty()) {
                return CompletableFuture.completedFuture(unauthorized());
            }
            LOGGER.debug("Access token: {}", accessToken);

            List <User> user = userDao.findAccessToken(accessToken);
            if (user.size() != 1) {
                return CompletableFuture.completedFuture(unauthorized());
            }

            final JsonNode jsonNode1 = Json.toJson(user);

            /*if( accessToken == jsonNode1.findValue("refreshToken").asText() ){

                User user1 = userDao.generateToken(jsonNode1.findValue("userName").asText());
                final JsonNode json = Json.toJson(user);


            }*/

            LOGGER.debug("User: {}", user);

            ctx.args.put("user", user);

            return delegate.call(ctx);
        }

    }