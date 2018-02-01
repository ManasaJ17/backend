    package controllers;

    import com.fasterxml.jackson.databind.JsonNode;
    import dao.UserDao;
    import models.Restaurant;
    import play.db.jpa.Transactional;
    import play.mvc.Controller;
    import play.mvc.Result;

    import javax.inject.Inject;

    public class RestaurantController extends Controller {

        private UserDao userDao;

        @Inject
        public RestaurantController(UserDao userDao) {
            this.userDao = userDao;
        }

        @Transactional
        public Result createRestaurant() {

            final JsonNode jsonNode = request().body().asJson();
            final String restName = jsonNode.get("name").asText();
            final String address = jsonNode.get("address").asText();
            final String contact = jsonNode.get("contact").asText();
            final String hpUrl = jsonNode.get("homepageUrl").asText();
            final String fbUrl = jsonNode.get("fbUrl").asText();
            final String cost = jsonNode.get("cost").asText();


            if (null == restName) {
                return badRequest("Missing restuarant name");
            }

            if (null == address) {
                return badRequest("Missing address");
            }

            if (null == contact) {
                return badRequest("Missing contact");
            }

            Restaurant user =  new Restaurant();
            user.setRestaurantName(restName);
            user.setContact(contact);
            user.setAddress(address);
            user.setHomepageUrl(hpUrl);
            user.setFbUrl(fbUrl);
            user.setCost(cost);


            //user = userDao.persist(user);


            return created( );
        }

        @Transactional
        public Result UpdateRestaurant() {
            return ok();
        }

        @Transactional
        public Result deleteRestaurant() {
            return ok();
        }

    }
