package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;

@Entity
public class Users{

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


    public Users(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public Users(String userName) {
        this.userName = userName;
    }


    public Users () {

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

    public void setToken() {
        this.token = RandomStringUtils.randomAlphanumeric(22);
    }
}
