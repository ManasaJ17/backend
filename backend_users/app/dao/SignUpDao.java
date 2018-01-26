package dao;

import models.User;

import java.util.List;

public interface SignUpDao<E>  {

    E persist(E entity);

    List<User> findAll();

}