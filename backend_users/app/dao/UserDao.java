package dao;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.User;
import play.db.jpa.JPAApi;
import play.libs.Json;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
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

    public  JsonNode getUser(String name) {

        String str = "SELECT u FROM User u WHERE u.userName = :name OR u.email = :name";
        TypedQuery<User> query = jpaApi.em().createQuery(str, User.class);
        query.setParameter("name", name);

        List<User> result = query.getResultList();
        if( result.size() == 1 ) {

            final JsonNode json = Json.toJson(result);
            return  json;
        }

        return null;
    }

    public User generateToken(String userName, int flag) {


        SecureRandom random = new SecureRandom();

        User user = jpaApi.em().find(User.class,userName);

        long longToken = Math.abs( random.nextLong() );
        String token = Long.toString( longToken, 16 );
        user.setToken(token);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis() + (12*60*60*1000));
        Long expiry = timestamp.getTime() ;
        user.setTokenExpire(expiry);

        if(flag == 1) {
            long refToken = Math.abs(random.nextLong());
            final String refreshToken = Long.toString(refToken, 16);
            user.setRefreshToken(refreshToken);
        }

        return user;
    }

    public JsonNode findAccessToken(String accessToken){

        String str = "SELECT u FROM User AS u WHERE u.token = :token";
        TypedQuery<User> query = jpaApi.em().createQuery(str, User.class);
        query.setParameter("token", accessToken);

        List<User> result = query.getResultList();
        if( result.size() == 1 ) {

            final JsonNode json = Json.toJson(result);
            return  json;
        }

        return null;
    }

    public JsonNode findRefreshToken(String refreshToken){

        String str = "SELECT u FROM User AS u WHERE u.refreshToken = :token";
        TypedQuery<User> query = jpaApi.em().createQuery(str, User.class);
        query.setParameter("token", refreshToken);

        List<User> result = query.getResultList();
        if( result.size() == 1 ) {

            final JsonNode json = Json.toJson(result);
            return  json;
        }

        return null;
    }

    public String generateSalt(){

        SecureRandom random = new SecureRandom();

        Integer intSalt = Math.abs( random.nextInt() );
        String salt = Integer.toString( intSalt, 8 );

        return salt;
    }

    public String hashedPassword(String password, String salt, int iteration) throws NoSuchAlgorithmException {

        String saltPwd = salt.concat(password);

        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        final byte[] hash = mDigest.digest(saltPwd.getBytes(StandardCharsets.UTF_8));

        StringBuffer hexString = new StringBuffer();

        for(int i=0; i< iteration; i++){

            String hex = Integer.toHexString(0xff & hash[i] );
            hexString.append(hex);
        }
        return hexString.toString();
    }

  /* public void updatePassword(String newPassword, String email) {


        Query query = jpaApi.em().createQuery( "UPDATE User u SET u.password = :password WHERE u.email = :email");
        query.setParameter("password", newPassword);
        query.setParameter("", email);
        query.executeUpdate();


    }*/

}