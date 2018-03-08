        package controllers.security;

        import com.google.inject.Inject;
        import dao.UserDao;
        import models.User;
        import play.Logger;
        import play.mvc.Action;
        import play.mvc.Http;
        import play.mvc.Result;
        import java.sql.Timestamp;
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

                User user = userDao.findAccessToken(accessToken);
                if (null == user) {
                    return CompletableFuture.completedFuture(unauthorized());
                }

                Long currentTime = new Timestamp(System.currentTimeMillis()).getTime();
                if(currentTime > user.getTokenExpire()) {
                    LOGGER.debug("time expired");
                    return CompletableFuture.completedFuture(unauthorized());
                }

                LOGGER.debug("User: {}", user);

                ctx.args.put("user", user);

                return delegate.call(ctx);
            }

        }