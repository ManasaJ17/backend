    package models;

    import com.fasterxml.jackson.annotation.JsonIgnore;
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
        private String token;


        public User(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

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

        public void setToken(String token) {
            this.token = token;



        }
    }
