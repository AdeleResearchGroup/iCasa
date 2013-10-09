
define(["require", "hubu", "contracts/DeviceWidgetContract", "knockout", "log4javascript"], (require, hub, DeviceWidgetContract, ko, log4javascript) ->

    console.log("presenceSensor module loaded !!!");

    class PresenceSensorWidget extends DeviceWidgetContract
        name : null
        logger : null
        iconURL : null;

        constructor : (name) ->
            @name = name;
            @logger = log4javascript.getLogger("PresenceSensorWidget");
            @logger.removeAllAppenders();
            @logger.addAppender(new log4javascript.BrowserConsoleAppender());
            @iconURL = require.toUrl("./movementDetector.png");

        start : -> @logger.info("PresenceSensorWidget starting...");

        stop : -> @logger.info("PresenceSensorWidget stoping...");

        configure : (theHub, config) ->
          if (config?.name?) then @name = config.name;
          @hub = theHub;
          @hub.provideService({
            component: this,
            contract: DeviceWidgetContract
          });

        getComponentName: -> return @name;

        getBaseIconURL : () -> return @iconURL;

        getCurrentIconURL : () -> null;

        manageDynamicIcon : () -> false;

        manageDevice : (device) ->
          return ((device.type() == "iCasa.PresenceSensor") || device.hasService("fr.liglab.adele.icasa.device.presence.PresenceSensor"));

        getStatusWindowTemplateURL : () -> null;

        getDecorators : () ->
          return [ {
            name: "presence",
            url: require.toUrl('./movementDetector_detected.png'),
            width: 32,
            height: 32,
            positionX: 1,
            positionY: 1,
            show: false
          } ];

        propHasChanged : (device) ->
          presence = device.getPropertyValue("presenceSensor.sensedPresence");
          ko.utils.arrayForEach(device.decorators(), (decorator) ->
            if (!(decorator?))
              return;
            if (decorator.name() == "presence")
              decorator.show(presence == true);
          );

        init : (deviceViewModel) ->
          null;

    instance = hub.createInstance(PresenceSensorWidget, {name : "presenceSensorWidget-1"});

    console.log("presenceSensor module end loaded !!!");

    return instance;
);
