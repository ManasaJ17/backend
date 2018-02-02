        package models;

        import com.fasterxml.jackson.annotation.JsonIgnore;
        import com.fasterxml.jackson.annotation.JsonProperty;

        import javax.persistence.*;

        @Entity
        public class User {

            @Id
            String userName;

            @Basic
            String password;

            @Basic
            String email;

            @Basic
            Integer role;

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


            public User(String token) {
                this.token = token;
            }


            public User() {

            }

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }

           //@JsonIgnore
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

            @JsonIgnore
            public Integer getRole() {
                return role;
            }

            public void setRole(Integer role) {
                this.role = role;
            }

           @JsonIgnore
            public String getToken() {
                return token;
            }

            public void setToken(String token) {  this.token = token; }

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
        }
