package dao;

import com.google.inject.Inject;
import models.Restaurant;
import play.db.jpa.JPAApi;

import javax.persistence.TypedQuery;
import java.util.List;

public class RestaurantDao implements SignUpDao<Restaurant> {

    private JPAApi jpaApi;

    @Inject
    public RestaurantDao(JPAApi jpaApi) { this.jpaApi = jpaApi; }



    @Override
    public Restaurant persist(Restaurant restaurant) {

        jpaApi.em().persist(restaurant);

        return restaurant;
    }

    @Override
    public List<Restaurant> findAll() {

        TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT r FROM Restaurant r", Restaurant.class);
        List<Restaurant> restaurants = query.getResultList();

        return restaurants;
    }
}
