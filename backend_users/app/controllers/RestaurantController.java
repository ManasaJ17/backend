package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import dao.Restaurantdao;
import dao.UserDao;
import models.Restaurant;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class RestaurantController extends Controller {

    private Restaurantdao restaurantdao;

    @Inject
    public RestaurantController(Restaurantdao restaurantdao) {
        this.restaurantdao = restaurantdao;
    }

    @Transactional
    public Result createRestaurant() {

        final JsonNode jsonNode = request().body().asJson();
        final String restName = jsonNode.get("name").asText();
        final String type=jsonNode.get("type").asText();
        final String timings=jsonNode.get("timings").asText();
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

        Restaurant res=  new Restaurant();
        res.setRestaurantName(restName);
        res.setType(type);
        res.setTimings(timings);
        res.setContact(contact);
        res.setAddress(address);
       res.setHomepageUrl(hpUrl);
        res.setFbUrl(fbUrl);
        res.setCost(cost);


        res= restaurantdao.persist(res);


        return created(" "+res);
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
