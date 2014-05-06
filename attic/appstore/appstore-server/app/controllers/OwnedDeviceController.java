package controllers;

import models.OwnedDevice;
import models.User;
import org.codehaus.jackson.node.ArrayNode;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

/**
 *
 */
public class OwnedDeviceController extends Controller{

    static Form<OwnedDevice> deviceForm = form(OwnedDevice.class);


    @SecureSocial.SecuredAction
    public static Result getDevices(){
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.getUserByUserId(socialUser.id().id());
        System.out.println("get Devices ");
        ArrayNode devices = OwnedDevice.toJson(user.devices);
        return ok(devices);
    }
    @SecureSocial.SecuredAction
    public static Result addDevice(){
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.getUserByUserId(socialUser.id().id());
        Form<OwnedDevice> filledForm = deviceForm.bindFromRequest();
        OwnedDevice device = filledForm.get();
        user.assignDevice(device);
        System.out.println("Add Device");
        return ok();
    }
}
