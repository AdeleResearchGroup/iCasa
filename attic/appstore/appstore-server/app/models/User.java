package models;

import models.values.Order;
import models.values.Product;
import models.values.ProductPrice;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import scala.Option;
import securesocial.core.*;
//import securesocial.core.java.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

//import securesocial.core.java.PasswordInfo;

/**
 *
 */
@Entity
@Table(name="User")
public class User extends Model {



    @Id
    @Column(name="id")
    @GeneratedValue
    public int id;

    @Column(name="userId")
    public String userId;

    @Constraints.Required
    @Column(name="username")
    public String username;

    @Column(name="fullname")
    public String fullname;

    @Column(name="email")
    public String email;

    @Column(name="password")
    public String password;

    @Column(name="provider")
    public String provider;

    @Column(name="authmethod")
    public String authMethod;

    @OneToMany
    @JoinTable(name="Orders")
    public List<Order> orders;

    @OneToMany
    @JoinTable(name="OwnedDevice")
    public List<OwnedDevice> devices;

    public static void create(Identity user) {
        User newUser = User.getUserByUserId(user.id().id());
        if (newUser == null){
            newUser = new User();
        }
        System.out.println("Writing user on the DB");
        newUser.username = user.id().id();
        newUser.userId = user.id().id() ;
        if (user.fullName() != null){
            newUser.fullname = user.fullName();
        } else {
            newUser.fullname = newUser.username;
        }
        newUser.email = user.email().toString();
        newUser.provider = user.id().providerId();
        newUser.authMethod  = user.authMethod().method();

        if (user.authMethod() == AuthenticationMethod.UserPassword()) {
            newUser.password = user.passwordInfo().get().password();
        }
        else if (user.authMethod() == AuthenticationMethod.OAuth1()){
            newUser.password = user.oAuth1Info().get().token();
        } else if (user.authMethod() == AuthenticationMethod.OAuth2()){
            newUser.password = user.oAuth2Info().get().accessToken();
        }
        System.out.println("Fullname" + user.fullName());
        newUser.save();
    }

    public static Identity getSocialUser(String userId){
        User user = User.getUserByUserId(userId);
        SocialUser socialUser = null;
        if(user != null){
            UserId uid =  new UserId(userId, user.provider);
            AuthenticationMethod method = null;
            PasswordInfo passwordInfo = null;
            OAuth1Info oAuth1Info = null;
            OAuth2Info oAuth2Info = null;
//            socialUser.id =
//            socialUser.id.id = userId;
//            socialUser.id.provider = user.provider;
//            socialUser.fullName = user.fullname;
//            socialUser.avatarUrl = null;
//            socialUser.email = user.email;

            //socialUser.isEmailVerified = true;
            if (user.authMethod.compareToIgnoreCase(securesocial.core.AuthenticationMethod.UserPassword().method())==0){
                method = AuthenticationMethod.UserPassword();
                passwordInfo = new PasswordInfo(user.password, user.password, Option.<String>empty());
                System.out.println("Is U_PWD");
            } else if (user.authMethod.compareToIgnoreCase(securesocial.core.AuthenticationMethod.OAuth1().method())==0){
                method = AuthenticationMethod.OAuth1();
                oAuth1Info = new OAuth1Info(user.password, user.password);
                System.out.println("Is OAUTH1");
            } else if (user.authMethod.compareToIgnoreCase(securesocial.core.AuthenticationMethod.OAuth2().method()) == 0){
                method = AuthenticationMethod.OAuth2();
                oAuth2Info = new OAuth2Info(user.password, Option.<String>empty(), Option.empty(), Option.<String>empty());
                System.out.println("Is OAUTH2");
            } else {
                System.out.println("Is NOTHING!!!" + user.authMethod + "Oth! " +securesocial.core.AuthenticationMethod.OAuth2().method());
                return null;
            }

            socialUser = new SocialUser (uid, userId, userId, user.fullname, Option.apply(user.email), Option.<String>empty(), method, Option.apply(oAuth1Info), Option.apply(oAuth2Info), Option.apply(passwordInfo));

        }
        return socialUser;
    }

    public static Finder<String,User> find = new Finder<String, User>(
            String.class, User.class
    );

    public static User getUserByUserId(String userId){
        List<User> listUsers = find.where().eq("userId", userId).findList();
        if (listUsers.isEmpty()){
            System.out.println("Is Empty!!");
            return null;
        }
        return listUsers.get(0);//It must be only one user with the Id
    }

    public static User getUser(String username, String password) {
        User user = find.where().eq("userId", username).eq("password", password).findUnique();
        return user;
    }

    public void buyProduct(Product product){

        ProductPrice price = product.getPrice();
        //Assign information to the new order
        Order order = new Order();
        order.price = price;
        order.user = this;
        order.save();
        orders.add(order);
        this.saveManyToManyAssociations("orders");
        this.update();
    }

    public List<Product> getOwnedProducts(){
        List<Product> owned = new ArrayList<Product>();
        for(Order order: this.orders){
            owned.add(order.price.product);
        }
        return owned;
    }

    public void assignDevice(OwnedDevice device){
        device.user = this;
        this.devices.add(device);
        device.save();
        this.update();
    }
}
