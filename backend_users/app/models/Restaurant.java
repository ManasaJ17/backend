package models;


import javax.persistence.*;

@Entity
public class Restaurant {

    public enum ApproveStatus {
        New, Approved, Rejected
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer Id;

    @Basic
    String name;

    @Basic
    String type;

    @Basic
    String address;

    @Basic
    Long contact;

    @Basic
    String homepageUrl;

    @Basic
    String fbUrl;

    @Basic
    Integer cost;

    @Basic
    ApproveStatus status;

    @OneToOne
    private User owner;

    //Boolean isReviewed;
    
    //Boolean isApproved;


    
    public Restaurant() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getContact() {
        return contact;
    }

    public void setContact(Long contact) {
        this.contact = contact;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public void setHomepageUrl(String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }

    public String getFbUrl() {
        return fbUrl;
    }

    public void setFbUrl(String fbUrl) {
        this.fbUrl = fbUrl;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public ApproveStatus getStatus() {
        return status;
    }

    public void setStatus(ApproveStatus status) {
        this.status = status;
    }


    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
