package dao;

import models.Users;

import java.util.List;

public interface SignUpDao<E>  {

    E persist(E entity);

    List<Users> findAll();

}