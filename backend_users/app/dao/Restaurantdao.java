    package dao;

    import com.google.inject.Inject;
    import models.Restaurant;
    import models.User;
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

        public List<Restaurant> findByOwner(User owner) {

            TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT r FROM Restaurant r WHERE r.owner = :owner ", Restaurant.class);
            query.setParameter("owner", owner);
            List<Restaurant> restaurants = query.getResultList();

            return restaurants;
        }

        public boolean checkRestaurant (String name, String address){

            TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT r FROM Restaurant r WHERE r.name = :name AND r.address = :address ", Restaurant.class);
            query.setParameter("name", name);
            query.setParameter("address", address);
            List<Restaurant> restaurants = query.getResultList();

            if(restaurants.isEmpty()){
                return false;
            }

            return true;
        }


    }
