
define(["require", "hubu", "contracts/DeviceWidgetContract", "knockout", "log4javascript"], (require, hub, DeviceWidgetContract, ko, log4javascript) ->

    console.log("BoxWidget module loaded !!!");

    class BoxWidget extends DeviceWidgetContract
        name : null
        logger : null
        iconURL : null;

        constructor : (name) ->
            @name = name;
            @logger = log4javascript.getLogger("BoxWidget");
            @logger.removeAllAppenders();
            @logger.addAppender(new log4javascript.BrowserConsoleAppender());
            @iconURL = require.toUrl("./livebox.png");

        start : -> @logger.info("BoxWidget starting...");

        stop : -> @logger.info("BoxWidget stoping...");

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
          return (device.hasService("fr.liglab.adele.icasa.device.box.Box"));

        getStatusWindowTemplateURL : () -> null;

        getDecorators : () ->
          return [ ];

        propHasChanged : (device) ->
          null;

        init : (deviceViewModel) ->
          null;

    instance = hub.createInstance(BoxWidget, {name : "box-widget-1"});

    console.log("BoxWidget module end loaded !!!");

    return instance;
);
