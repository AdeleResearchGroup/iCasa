
define(["require", "hubu", "contracts/DeviceWidgetContract", "knockout", "log4javascript"], (require, hub, DeviceWidgetContract, ko, log4javascript) ->

    console.log("PushButtonWidget module loaded !!!");

    class PushButtonWidget extends DeviceWidgetContract
        name : null
        logger : null
        iconURL : null;

        constructor : (name) ->
            @name = name;
            @logger = log4javascript.getLogger("PushButtonWidget");
            @logger.removeAllAppenders();
            @logger.addAppender(new log4javascript.BrowserConsoleAppender());
            @iconURL = require.toUrl("./pushButton.png");

        start : -> @logger.info("PushButtonWidget starting...");

        stop : -> @logger.info("PushButtonWidget stoping...");

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
          return (device.hasService("fr.liglab.adele.icasa.device.button.PushButton"));

        getStatusWindowTemplateURL : () -> null;

        getDecorators : () ->
          return [ ];

        propHasChanged : (device) ->
          null;

        init : (deviceViewModel) ->
          null;

    instance = hub.createInstance(PushButtonWidget, {name : "push-button-widget-1"});

    console.log("PushButtonWidget module end loaded !!!");

    return instance;
);
