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
                final String type = jsonNode.get("type").asText();
                final String cuisine = jsonNode.get("cuisine").asText();
                final String address = jsonNode.get("address").asText();
                final String area = jsonNode.get("area").asText();
                final Long contact = jsonNode.get("contact").asLong();
                final String timings = jsonNode.get("timings").asText();
                final String hpUrl = jsonNode.get("hpUrl").asText();
                final String fbUrl = jsonNode.get("fbUrl").asText();
                final Integer cost = jsonNode.get("cost").asInt();
                final Double latitude = jsonNode.get("lat").asDouble();
                final Double longitude = jsonNode.get("lng").asDouble();
                //final String imagePath = jsonNode.get("path").asText();

                if (null == name) {
                    return badRequest("Missing restaurant name");
                }

                if (null == address) {
                    return badRequest("Missing address");
                }

                if (null == area) {
                    return badRequest("Missing area");
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
                restaurant.setType(type);
                restaurant.setCuisine(cuisine);
                restaurant.setContact(contact);
                restaurant.setAddress(address);
                restaurant.setArea(area);
                restaurant.setTimings(timings);
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
                LOGGER.debug("inside getAllClientRestaurants");

                final User user = (User) ctx().args.get("user");

                final List<Restaurant> clients = restaurantDao.findByOwner(user);
                LOGGER.debug(String.valueOf(clients));

                if(clients.isEmpty()){

                    return badRequest("no restaurants to display!!");
                }

                final JsonNode restaurants= Json.toJson(clients);
                LOGGER.debug(String.valueOf(restaurants));

                return ok(restaurants);
            }

            @Transactional
            @Authenticator
            public Result updateRestaurant() {

                LOGGER.debug("inside updateRestaurant");

                final JsonNode jsonNode = request().body().asJson();
                final String name = jsonNode.get("name").asText();
                final String type = jsonNode.get("type").asText();
                final String cuisine = jsonNode.get("cuisine").asText();
                final String address = jsonNode.get("address").asText();
                final Long contact = jsonNode.get("contact").asLong();
                final String timings = jsonNode.get("timings").asText();
                final String hpUrl = jsonNode.get("hpUrl").asText();
                final String fbUrl = jsonNode.get("fbUrl").asText();
                final Integer cost = jsonNode.get("cost").asInt();
                LOGGER.debug("after taking input");

                Restaurant restaurant = restaurantDao.getRestaurant(name,address);
                restaurant.setType(type);
                restaurant.setCuisine(cuisine);
                restaurant.setContact(contact);
                restaurant.setTimings(timings);
                restaurant.setHomepageUrl(hpUrl);
                restaurant.setFbUrl(fbUrl);
                restaurant.setCost(cost);
                restaurant =restaurantDao.persist(restaurant);

                com.fasterxml.jackson.databind.node.ObjectNode json = Json.newObject();
                json.put("name",restaurant.getName());
                json.put("type", restaurant.getType());
                json.put("cuisine",restaurant.getCuisine());
                json.put("address",restaurant.getAddress());
                json.put("area",restaurant.getArea());
                json.put("contact",restaurant.getContact());
                json.put("timings",restaurant.getTimings());
                json.put("hpUrl",restaurant.getHomepageUrl());
                json.put("fbUrl",restaurant.getFbUrl());
                json.put("cost",restaurant.getCost());

                return ok(json);
            }

            @Transactional
            @Authenticator
            public Result getRestaurantById(int Id){

                LOGGER.debug("inside getRestaurantById" + Id);

                Restaurant restaurant = restaurantDao.getRestaurantById(Id);
                JsonNode json = Json.toJson(restaurant);
                LOGGER.debug(String.valueOf(json));

                return ok(json);
            }


            @Transactional
            public Result getRestaurantsBySearch() {
                LOGGER.debug("inside getRestaurantBySearch") ;

                JsonNode jsonNode = request().body().asJson();
                final String searchField = jsonNode.get("search").asText();

                List<Restaurant> restaurants =restaurantDao.searchRestaurant(searchField);
                if (null == restaurants){
                    return badRequest();
                }

                JsonNode json = Json.toJson(restaurants);

                return ok(json);
            }
        }


