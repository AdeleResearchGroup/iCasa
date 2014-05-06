/*
 * Copyright ADELE Research Group LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package controllers;

import models.values.Application;
import models.values.Service;
import org.codehaus.jackson.node.ArrayNode;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

/**
 *
 */
public class ServiceController extends Controller {
	static Form<Service> serviceForm = form(Service.class);


	public static Result services(){
        System.out.println("Getting applications");

        List<Service> serviceList = null;
        serviceList = Service.all();
        ArrayNode services = Json.newObject().arrayNode();
        for (Service application : serviceList) {
            services.add(Service.toJson(application));
        }
        return ok(services);
    }

	public static Result addService(){
		Form<Service> filledForm = serviceForm.bindFromRequest();
		if (filledForm.hasErrors()){
            System.out.println(filledForm);
			return badRequest();
		} else {
			try{
				Service.create(filledForm.get());
			}catch (Exception ex){
                ex.printStackTrace();
				return badRequest();
			}
			return ok();
		}
	}
}
