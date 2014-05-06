package controllers;

import models.User;
import models.values.Device;
import models.values.Order;
import models.values.Product;
import org.codehaus.jackson.node.ArrayNode;
import org.h2.util.IOUtils;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;

import java.io.*;
import java.util.List;

/**
 *
 */
public class DeviceController extends Controller {
    static Form<Device> deviceForm = form(Device.class);


    @SecureSocial.SecuredAction
    public static Result devices(){
        System.out.println("Getting devices");

        List<Device> deviceList = null;
        deviceList = Device.all();
        ArrayNode devices = Json.newObject().arrayNode();
        for (Device device : deviceList) {
            devices.add(Device.toJson(device));
        }
        return ok(devices);
    }
    @SecureSocial.SecuredAction
    public static Result addDevice(){
        Form<Device> filledForm = deviceForm.bindFromRequest();
        Device ndevice;
        if (filledForm.hasErrors()){
            System.out.println(filledForm);
            return badRequest();
        }

        try{
            ndevice = filledForm.get();
            Device.create(ndevice);
        }catch (Exception ex){
            ex.printStackTrace();
            return badRequest();
        }
        return ok(Device.toJson(ndevice));
    }

    @SecureSocial.SecuredAction
    public static Result ownedDevices(){
        Identity socialUser = (Identity) ctx().args.get(SecureSocial.USER_KEY);
        User user = User.getUserByUserId(socialUser.id().id());
        List<Order> buy = user.orders;
        List<Product> allProducts = null;

        allProducts = user.getOwnedProducts();
        ArrayNode devices = Json.newObject().arrayNode();
        for (Product product : allProducts) {
            for (Device device: product.lastVersion.devices){
                devices.add(Device.toJson(device));
            }
        }
        return ok(devices);
    }
    @SecureSocial.SecuredAction
    public static Result uploadImage(String productId){
        System.out.println(productId);
        Device device = Device.find.byId(Integer.parseInt(productId));
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart picture = body.getFile("devicePicture");
        String fileURL = "public/images/devices/" + productId + ".jpg";
        String serverURL = "assets/images/devices/" + productId + ".jpg";
        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            File file = picture.getFile();
            try {
                InputStream is    = new FileInputStream(file);
                OutputStream os = new FileOutputStream(new File(fileURL));
                IOUtils.copy(is, os);
                is.close();
                os.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                flash("error", "unable to save file");
                return badRequest();
            }
            device.imageURL = serverURL ;
            device.save();
            return ok(Device.toJson(device));
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }
}
