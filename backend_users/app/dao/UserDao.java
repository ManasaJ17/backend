    package dao;


    import com.fasterxml.jackson.databind.JsonNode;
    import com.google.inject.Inject;
    import models.User;
    import org.apache.commons.lang3.RandomStringUtils;
    import play.db.jpa.JPAApi;
    import play.db.jpa.Transactional;
    import play.libs.Json;

    import javax.persistence.EntityManager;
    import javax.persistence.Query;
    import javax.persistence.TypedQuery;
    import java.util.List;
    import java.util.Random;

    public class UserDao implements SignUpDao<User> {


        private JPAApi jpaApi;


        @Inject
        public UserDao(JPAApi jpaApi) { this.jpaApi = jpaApi; }




        public List<User> getUserByAccessToken(String token) {


            TypedQuery<User> query = jpaApi.em().createQuery("SELECT u FROM User u WHERE u.token = :token", User.class);
            query.setParameter("token", token);

            List<User> result1 = query.getResultList();

            return result1;


        }


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

            String str = "SELECT u FROM User u WHERE u.userName = :name";
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