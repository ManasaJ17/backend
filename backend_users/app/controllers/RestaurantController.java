    package controllers;

    import com.fasterxml.jackson.databind.JsonNode;
    import controllers.security.Authenticator;
    import dao.RestaurantDao;
    import models.Restaurant;
    import models.User;
    import play.Logger;
    import play.db.jpa.Transactional;
    import play.libs.Json;
    import play.mvc.Controller;
    import play.mvc.Result;

    import javax.inject.Inject;

    import java.util.List;


    public class RestaurantController extends Controller {

        private RestaurantDao restaurantDao;

        @Inject
        public RestaurantController(RestaurantDao restaurantDao) {
            this.restaurantDao = restaurantDao;
        }

        private final static Logger.ALogger LOGGER = Logger.of(RestaurantController.class);



        @Transactional
        @Authenticator
        public Result createRestaurant() {

            LOGGER.debug("in createRestaurant");

            final JsonNode json = (JsonNode) ctx().args.get("user");
            final JsonNode jsonNode = request().body().asJson();
            final String name = jsonNode.get("name").asText();
            final String type = jsonNode.get("type").asText();
            final String address = jsonNode.get("address").asText();
            final Long contact = jsonNode.get("contact").asLong();
            final String hpUrl = jsonNode.get("hpUrl").asText();
            final String fbUrl = jsonNode.get("fbUrl").asText();
            final Integer cost = jsonNode.get("cost").asInt();



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

            Restaurant restaurant =  new Restaurant();
            restaurant.setName(name);
            restaurant.setType(type);
            restaurant.setContact(contact);
            restaurant.setAddress(address);
            restaurant.setHomepageUrl(hpUrl);
            restaurant.setFbUrl(fbUrl);
            restaurant.setCost(cost);
            restaurant.setStatus(Restaurant.ApproveStatus.New);

            User owner = Json.fromJson(json.findValue("userName"), User.class);
            restaurant.setOwner(owner);


            restaurant = restaurantDao.persist(restaurant);


            return created( restaurant.getName().toString() + "  will be reviewed");
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
        @Authenticator
        public Result getAllRestaurants() {

            final List<Restaurant> clients = restaurantDao.findAll();

            final JsonNode jsonNode1 = Json.toJson(clients);

            return ok(jsonNode1);
        }

    }
