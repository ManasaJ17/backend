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

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AuthenticatorAction extends Action.Simple {

    private final static Logger.ALogger LOGGER = Logger.of(AuthenticatorAction.class);

    private UserDao userDao;
    private JPAApi jpaApi;

    @Inject
    public AuthenticatorAction(UserDao userDao) {
        this.userDao = userDao;
    }



    @Override
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

      /*final User user = jpaApi.withTransaction(entityManager -> {

            Query query = entityManager.createNativeQuery("SELECT NEW models.Users(u.token) FROM User AS u WHERE u.token = :token ");
          query.setParameter("token", accessToken);


          List<User> user1= query.getResultList();



           return new User();

        });
        */


      final List<User> user = userDao.getUserByAccessToken(accessToken);


        if (user.size() != 1) {

            return CompletableFuture.completedFuture(unauthorized());
        }


        LOGGER.debug("User: {}", user);

        ctx.args.put("user", user);

        return delegate.call(ctx);
    }

}