(function() {
  var __hasProp = {}.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; };

  define(["require", "hubu", "contracts/DeviceWidgetContract", "knockout", "log4javascript"], function(require, hub, DeviceWidgetContract, ko, log4javascript) {
    var BoxWidget, instance;
    console.log("BoxWidget module loaded !!!");
    BoxWidget = (function(_super) {

      __extends(BoxWidget, _super);

      BoxWidget.prototype.name = null;

      BoxWidget.prototype.logger = null;

      BoxWidget.prototype.iconURL = null;

      function BoxWidget(name) {
        this.name = name;
        this.logger = log4javascript.getLogger("BoxWidget");
        this.logger.removeAllAppenders();
        this.logger.addAppender(new log4javascript.BrowserConsoleAppender());
        this.iconURL = require.toUrl("./livebox.png");
      }

      BoxWidget.prototype.start = function() {
        return this.logger.info("BoxWidget starting...");
      };

      BoxWidget.prototype.stop = function() {
        return this.logger.info("BoxWidget stoping...");
      };

      BoxWidget.prototype.configure = function(theHub, config) {
        if (((config != null ? config.name : void 0) != null)) {
          this.name = config.name;
        }
        this.hub = theHub;
        return this.hub.provideService({
          component: this,
          contract: DeviceWidgetContract
        });
      };

      BoxWidget.prototype.getComponentName = function() {
        return this.name;
      };

      BoxWidget.prototype.getBaseIconURL = function() {
        return this.iconURL;
      };

      BoxWidget.prototype.getCurrentIconURL = function() {
        return null;
      };

      BoxWidget.prototype.manageDynamicIcon = function() {
        return false;
      };

      BoxWidget.prototype.manageDevice = function(device) {
        return device.hasService("fr.liglab.adele.icasa.device.box.Box");
      };

      BoxWidget.prototype.getStatusWindowTemplateURL = function() {
        return null;
      };

      BoxWidget.prototype.getDecorators = function() {
        return [];
      };

      BoxWidget.prototype.propHasChanged = function(device) {
        return null;
      };

      BoxWidget.prototype.init = function(deviceViewModel) {
        return null;
      };

      return BoxWidget;

    })(DeviceWidgetContract);
    instance = hub.createInstance(BoxWidget, {
      name: "box-widget-1"
    });
    console.log("BoxWidget module end loaded !!!");
    return instance;
  });

}).call(this);
