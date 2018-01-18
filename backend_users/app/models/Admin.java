package models;

import javax.persistence.*;

@Entity
public class Admin{

    @Id
    String userName;

    @Basic
    String password;

    @Basic
    String email;


    public Admin(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public Admin() {

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
}