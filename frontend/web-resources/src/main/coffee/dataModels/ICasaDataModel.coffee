
define(['jquery', 'backbone', 'underscore', 'hubu', 'contracts/DataModelConnectionMgr','contracts/RemoteNotifMgr', 'domReady'],
     ($, Backbone, _, hub, DataModelConnectionMgr, RemoteNotifMgr) ->

        # gatewayURL = $("#map").attr("gatewayURL").replace(/\/$/, "");
         gatewayURL = window.location.origin;
         serverUrl = gatewayURL + "/icasa";

         DataModel =
            Models : {}
            Collections : {}
            collections : {}
            models : {}
            resetState : ()->
                $.ajax(
                    type: 'DELETE',
                    url: gatewayURL + "/icasa/simulation",
                    success: () =>
                        DataModel.collections.zones.reset();
                        DataModel.collections.persons.reset();
                        DataModel.collections.devices.fetch(); #Get unremoved devices (real devices)
                    error :() =>
                        console.log "Unable to reset simulation in backend";
                );

         #Valid only for dashboard

         class DataModel.Models.AccessRight extends Backbone.Model
            urlRoot: "#{serverUrl}/policies/policy/"

         class DataModel.Collections.AccessRights extends Backbone.Collection
            url: "#{serverUrl}/policies/"
            model: DataModel.Models.AccessRight

         class DataModel.Models.Application extends Backbone.Model
            urlRoot: "#server#/apps/apps".replace /#server#/, serverUrl
            defaults:
                status: "Started"
            constructor: ()->
                super
                @accessRights = new DataModel.Collections.AccessRights()
                #Get access right for the created application model.
                console.log @get('id');
                if @get('id')? && @.get('id') != "NONE"
                    @accessRights.url = "#{serverUrl}/policies/application/" + @.get('id');
                    @accessRights.fetch();

         class DataModel.Collections.Applications extends Backbone.Collection
            url: "#server#/apps/apps".replace /#server#/, serverUrl
            model: DataModel.Models.Application
            updateAccessRights : new Backbone.Model({update:true})

         #end valid only for dashboard


         class DataModel.Models.Frontend extends Backbone.Model
            urlRoot : "/dashboard/frontend"

         class DataModel.Models.Layerapp extends Backbone.Model
            urlRoot : "#server#/layerapps/layerapps".replace /#server#/, serverUrl

         class DataModel.Models.Device extends Backbone.Model
            urlRoot : "#server#/devices/device".replace /#server#/, serverUrl

         class DataModel.Models.Property extends Backbone.Model
          idAttribute: "name"
          defaults:
              visible: false

         class DataModel.Models.Properties extends Backbone.Collection
              model: DataModel.Models.Property

         class DataModel.Collections.Devices extends Backbone.Collection
            url: "#server#/devices/devices".replace /#server#/, serverUrl
            model: DataModel.Models.Device

         class DataModel.Collections.LayerApps extends Backbone.Collection
            url: "#server#/layerapps/layerapps".replace /#server#/, serverUrl
            model: DataModel.Models.Layerapp

         class DataModel.Models.DeviceType extends Backbone.Model
            urlRoot : "#server#/devices/deviceType".replace /#server#/, serverUrl

         class DataModel.Collections.DeviceTypes extends Backbone.Collection
            url: "#server#/devices/deviceTypes".replace /#server#/, serverUrl
            model: DataModel.Models.DeviceType

         class DataModel.Collections.SimulatedDeviceTypes extends Backbone.Collection
            url: "#server#/devices/simulatedDeviceTypes".replace /#server#/, serverUrl
            model: DataModel.Models.DeviceType

         class DataModel.Models.PersonType extends Backbone.Model
            urlRoot : "#server#/persons/personType".replace /#server#/, serverUrl

         class DataModel.Collections.PersonTypes extends Backbone.Collection
            url: "#server#/persons/personTypes".replace /#server#/, serverUrl
            model: DataModel.Models.PersonType

         class DataModel.Models.Person extends Backbone.Model
            urlRoot : "#server#/persons/person".replace /#server#/, serverUrl

         class DataModel.Collections.Persons extends Backbone.Collection
            url: "#server#/persons/persons".replace /#server#/, serverUrl
            model: DataModel.Models.Person

         class DataModel.Models.Zone extends Backbone.Model
           urlRoot : "#server#/zones/zone".replace /#server#/, serverUrl

         class DataModel.Collections.Zones extends Backbone.Collection
           url: "#server#/zones/zones".replace /#server#/, serverUrl
           model: DataModel.Models.Zone

         class DataModel.Models.Clock extends Backbone.Model
           urlRoot : "#server#/clocks/clock".replace /#server#/, serverUrl

         class DataModel.Models.Script extends Backbone.Model
           urlRoot : "#server#/scriptPlayer/script".replace /#server#/, serverUrl

         class DataModel.Collections.Scripts extends Backbone.Collection
           url: "#server#/scriptPlayer/scripts".replace /#server#/, serverUrl
           model: DataModel.Models.Script

         #Valid only for dashboard
         DataModel.collections.applications = new DataModel.Collections.Applications();
         DataModel.collections.applications.add(new DataModel.Models.Application({id: "NONE", name:"NONE", version: "0.0.0"}))
         #End valid for dashboard.

         # initial import of data model
         DataModel.models.clock = new DataModel.Models.Clock({id: "default"});
         console.log DataModel.models.clock;

         DataModel.collections.scripts = new DataModel.Collections.Scripts();

         DataModel.collections.zones = new DataModel.Collections.Zones();

         DataModel.collections.persons = new DataModel.Collections.Persons();

         DataModel.collections.personTypes = new DataModel.Collections.PersonTypes();

         DataModel.collections.devices = new DataModel.Collections.Devices();
         console.log DataModel.collections.devices;

         DataModel.collections.LayerApps = new DataModel.Collections.LayerApps();
         console.log DataModel.Collections.LayerApps;

         DataModel.collections.deviceTypes = new DataModel.Collections.SimulatedDeviceTypes();

         # component that will manage remote Data model connections
         class DataModelMgrImpl
           hub : null;
           name : null;
           connected : false;
           url : null;

           getComponentName: () ->
             return @name;

           configure: (theHub, configuration) =>
             @hub = theHub;
             @connected = false;
             if (config?.name?)
               @name = config.name;
             if (config?.url?)
               @url = config.url;

             @hub.provideService({
               component: @,
               contract: DataModelConnectionMgr
             });

           setURL : (usedURL) =>
             @url = usedURL;

           getConnectionEventTopic : () ->
             return DataModelConnectionMgr.getConnectionEventTopic();

           setConnected : (connectedFlag) =>
             if (@connected != connectedFlag)
               @connected = connectedFlag;
               hub.publish(@, @getConnectionEventTopic(), {connected : connectedFlag});

           isConnected : () =>
             @connected;

           reconnect : () =>
             initialConnection = false;
             connectionCallback = {
               success : (data) =>
                 if (!initialConnection)
                   initialConnection = true;
                   @setConnected(true);
                 console.log(data);

               error : (err) =>
                 @setConnected(false);
                 throw err;
             };

             DataModel.models.clock.fetch(connectionCallback);
             DataModel.collections.scripts.fetch(connectionCallback);
             DataModel.collections.zones.fetch(connectionCallback);
             DataModel.collections.persons.fetch(connectionCallback);
             DataModel.collections.personTypes.fetch(connectionCallback);
             DataModel.collections.devices.fetch(connectionCallback);
             DataModel.collections.deviceTypes.fetch(connectionCallback);
             DataModel.collections.LayerApps.fetch(connectionCallback);
             #Valid only for dashboard.
             updateApplications = {
               success : (data) =>
                 DataModel.collections.applications.add(new DataModel.Models.Application({id: "NONE", name:"NONE", version: "0.0.0"}))
               error : (err) =>
                 console.log "updating applications Error"
                 DataModel.collections.applications.add(new DataModel.Models.Application({id: "NONE", name:"NONE", version: "0.0.0"}))
             };
             DataModel.collections.applications.fetch(updateApplications)
             #updateApplications);
             #End valid only for dashboard.

           start : () =>
             @reconnect();


           stop : () =>
             @connected = false;

         hub.createInstance(DataModelMgrImpl, {name : "DataModelMgrImpl-1", url : serverUrl});

         return DataModel;
);
