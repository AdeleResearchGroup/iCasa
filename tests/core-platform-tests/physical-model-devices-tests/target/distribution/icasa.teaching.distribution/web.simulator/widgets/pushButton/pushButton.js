(function() {
  var __hasProp = {}.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; };

  define(["require", "hubu", "contracts/DeviceWidgetContract", "knockout", "log4javascript"], function(require, hub, DeviceWidgetContract, ko, log4javascript) {
    var PushButtonWidget, instance;
    console.log("PushButtonWidget module loaded !!!");
    PushButtonWidget = (function(_super) {

      __extends(PushButtonWidget, _super);

      PushButtonWidget.prototype.name = null;

      PushButtonWidget.prototype.logger = null;

      PushButtonWidget.prototype.iconURL = null;

      function PushButtonWidget(name) {
        this.name = name;
        this.logger = log4javascript.getLogger("PushButtonWidget");
        this.logger.removeAllAppenders();
        this.logger.addAppender(new log4javascript.BrowserConsoleAppender());
        this.iconURL = require.toUrl("./pushButton.png");
      }

      PushButtonWidget.prototype.start = function() {
        return this.logger.info("PushButtonWidget starting...");
      };

      PushButtonWidget.prototype.stop = function() {
        return this.logger.info("PushButtonWidget stoping...");
      };

      PushButtonWidget.prototype.configure = function(theHub, config) {
        if (((config != null ? config.name : void 0) != null)) {
          this.name = config.name;
        }
        this.hub = theHub;
        return this.hub.provideService({
          component: this,
          contract: DeviceWidgetContract
        });
      };

      PushButtonWidget.prototype.getComponentName = function() {
        return this.name;
      };

      PushButtonWidget.prototype.getBaseIconURL = function() {
        return this.iconURL;
      };

      PushButtonWidget.prototype.getCurrentIconURL = function() {
        return null;
      };

      PushButtonWidget.prototype.manageDynamicIcon = function() {
        return false;
      };

      PushButtonWidget.prototype.manageDevice = function(device) {
        return device.hasService("fr.liglab.adele.icasa.device.button.PushButton");
      };

      PushButtonWidget.prototype.getStatusWindowTemplateURL = function() {
        return null;
      };

      PushButtonWidget.prototype.getDecorators = function() {
        return [];
      };

      PushButtonWidget.prototype.propHasChanged = function(device) {
        return null;
      };

      PushButtonWidget.prototype.init = function(deviceViewModel) {
        return null;
      };

      return PushButtonWidget;

    })(DeviceWidgetContract);
    instance = hub.createInstance(PushButtonWidget, {
      name: "push-button-widget-1"
    });
    console.log("PushButtonWidget module end loaded !!!");
    return instance;
  });

}).call(this);
