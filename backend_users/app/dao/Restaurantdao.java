package dao;


import com.google.inject.Inject;
import models.Restaurant;
import org.apache.commons.lang3.RandomStringUtils;
import play.db.jpa.JPAApi;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Random;

public class Restaurantdao implements SignUpDao<Restaurant> {


    private JPAApi jpaApi;

    @Inject
    public Restaurantdao(JPAApi jpaApi) { this.jpaApi = jpaApi; }


    public Restaurant persist(Restaurant res) {

        jpaApi.em().persist(res);

        return res;
    }

   public List<Restaurant> findAll() {

        TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT u FROM User u", Restaurant.class);
        List<Restaurant> users = query.getResultList();

        return users;
    }

    public List<Restaurant> getUser(String userName) {

        String str = "SELECT u"+ " FROM User u WHERE u.userName = :name";
        TypedQuery<Restaurant> query = jpaApi.em().createQuery(str, Restaurant.class);
        query.setParameter("name", userName);

        List<Restaurant> result = query.getResultList();

        return result;
    }

   /* public String generateToken(String userName) {

        String token= RandomStringUtils.randomAlphanumeric(22);


        Restaurant b = jpaApi.em().find(Restaurant.class,userName);
        b.setToken(token);

        return token;
    }*/


}