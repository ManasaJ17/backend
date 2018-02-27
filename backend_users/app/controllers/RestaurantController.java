package controllers;

        import com.fasterxml.jackson.databind.JsonNode;
        import controllers.security.Authenticator;
        import dao.RestaurantDao;
        import dao.UserDao;
        import models.Restaurant;
        import models.User;
        import play.Logger;
        import play.db.jpa.Transactional;
        import play.libs.Json;
        import play.mvc.Controller;
        import play.mvc.Result;
        import javax.inject.Inject;
        import java.util.ArrayList;
        import java.util.List;


        public class RestaurantController extends Controller {

            private RestaurantDao restaurantDao;
            private UserDao userDao;

            @Inject
            public RestaurantController(RestaurantDao restaurantDao, UserDao userDao) {
                this.restaurantDao = restaurantDao;
                this.userDao = userDao;
            }

            private final static Logger.ALogger LOGGER = Logger.of(RestaurantController.class);

            @Transactional
            @Authenticator
            public Result createRestaurant()  {

                LOGGER.debug("inside createRestaurant");

                final User user = (User) ctx().args.get("user");

                final JsonNode jsonNode = request().body().asJson();
                final String name = jsonNode.get("name").asText();
                //final String type = jsonNode.get("type").asText();
                final String address = jsonNode.get("address").asText();
                final Long contact = jsonNode.get("contact").asLong();
                final String hpUrl = jsonNode.get("hpUrl").asText();
                final String fbUrl = jsonNode.get("fbUrl").asText();
                final Integer cost = jsonNode.get("cost").asInt();
                final Double latitude = jsonNode.get("lat").asDouble();
                final Double longitude = jsonNode.get("lng").asDouble();

                if (null == name) {
                    return badRequest("Missing restaurant name");
                }

                if (null == address) {
                    return badRequest("Missing address");
                }

                if (null == contact) {
                    return badRequest("Missing contact");
                }

                if (null == hpUrl) {
                    return badRequest("Missing HomePage URL");
                }

                if (null == cost) {
                    return badRequest("Missing Cost");
                }

                if (null == latitude){
                    return badRequest("Missing Latitude");
                }

                if (null == longitude){
                    return badRequest("Missing Longitude");
                }

                boolean doesExists = restaurantDao.checkRestaurant(name , address);
                if (doesExists) {
                    return badRequest("Restaurant already registered");
                }

                Restaurant restaurant = new Restaurant();
                restaurant.setName(name);
                restaurant.setType("foodtruck");
                restaurant.setContact(contact);
                restaurant.setAddress(address);
                restaurant.setHomepageUrl(hpUrl);
                restaurant.setFbUrl(fbUrl);
                restaurant.setCost(cost);
                restaurant.setStatus(Restaurant.ApproveStatus.New);
                restaurant.setLatitude(latitude);
                restaurant.setLongitude(longitude);
                restaurant.setOwner(user);
                restaurant = restaurantDao.persist(restaurant);

                if (user.getRestaurants().isEmpty()){

                       LOGGER.debug("inside null res list");
                       List<Restaurant> res = new ArrayList<>();
                       res.add(restaurant);
                       LOGGER.debug("added res list");
                       user.setRestaurants(res);
                }
                else {

                       LOGGER.debug("inside else res");
                       user.getRestaurants().add(restaurant);
                }

                userDao.persist(user);
                LOGGER.debug("created res");


                com.fasterxml.jackson.databind.node.ObjectNode json = Json.newObject();
                json.put("name", restaurant.getName());

                return created(json);


            }

            @Transactional
            public Result UpdateRestaurant() {
                return ok();
            }

            @Transactional
            public Result deleteRestaurant() {
                return ok();
            }

            @Transactional
            public Result getAllRestaurants() {

                final List<Restaurant> clients = restaurantDao.findAll();

                final JsonNode jsonNode1 = Json.toJson(clients);

                return ok(jsonNode1);
            }

            @Transactional
            @Authenticator
            public Result getClientRestaurants(){

                final User user = (User) ctx().args.get("user");

                final List<Restaurant> clients = restaurantDao.findByOwner(user);

                if(clients.isEmpty()){

                    return badRequest("no restaurants to display!!");
                }

                final JsonNode jsonNode1 = Json.toJson(clients);

                return ok(jsonNode1);
            }

        }
