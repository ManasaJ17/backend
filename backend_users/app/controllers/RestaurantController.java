    package controllers;

            import com.fasterxml.jackson.databind.JsonNode;
            import com.sun.mail.smtp.SMTPMessage;
            import controllers.security.Authenticator;
            import dao.RestaurantDao;
            import dao.UserDao;
            import models.Restaurant;
            import models.User;
            import play.Logger;
            import play.db.jpa.Transactional;
            import play.libs.F;
            import play.libs.Json;
            import play.mvc.Controller;
            import play.mvc.Result;
            import javax.inject.Inject;
            import javax.mail.*;
            import javax.mail.internet.InternetAddress;
            import java.time.LocalDateTime;
            import java.time.LocalTime;
            import java.util.ArrayList;
            import java.util.List;
            import java.util.Properties;


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
                    final String imagePath = jsonNode.get("img").asText();


                    LOGGER.debug("img" + imagePath);

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
                    restaurant.setImg(imagePath);
                    restaurant.setCuisineCount(cuisine.split("\\,").length);
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

                    Properties props = new Properties();
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.socketFactory.port", "465");
                    props.put("mail.smtp.socketFactory.class",
                            "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", "587");

                    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication("platrovacare02@gmail.com","Platrova1234");
                        }
                    });

                    try {

                        LOGGER.debug("Inside mail try");

                        String url = "http://localhost:9000/restaurant/status/";

                        SMTPMessage message = new SMTPMessage(session);
                        message.setFrom(new InternetAddress("platrovacare02@gmail.com"));
                        message.setRecipients(Message.RecipientType.TO,
                                InternetAddress.parse( "platrovaservice@gmail.com"));

                        message.setSubject("Review Restaurant: " + name);
                        message.setText("Review  " + name + " Details of the Restaurant:\n\n Address:"+ address+"\n\nContact: " + contact + "\n\n emailid: " + user.getEmail()+
                                        "\nClick the link below to Approve the Restaurant:\n "+ url + "1/" + restaurant.getId() +
                                        "\nClick the link below to Reject the Restaurant:\n "+ url + "2/"  + restaurant.getId()
                        );

                        message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

                        Transport.send(message);

                        System.out.println("sent");

                    }

                    catch (MessagingException e) {

                        LOGGER.debug("Inside mail catch");
                        throw new RuntimeException(e);
                    }

                    com.fasterxml.jackson.databind.node.ObjectNode json = Json.newObject();
                    json.put("name", restaurant.getName());

                    return created(json);


                }

                @Transactional
                public Result deleteRestaurant() {

                    final JsonNode jsonNode = request().body().asJson();
                    final String name = jsonNode.get("name").asText();
                    final String address = jsonNode.get("address").asText();
                    return ok();
                }

                @Transactional
                public Result getAllRestaurants() {

                    final List<Restaurant> clients = restaurantDao.findAll();

                    JsonNode jsonNode1 = Json.toJson(clients);

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
                    final String image = jsonNode.get("img").asText();
                    LOGGER.debug("after taking input");

                    Restaurant restaurant = restaurantDao.getRestaurant(name,address);
                    restaurant.setType(type);
                    restaurant.setCuisine(cuisine);
                    restaurant.setContact(contact);
                    restaurant.setTimings(timings);
                    restaurant.setHomepageUrl(hpUrl);
                    restaurant.setFbUrl(fbUrl);
                    restaurant.setCost(cost);
                    restaurant.setImg(image);
                    restaurant.setCuisineCount(cuisine.split("\\,").length);
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
                    json.put("img", restaurant.getImg());

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
                    final String keyword = jsonNode.get("keyword").asText();

                    List<Restaurant> restaurants =restaurantDao.searchRestaurant(keyword);
                    if (null == restaurants){
                        LOGGER.debug("no restaurants");
                        return badRequest();
                    }

                    JsonNode json = Json.toJson(restaurants);
                    LOGGER.debug(String.valueOf(json));

                    return ok(json);
                }

                @Transactional
                public Result getNearByRestaurants() {

                    LOGGER.debug("inside getNearByRestaurants ");

                    JsonNode jsonNode = request().body().asJson();
                    final Double latitude = jsonNode.get("lat").asDouble();
                    final Double longitude = jsonNode.get("lng").asDouble();

                    LOGGER.debug("lat & long: " + String.valueOf(latitude + " " + longitude));


                    List<F.Tuple<Restaurant, Double>> restaurants = restaurantDao.nearByRestaurants(latitude, longitude);

                    if (null == restaurants) {
                        return badRequest("no restaurants to display");
                    }

                        final JsonNode jsonNode1 = Json.toJson(restaurants);
                        LOGGER.debug(String.valueOf(jsonNode1));

                        return ok(jsonNode1);

                }

                @Transactional
                public Result getPopularRestaurants() {

                    LOGGER.debug("inside getPopularRestaurants");

                    List<Restaurant> restaurants = restaurantDao.popularRestaurants();

                    JsonNode json = Json.toJson(restaurants);
                    LOGGER.debug("before return");

                    return ok(json);
                }

        @Transactional
        public Result updateStatus( int status, int id){

            LOGGER.debug("inside updateStatus");

            Restaurant restaurant = restaurantDao.getRestaurantById(id);
            User owner = restaurant.getOwner();

            String email = owner.getEmail();

            if(status == 1){
                restaurant.setStatus(Restaurant.ApproveStatus.Approved);
            }
            else
                restaurant.setStatus(Restaurant.ApproveStatus.Rejected);

            restaurantDao.persist(restaurant);

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("platrovaservice@gmail.com","Platrova1234");
                }
            });

            try {

                LOGGER.debug("Inside mail try");

                SMTPMessage message = new SMTPMessage(session);
                message.setFrom(new InternetAddress("platrovaservice@gmail.com"));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(email));

                message.setSubject("Approval Mail");
                message.setText("Hey, \n Your Restaurant, " + restaurant.getName() + " has been "+ restaurant.getStatus());

                message.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS);

                Transport.send(message);

                System.out.println("sent");

            }

            catch (MessagingException e) {

                LOGGER.debug("Inside mail catch");
                throw new RuntimeException(e);
            }

            LOGGER.debug("");

            return ok("Role updated & approval mail is sent to the Client");

        }

        @Transactional
        @Authenticator
        public Result RestaurantsByLikes()
        {
            LOGGER.debug("inside Restaurants Based on Likes");

            final User user = (User)ctx().args.get("user");

            String likes = user.getLikes();
            String[] like = likes.split("\\,");
            LOGGER.debug(String.valueOf(like[0]));

            F.Tuple<String,List<Restaurant>> restaurants;

            List<F.Tuple<String,List<Restaurant>>> restList= new ArrayList<>();

            for(int i = 0 ; i < like.length ; i++){
                LOGGER.debug(like[i]);
                restaurants = restaurantDao.getRestaurantsByLikes(like[i]);
                LOGGER.debug(String.valueOf(restaurants));

                if (null != restaurants){
                    restList.add(restaurants);
                }
            }

            JsonNode jsonNode1  = Json.toJson(restList);
            return ok(jsonNode1);

        }

    }


