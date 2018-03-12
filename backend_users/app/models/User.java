            package models;
            import com.fasterxml.jackson.annotation.*;
            import javax.persistence.*;
            import java.util.List;

            @Entity
            public class User {

                public enum Role {
                  Admin, Client, User
                }

                @Id
                String userName;

                @Basic
                String password;

                @Basic
                String email;

                @Basic
                Role role;

                @Basic
                @JsonProperty("access_token")
                private String token;

                @Basic
                @JsonProperty("token_expiry")
                private Long tokenExpire;

                @Basic
                @JsonProperty("refresh_token")
                private String refreshToken;

                @Basic
                private String salt;

                @JsonManagedReference
                @OneToMany(mappedBy = "owner")
                private List<Restaurant> restaurants;

               @Basic
               String imagePath;

               @Basic
               String likes;

               @Basic
               long contact;

                public User() {

                }

                public User (String userName){
                    this.userName = userName;
                }

                public User(String userName, List<Restaurant> restaurants) {
                    this.userName = userName;
                    this.restaurants = restaurants;
                }

                public String getUserName() {
                    return userName;
                }

                public void setUserName(String userName) {
                    this.userName = userName;
                }

                public String getPassword() {
                    return password;
                }

                public void setPassword(String password) {
                    this.password = password;
                }

                public String getEmail() {
                    return email;
                }

                public void setEmail(String email) {
                    this.email = email;
                }

                //@JsonIgnore
                public Role getRole() {
                    return role;
                }

                public void setRole(Role role) {
                    this.role = role;
                }

                public String getToken() {
                    return token;
                }

                public void setToken(String token) {
                    this.token = token;
                }

                public Long getTokenExpire() {
                    return tokenExpire;
                }

                public void setTokenExpire(Long tokenExpire) {
                    this.tokenExpire = tokenExpire;
                }

                public String getRefreshToken() {
                    return refreshToken;
                }

                public void setRefreshToken(String refreshToken) {
                    this.refreshToken = refreshToken;
                }

                public String getSalt() {
                    return salt;
                }

                public void setSalt(String salt) {
                    this.salt = salt;
                }

                public List<Restaurant> getRestaurants() {
                    return restaurants;
                }

                public void setRestaurants(List<Restaurant> restaurants) {
                    this.restaurants = restaurants;
                }


                public String getLikes() {
                    return likes;
                }

                public void setLikes(String likes) {
                    this.likes = likes;
                }

                public long getContact() {
                    return contact;
                }

                public void setContact(long contact) {
                    this.contact = contact;
                }
                public String getImagePath() {
                    return imagePath;
                }

                public void setImagePath(String imagePath) {
                    this.imagePath = imagePath;
                }
            }
