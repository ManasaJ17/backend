            package dao;


            import com.google.inject.Inject;
            import models.User;
            import org.apache.commons.lang3.RandomStringUtils;
            import play.db.jpa.JPAApi;
            import play.db.jpa.Transactional;
            import views.html.helper.input;

            import javax.persistence.Query;
            import javax.persistence.TypedQuery;
            import java.nio.charset.StandardCharsets;
            import java.security.MessageDigest;
            import java.security.NoSuchAlgorithmException;
            import java.security.SecureRandom;
            import java.sql.Timestamp;
            import java.util.List;
            import java.util.Random;

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

                    String str = "SELECT u FROM User u WHERE u.userName = :name";
                    TypedQuery<User> query = jpaApi.em().createQuery(str, User.class);
                    query.setParameter("name", userName);

                    List<User> result = query.getResultList();

                    return result;
                }

                public User generateToken(String userName) {


                    SecureRandom random = new SecureRandom();

                    User user = jpaApi.em().find(User.class,userName);

                    long longToken = Math.abs( random.nextLong() );
                    String token = Long.toString( longToken, 16 );
                    user.setToken(token);


                    Timestamp timestamp = new Timestamp(System.currentTimeMillis() + (12*60*60*1000));
                    Long expiry = timestamp.getTime() ;
                    user.setTokenExpire(expiry);


                    long refToken = Math.abs( random.nextLong() );
                    final String refreshToken = Long.toString( refToken, 16 );
                    user.setRefreshToken(refreshToken);

                    return user;

                }

                public List<User> findAccessToken(String accessToken){

                    String str = "SELECT NEW models.User(u.token) FROM User AS u WHERE u.token = :token OR u.refreshToken= :token";
                    TypedQuery<User> query = jpaApi.em().createQuery(str, User.class);
                    query.setParameter("token", accessToken);

                    return query.getResultList();

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




            }