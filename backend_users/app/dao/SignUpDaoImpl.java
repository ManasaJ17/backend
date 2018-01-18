package dao;

import com.google.inject.Inject;
import models.Users;
import play.db.jpa.JPAApi;


import javax.persistence.TypedQuery;
import java.util.List;

public class SignUpDaoImpl implements SignUpDao<Users> {

    private JPAApi jpaApi;

       @Inject
    public SignUpDaoImpl(JPAApi jpaApi) { this.jpaApi = jpaApi; }

    public Users persist(Users user) {

        jpaApi.em().persist(user);

        return user;
    }

    public List<Users> findAll() {

        TypedQuery<Users> query = jpaApi.em().createQuery("SELECT u FROM Users u", Users.class);
        List<Users> users = query.getResultList();

        return users;
    }
}