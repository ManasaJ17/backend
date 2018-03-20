    package dao;

    import com.google.inject.Inject;
    import controllers.RestaurantController;
    import models.Restaurant;
    import models.User;
    import play.Logger;
    import play.db.jpa.JPAApi;
    import play.libs.F;
    import javax.persistence.TypedQuery;
    import java.util.ArrayList;
    import java.util.List;


    import static java.lang.Math.*;

    public class RestaurantDao implements SignUpDao<Restaurant> {

        private JPAApi jpaApi;

        @Inject
        public RestaurantDao(JPAApi jpaApi) { this.jpaApi = jpaApi; }

        private final static Logger.ALogger LOGGER = Logger.of(RestaurantController.class);



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

            TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT r FROM Restaurant r WHERE r.owner = :owner AND r.status NOT LIKE :status ", Restaurant.class);
            query.setParameter("owner", owner);
            query.setParameter("status", Restaurant.ApproveStatus.Rejected);
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

        public Restaurant getRestaurant(String name, String address){

            TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT r FROM Restaurant r WHERE r.name = :name AND r.address = :address ", Restaurant.class);
            query.setParameter("name", name);
            query.setParameter("address", address);
            List<Restaurant> result = query.getResultList();

            Restaurant restaurant = result.get(0);

            return (restaurant);

        }

        public Restaurant getRestaurantById(int id){
            TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT r FROM Restaurant r WHERE r.Id = :id ", Restaurant.class);
            query.setParameter("id", id);
            List<Restaurant> result = query.getResultList();
            Restaurant restaurant = result.get(0);

            return (restaurant);
        }


        public List<Restaurant> searchRestaurant(String search) {


            TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT r FROM Restaurant r WHERE  r.status = :status AND (r.area = :search OR r.name = :search) ", Restaurant.class);
            query.setParameter("search", search);
            query.setParameter("status", Restaurant.ApproveStatus.Approved);
            List<Restaurant> result = query.getResultList();

           if (result.isEmpty()){
               return null;
           }
           return result;

        }

        public List<F.Tuple<Restaurant, Double>> nearByRestaurants(Double latitude, Double longitude ){

            Double distance;
            F.Tuple<Restaurant, Double> tuple ;
            List<F.Tuple<Restaurant, Double>> result = new ArrayList<>();

            TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT r FROM Restaurant r WHERE (SQRT(POW(r.latitude - :latitude,2) + POW(r.longitude - :longitude,2))) < 0.045 AND r.status = :status "
                            , Restaurant.class);
            query.setParameter("latitude", latitude);
            query.setParameter("longitude", longitude);
            query.setParameter("status", Restaurant.ApproveStatus.Approved);
            List<Restaurant> restaurants = query.setMaxResults(10).getResultList();
            LOGGER.debug(String.valueOf(restaurants));

            if (restaurants.isEmpty()){
                return null;
            }

            for (int i=0; i< restaurants.size(); i++) {
                LOGGER.debug("inside for");

                Restaurant restaurant = restaurants.get(i);

                distance = (Math.sqrt((pow(restaurant.getLatitude() - latitude,2) + pow(restaurant.getLongitude() - longitude,2))) * 111);

                tuple = new F.Tuple(restaurant, String.format("%.2f",distance));
                result.add(tuple);
            }


            LOGGER.debug("at return "+ String.valueOf(result));
            return result;

        }

        public List<Restaurant> popularRestaurants() {

            TypedQuery<Restaurant> query = jpaApi.em().createQuery("SELECT r FROM Restaurant r WHERE  r.status = :status", Restaurant.class);
            query.setParameter("status", Restaurant.ApproveStatus.Approved);

            LOGGER.debug("inside popularDao");
            List<Restaurant> restaurants = query.setMaxResults(8).getResultList();
            LOGGER.debug(restaurants.toString());

            return restaurants;
        }

        public F.Tuple<String,List<Restaurant>> getRestaurantsByLikes(String like){

            F.Tuple<String,List<Restaurant>> tuple;

            TypedQuery<Restaurant> query = jpaApi.em().createQuery("select r from Restaurant r  WHERE  (SUBSTRING_INDEX(r.cuisine, ',' , 1 ) = :like  " +
                    "OR  SUBSTRING_INDEX(r.cuisine, ',', -1 ) = :like OR " +
                    " SUBSTRING_INDEX(SUBSTRING_INDEX(r.cuisine, ',', r.cuisineCount-1 ),',',-1)= :like OR " +
                    "SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(r.cuisine, ',', r.cuisineCount-1 ),',',-2),',',1)= :like OR " +
                    "SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(r.cuisine, ',', r.cuisineCount-1 ),',',-3),',',1)= :like) AND r.status = :status " ,Restaurant.class);

            query.setParameter("like",like);
            query.setParameter("status",Restaurant.ApproveStatus.Approved);

            List<Restaurant> restaurants = query.setMaxResults(8).getResultList();

            tuple = new  F.Tuple(like, restaurants);

            return tuple;

        }
    }
