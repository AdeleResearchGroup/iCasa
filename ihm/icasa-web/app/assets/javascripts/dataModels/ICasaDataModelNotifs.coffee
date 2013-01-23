# @author Thomas Leveque
define ["jquery", "knockout", "knockback", "atmosphere", "dataModels/ICasaDataModel"], ($, ko, kb, atmosphere, DataModel) ->
  socket = atmosphere
  serverUrl = "http://" + window.location.hostname + ":8080"
  transport = "sse"
  # workaround for SECURITY Exception on Chrome while using sse on localhost
  isChrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
  if (isChrome && (serverUrl.indexOf("localhost") > -1))
      transport = "long-polling";

  requestUrl = "#server#/atmosphere/event".replace(/#server#/, serverUrl)
  request =
    url: requestUrl
    contentType: "application/json"
    logLevel: "debug"
    shared: true
    transport: transport
    trackMessageLength: true
    enableXDR: true
    fallbackTransport: "long-polling"
    dropAtmosphereHeaders: true
    attachHeadersAsQueryString: true

  request.onOpen = (response) ->
    transport = response.transport
    console.log("Connection opened using " + transport)

  request.onReconnect = (request, response) ->
    socket.info "Reconnecting"

  request.onMessage = (response) ->
    json = "undefined"
    message = response.responseBody
    try
      json = $.parseJSON(message)
    catch error
      console.log "This doesn't look like a valid JSON: ", message.data
      return
    console.log "Received message :", json

    # manage zone events
    if (json.eventType == "zone-added")
      zone = DataModel.collections.zones.get(json.zoneId);
      if ((zone != null)  && (zone != undefined))
        DataModel.collections.zones.create(json.zone);
    if (json.eventType == "zone-removed")
      zone = DataModel.collections.zones.get(json.zoneId);
      if ((zone != null)  && (zone != undefined))
        DataModel.collections.zones.remove(zone);
    if ((json.eventType == "zone-resized") || (json.eventType == "zone-moved") || (json.eventType == "zone-variable-added") || (json.eventType == "zone-variable-removed") || (json.eventType == "zone-variable-updated"))
      zone = DataModel.collections.zones.get(json.zoneId);
      if ((zone != null)  && (zone != undefined))
        zone.set(json.zone);

    # manage device type events
    if (json.eventType == "device-type-added")
      deviceType = DataModel.collections.deviceTypes.get(json.deviceTypeId);
      if ((deviceType != null)  && (deviceType != undefined))
        DataModel.collections.deviceTypes.create(json.deviceType);
    if (json.eventType == "device-type-removed")
      deviceType = DataModel.collections.deviceTypes.get(json.deviceTypeId);
      if ((deviceType != null)  && (deviceType != undefined))
        DataModel.collections.deviceTypes.remove(deviceType);

    # manage device events
    if (json.eventType == "device-added")
      device = DataModel.collections.devices.get(json.deviceId);
      if ((device != null)  && (device != undefined))
        DataModel.collections.deviceTypes.create(json.device);
    if (json.eventType == "device-removed")
      device = DataModel.collections.devices.get(json.deviceId);
      if ((device != null)  && (device != undefined))
        DataModel.collections.devices.remove(device);
    if ((json.eventType == "device-position-update") || (json.eventType == "device-property-added") || (json.eventType == "device-property-removed") || (json.eventType == "device-property-updated"))
      device = DataModel.collections.devices.get(json.deviceId);
      if ((device != null)  && (device != undefined))
        device.set(json.device);

    # manage person type events
    if (json.eventType == "person-type-added")
      personType = DataModel.collections.personTypes.get(json.personTypeId);
      if ((personType != null)  && (personType != undefined))
        DataModel.collections.personTypes.create(json.personType);
    if (json.eventType == "person-type-removed")
      personType = DataModel.collections.personTypes.get(json.personTypeId);
      if ((personType != null)  && (personType != undefined))
        DataModel.collections.personTypes.remove(personType);

    # manage person events
    if (json.eventType == "person-added")
      person = DataModel.collections.persons.get(json.personId);
      if ((person != null)  && (person != undefined))
        DataModel.collections.persons.create(json.person);
    if (json.eventType == "person-removed")
      person = DataModel.collections.persons.get(json.personId);
      if ((person != null)  && (person != undefined))
        DataModel.collections.persons.remove(person);
    if (json.eventType == "person-position-update")
      person = DataModel.collections.persons.get(json.personId);
      if ((person != null) && (person != undefined))
        person.set(json.person);

  request.onClose = (response) ->
    console.log "Connection closed"

  request.onError = (response) ->
    console.log "Connection error"

  subSocket = socket.subscribe(request)
