package dao;


import com.google.inject.Inject;
import models.User;
import org.apache.commons.lang3.RandomStringUtils;
import play.db.jpa.JPAApi;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class UserDao implements SignUpDao<User> {


    private JPAApi jpaApi;

    @Inject
    public UserDao(JPAApi jpaApi) { this.jpaApi = jpaApi; }


    public User persist(User user) {

        jpaApi.em().persist(user);

        return user;
    }

    public List<User> findAll() {

        TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u", User.class);
        List<User> users = query.getResultList();

        return users;
    }

    public List<User> getUser(String userName) {

        String str = "SELECT u"+ " FROM User u WHERE u.userName = :name";
        TypedQuery<User> query = jpaApi.em().createQuery(str, User.class);
        query.setParameter("name", userName);

        List<User> result = query.getResultList();

        return result;
    }

    public String generateToken(String userName) {

        String token= RandomStringUtils.randomAlphanumeric(22);

        User b = jpaApi.em().find(User.class,userName);
        b.setToken(token);
        return token;
    }


}