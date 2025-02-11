# @author Thomas Leveque
define ["jquery", "dataModels/ICasaDataModel", 'hubu', "contracts/RemoteNotifMgr", 'domReady'], ($, DataModel, hub, RemoteNotifMgr) ->


  locateModel = (collection, requiredId, customId)->
      returnedModel = undefined;
      returnedModel = collection.get(requiredId);
      if ((returnedModel == null || returnedModel == undefined ) && (customId != undefined))
        collection.each((internalModel) ->
          if (internalModel.get(customId) == requiredId)
            returnedModel = internalModel;
        );
      return returnedModel;

  #serverUrl = $("#map").attr("gatewayURL").replace(/\/$/, "");
  serverUrl = window.location.origin;
  console.log serverUrl;
  if !window.WebSocket
    window.WebSocket = window.MozWebSocket;
  if window.WebSocket
    socket = new WebSocket("#{serverUrl}/icasa/websocket/event".replace("http","ws"))


  requestUrl = "#server#/atmosphere/event".replace(/#server#/, serverUrl)

  socket.onmessage = (response) ->
    json = "undefined"
    message = response.data
    try
      json = $.parseJSON(message)
    catch error
      console.log "This doesn't look like a valid JSON: ", message.data
      return
    console.log "Received message :", json

    # manage zone events
    if (json.eventType == "zone-added")
      zone = locateModel(DataModel.collections.zones, json.zoneId, "zoneId");
      if ((zone == null)  || (zone == undefined))
        DataModel.collections.zones.add(json.zone);
    if (json.eventType == "zone-removed")
      zone = locateModel(DataModel.collections.zones, json.zoneId, "zoneId");
      if ((zone != null)  && (zone != undefined))
        DataModel.collections.zones.remove(zone);
    if ((json.eventType == "zone-resized") || (json.eventType == "zone-moved") || (json.eventType == "zone-variable-added") || (json.eventType == "zone-variable-removed") || (json.eventType == "zone-variable-updated"))
      zone = locateModel(DataModel.collections.zones, json.zoneId, "zoneId");
      if ((zone != null)  && (zone != undefined))
        zone.set(json.zone);

    # manage device type events
    if (json.eventType == "device-type-added")
      deviceType = locateModel(DataModel.collections.deviceTypes, json.deviceTypeId, "deviceTypeId");
      if ((deviceType == null)  || (deviceType == undefined))
        DataModel.collections.deviceTypes.add(json.deviceType);
    if (json.eventType == "device-type-removed")
      deviceType = locateModel(DataModel.collections.deviceTypes, json.deviceTypeId, "deviceTypeId");
      if ((deviceType != null)  && (deviceType != undefined))
        DataModel.collections.deviceTypes.remove(deviceType);

    # manage device events
    if (json.eventType == "device-added")
      device = locateModel(DataModel.collections.devices,json.deviceId, "deviceId");
      if ((device == null)  || (device == undefined))
        DataModel.collections.devices.add(json.device);
        #switch property to force update selected application access right
        DataModel.collections.applications.updateAccessRights.set({'update':Math.random().toString(36).substring(7)})
    if (json.eventType == "device-removed")
      device = locateModel(DataModel.collections.devices,json.deviceId, "deviceId");
      if ((device != null)  && (device != undefined))
        DataModel.collections.devices.remove(device);
        #switch property to force update selected application access right
        DataModel.collections.applications.updateAccessRights.set({'update':Math.random().toString(36).substring(7)})
    if ((json.eventType == "device-position-update") || (json.eventType == "device-property-added") || (json.eventType == "device-property-removed") || (json.eventType == "device-property-updated"))
      device = locateModel(DataModel.collections.devices,json.deviceId, "deviceId");
      if ((device != null)  && (device != undefined))
        device.set(json.device);

    # manage person type events
    if (json.eventType == "person-type-added")
      personType = locateModel(DataModel.collections.personTypes,json.personTypeId, "personTypeId");
      if ((personType == null)  || (personType == undefined))
        DataModel.collections.personTypes.add(json.personType);
    if (json.eventType == "person-type-removed")
      personType = locateModel(DataModel.collections.personTypes,json.personTypeId, "personTypeId");
      if ((personType != null)  && (personType != undefined))
        DataModel.collections.personTypes.remove(personType);

    # manage person events
    if (json.eventType == "person-added")
      person = locateModel(DataModel.collections.persons, json.personId, "personId");
      if ((person == null)  || (person == undefined))
        DataModel.collections.persons.add(json.person);
    if (json.eventType == "person-removed")
      person = locateModel(DataModel.collections.persons, json.personId, "personId");
      if ((person != null)  && (person != undefined))
        DataModel.collections.persons.remove(person);
    if (json.eventType == "person-position-update")
      person = locateModel(DataModel.collections.persons, json.personId, "personId");
      if ((person != null) && (person != undefined))
        person.set(json.person);

    # manage clock event
    if (json.eventType == "clock-modified")
      clock = DataModel.models.clock;
      clock.set(json.clock);

    # manage script events
    if ((json.eventType == "script-updated") || (json.eventType == "script-started") || (json.eventType == "script-stopped") || (json.eventType == "script-resumed") || (json.eventType == "script-paused"))
      script = locateModel(DataModel.collections.scripts, json.scriptId, "scriptId");
      if ((script != null) && (script != undefined))
        script.set(json.script);
    if (json.eventType == "script-added")
      script = locateModel(DataModel.collections.scripts, json.scriptId, "scriptId");
      if ((script != null) && (script != undefined))
        DataModel.collections.scripts.add(json.script);
    if (json.eventType == "script-removed")
      script = locateModel(DataModel.collections.scripts, json.scriptId, "scriptId");
      if ((script != null) && (script != undefined))
        DataModel.collections.scripts.remove(script);
    #valid for dashboard.
    # manage access rights.
    if(json.eventType == "access-right-modified")
        applicationModel = locateModel(DataModel.collections.applications, json.accessRight.applicationId, "applicationId");
        policy = locateModel(applicationModel.accessRights, json.accessRight.deviceId, "deviceId");
        policy.set(json.accessRight)
        #switch property to force update selected application access right
        DataModel.collections.applications.updateAccessRights.set({'update':Math.random().toString(36).substring(7)})
    if(json.eventType == "access-right-added")
        applicationModel = locateModel(DataModel.collections.applications, json.accessRight.applicationId, "applicationId");
        if applicationModel?
            applicationModel.accessRights.add(json.accessRight)
            #switch property to force update selected application access right
            DataModel.collections.applications.updateAccessRights.set({'update':Math.random().toString(36).substring(7)})
        else
            console.log "Application does not exist" + json.accessRight.applicationId;
    #end valid for dashboard.

  # component that will manage remote notification connections
  class RemoteNotifMgrImpl
    hub : null;
    name : null;
    connected : false;
    url : null;
    subSocket : null;

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
      contract: RemoteNotifMgr
      });

    setURL : (usedURL) =>
      @url = usedURL;

    getConnectionEventTopic : () ->
      return RemoteNotifMgr.getConnectionEventTopic();

    setConnected : (connectedFlag) =>
      if (@connected != connectedFlag)
        @connected = connectedFlag;
        hub.publish(@, @getConnectionEventTopic(), {connected : connectedFlag});

    isConnected : () =>
      @connected;

    reconnect : () =>
      #cleanup state
      #if (@subSocket?)
       #  @subSocket.unsubscribe();
      @setConnected(false);

      socket.onopen = (response) =>
        @setConnected(true);
        console.log("Connection opened using ws")

      socket.onreconnect = (request, response) =>
        @setConnected(true);
        console.log "Reconnecting"

      socket.onclose = (response) =>
        @setConnected(false);
        return console.log "Connection closed"

      socket.onerror = (response) =>
        @setConnected(false);
        return console.log "Connection error"

      #subSocket = socket.subscribe(request);

    start : () =>
      @reconnect();

    stop : () =>
      #if (@subSocket?)
      #  @subSocket.unsubscribe();
      @connected = false;


  return hub.createInstance(RemoteNotifMgrImpl, {name : "RemoteNotifMgrImpl-1", url : requestUrl});

