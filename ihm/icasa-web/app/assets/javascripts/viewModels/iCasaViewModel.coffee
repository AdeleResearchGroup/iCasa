
define(['jquery',
        'jquery.ui',
        'backbone',
        'knockout',
        'knockback',
        'handlebars',
        'dataModels/ICasaDataModel'
        'text!templates/deviceTable.html',
        'text!templates/personTable.html',
        'text!templates/zoneTable.html',
        'text!templates/scriptPlayer.html',
        'text!templates/tabs.html',
        'text!templates/deviceStatusWindow.html',
        'text!templates/personStatusWindow.html',
        'text!templates/bathroomScaleStatusWindow.html',
        'domReady'],
  ($, ui, Backbone, ko, kb, HandleBars, DataModel, devTabHtml, personTabHtml, zoneTabHtml, scriptPlayerHtml, tabsTemplateHtml, deviceStatusWindowTemplateHtml, personStatusWindowTemplateHtml, bathroomScaleStatusWindowTemplateHtml) ->

    # HTML custom bindings

    ko.bindingHandlers.staticTemplate = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->

            # Next, whether or not the supplied model property is observable, get its current value
            valueUnwrapped = ko.utils.unwrapObservable(valueAccessor());
            if valueUnwrapped.template == undefined
              return {controlsDescendantBindings: true};

            templateUnwrapped = ko.utils.unwrapObservable(valueUnwrapped.template);
            $(element).html(templateUnwrapped);

            innerBindingContext = bindingContext.extend(valueUnwrapped.data);
            ko.applyBindingsToDescendants(innerBindingContext, element);

            return { controlsDescendantBindings: true };
            
         update: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->

            # Next, whether or not the supplied model property is observable, get its current value
            valueUnwrapped = ko.utils.unwrapObservable(valueAccessor());
            if valueUnwrapped.template == undefined
              return {controlsDescendantBindings: true};

            templateUnwrapped = ko.utils.unwrapObservable(valueUnwrapped.template);
            $(element).html(templateUnwrapped);

            innerBindingContext = bindingContext.extend(valueUnwrapped.data);
            ko.applyBindingsToDescendants(innerBindingContext, element);

            return { controlsDescendantBindings: true };

    };


    ko.bindingHandlers.handlebarTemplate = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            value = valueAccessor();

            # Next, whether or not the supplied model property is observable, get its current value
            valueUnwrapped = ko.utils.unwrapObservable(value);

            template = HandleBars.compile(valueUnwrapped, {});
            $(element).html(template);

            return { controlsDescendantBindings: false };

        update: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            value = valueAccessor();

            # Next, whether or not the supplied model property is observable, get its current value
            valueUnwrapped = ko.utils.unwrapObservable(value);

            template = HandleBars.compile(valueUnwrapped, {});
            $(element).html(template);
    };

    ko.bindingHandlers.jqueryDraggable = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            # This will be called when the binding is first applied to an element

            $(element).draggable( {
                compartment: "#mapContainer",
                scroll: true,
                opacity: 0.70,
                start: (event, eventUI) ->
                  viewModel.isSizeHighlightEnabled(false);
                stop: (event, eventUI) ->
                  viewModel.positionX(eventUI.position.left + (viewModel.widgetWidth() / 2)); #TODO add positionX and positionY for zones
                  viewModel.positionY(eventUI.position.top  + (viewModel.widgetHeight() / 2));
                  viewModel.model().save();
                  viewModel.isSizeHighlightEnabled(true);
            });

            return { controlsDescendantBindings: false };
    };

    ko.bindingHandlers.jquerySelectable = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            # This will be called when the binding is first applied to an element

            $(element).selectable( {
                selected: (event, eventUI) ->
                  if (viewModel.isHighlighted())
                    viewModel.removeHighlight();
                  else
                    viewModel.addHighlight();
                unselected: (event, eventUI) ->
                  viewModel.removeHighlight();
            });

            return { controlsDescendantBindings: false };
    };

    ko.bindingHandlers.jqueryDialog = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            # This will be called when the binding is first applied to an element

            titleUnwrapped = ko.utils.unwrapObservable(valueAccessor());

            $(element).dialog({
                autoOpen: false,
                title: titleUnwrapped
                minWidth: 300
            });
#            $(element).dialog({
#                autoOpen: false,
#                position: {my: "center", at: "center", of: "#statusWindows"},
#                title: titleUnwrapped
#            })
#            .parent().resizable({
#                containment: "#statusWindows"
#            }).draggable({
#                containment: "#statusWindows",
#                opacity: 0.70
#            });

            return { controlsDescendantBindings: false };

        update: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            # This will be called when the binding is first applied to an element

            titleUnwrapped = ko.utils.unwrapObservable(valueAccessor());
            $(element).dialog("option", "title", titleUnwrapped);

            visibleUnwrapped = ko.utils.unwrapObservable(viewModel.statusWindowVisible);

            if (visibleUnwrapped)
              $(element).dialog( "open" );
            else
              $(element).dialog( "close" );

            return { controlsDescendantBindings: false };
    };

    ko.bindingHandlers.jqueryTooltip = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            # This will be called when the binding is first applied to an element

            titleUnwrapped = ko.utils.unwrapObservable(valueAccessor());

            $(element).tooltip(
                content: titleUnwrapped,
                items: "img[alt]"
            );

            return { controlsDescendantBindings: false };

        update: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            # This will be called when the binding is first applied to an element

            titleUnwrapped = ko.utils.unwrapObservable(valueAccessor());

            $(element).tooltip( "option", "content", titleUnwrapped);

            return { controlsDescendantBindings: false };
    };

    ko.bindingHandlers.jqueryTabs = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            # This will be called when the binding is first applied to an element

            tabHeadersTemplate = HandleBars.compile(tabsTemplateHtml);
            htmlString = tabHeadersTemplate();
            addTabDiv = (tab) ->
                tabContent = tab.tabTemplate;
                tabTemplateParams = { tabId: tab.tabHtmlId(), content: tabContent };
                htmlString = htmlString + '<div id="' + tab.tabHtmlId() + '">' + tabContent + '</div>';

            addTabDiv(tab) for tab in viewModel.tabs();

            $(element).html(htmlString);

            return { controlsDescendantBindings: false };
    };

    # View models

    class NamedViewModel extends kb.ViewModel
        constructor: (model) ->
           super(model);
           @id = kb.observable(model, 'id');
           @name = kb.observable(model, 'name');

    class ZoneViewModel extends NamedViewModel
        constructor: (model) ->
           super(model)
           @isRoom = kb.observable(model, 'isRoom');
           @leftX = kb.observable(model, 'leftX');
           @rightX = kb.observable(model, 'rightX');
           @bottomY = kb.observable(model, 'bottomY');
           @topY = kb.observable(model, 'topY');

    class ScriptViewModel extends NamedViewModel
        constructor: (model) ->
           super(model)
           @state = kb.observable(model, 'state');

    class DecoratorViewModel extends NamedViewModel
        constructor: (model) ->
           super(model);
           @id(model.name);
           @name = kb.defaultObservable(kb.observable(model, 'name'), 'state');

           @show = kb.defaultObservable(kb.observable(model, 'show'), false);
           @imgSrc = kb.defaultObservable(kb.observable(model, 'imgSrc'), '/assets/images/devices/decorators/play.png');
           @positionX = kb.defaultObservable(kb.observable(model, 'positionX'), 16);
           @positionY = kb.defaultObservable(kb.observable(model, 'positionY'), 16);
           @width = kb.defaultObservable(kb.observable(model, 'width'), 15);
           @height = kb.defaultObservable(kb.observable(model, 'height'), 15);
           @sizeFactor=ko.observable(1.0);
           @containerSizeDelta=ko.observable(0);
           @widgetWidth = ko.computed({
              read: () =>
                effWidth = @width() * @sizeFactor();
                return effWidth;
              owner: @
               }
           , @);
           @widgetHeight = ko.computed({
              read: () =>
                effHeight = @height() * @sizeFactor();
                return effHeight;
              owner: @
               }
           , @);
           @styleWidth = ko.computed({
              read: () =>
                return @widgetWidth() + "px";
              owner: @
               }
           , @);
           @styleHeight = ko.computed({
              read: () =>
                return @widgetHeight() + "px";
              owner: @
           }
           , @);
           @styleLeft = ko.computed({
              read: () =>
                if ((@sizeFactor() > 1.0) && ((@widgetWidth() - @width()) >= @containerSizeDelta()))
                  effPostionX = @positionX();
                else
                  effPositionX = @positionX() + (@containerSizeDelta() / 2) - ((@widgetWidth() - @width()) / 2);
                return effPositionX + "px";
              owner: @
           }
           , @);
           @styleTop = ko.computed({
              read: () =>
                if ((@sizeFactor() > 1.0) && ((@widgetHeight() - @height()) >= @containerSizeDelta()))
                  effPositionY = @positionY();
                else
                  effPositionY = @positionY() + (@containerSizeDelta() / 2)- ((@widgetHeight() - @height()) / 2);
                return effPositionY + "px";
              owner: @
           }
           , @);


    class DeviceTypeViewModel extends NamedViewModel
        constructor: (model) ->
           super(model);

    class DeviceViewModel extends NamedViewModel
        constructor: (model) ->
           super(model);

           @location = kb.observable(model, 'location');
           @properties=kb.observable(model, 'properties');
           @state = kb.defaultObservable(kb.observable(model, 'state'), 'activated');
           @isDesactivated = ko.computed({
              read: () =>
                return @state() == "deactivated";
              owner: @
           }
           , @);
           @fault = kb.defaultObservable(kb.observable(model, 'fault'), 'no');
           @positionX = kb.defaultObservable(kb.observable(model, 'positionX'), 0);
           @positionY = kb.defaultObservable(kb.observable(model, 'positionY'), 0);
           @sizeFactor=ko.observable(1.0);
           @widgetWidth = ko.computed({
              read: () =>
                effWidth = 32 * @sizeFactor();
                return effWidth;
              owner: @
           }
           , @);
           @widgetHeight = ko.computed({
              read: () =>
                effHeight = 32 * @sizeFactor();
                return effHeight;
              owner: @
           }
           , @);
           @styleWidth = ko.computed({
              read: () =>
                return @widgetWidth() + "px";
              owner: @
           }
           , @);
           @styleHeight = ko.computed({
              read: () =>
                return @widgetHeight() + "px";
              owner: @
           }
           , @);
           @styleLeft = ko.computed({
              read: () =>
                effPositionX = @positionX() - (@widgetWidth() / 2);
                return effPositionX + "px";
              owner: @
           }
           , @);
           @styleTop = ko.computed({
              read: () =>
                effPositionY = @positionY() - (@widgetHeight() / 2);
                return effPositionY + "px";
              owner: @
           }
           , @);
           @zones = kb.collectionObservable(DataModel.collections.zones, {view_model: ZoneViewModel});
           @locationZone = ko.computed({
              read: () =>
                zoneModel = @zones.collection().get(@location());
                if (zoneModel == undefined)
                  return null;
                return @zones.viewModelByModel(zoneModel);
              owner: @
           }
           );
           @tooltipContent = ko.computed( () =>
              return @name() + " /n" + @id();
           , @);
           @statusWindowTitle = ko.computed( () =>
             return @name();
           , @);
           @statusWindowTemplate = ko.observable(deviceStatusWindowTemplateHtml);
           @type = kb.observable(model, 'type');
           @imgSrc = ko.computed(() =>
              imgName = "NewDevice";
              if (@type() == "iCASA.Cooler")
                 imgName = "airConditionne";
              if (@type() == "iCASA.AudioSource")
                 imgName = "sourceSonore";
              if (@type() == "iCASA.DimmerLight")
                 imgName = "lampeVariable";
              if (@type() == "iCASA.Thermometer")
                 imgName = "thermometre";
              if (@type() == "iCASA.Heater")
                 imgName = "radiateur";
              if (@type() == "iCASA.Photometer")
                 imgName = "Photometer";
              if (@type() == "iCASA.BinaryLight")
                 imgName = "lampe";
              if (@type() == "iCASA.PresenceSensor")
                 imgName = "detecteurMouvements";
              if (@type() == "iCASA.Speaker")
                 imgName = "hautParleur";
              if (@type() == "iCASA.Power")
                 imgName = "Power";
              if (@type() == "iCASA.BathroomScale")
                 imgName = "pesePersonne";

              return "/assets/images/devices/" + imgName + ".png";
           , @);
           @decorators = ko.observableArray([
                new DecoratorViewModel new Backbone.Model {
                    name: "event",
                    imgSrc: '/assets/images/devices/decorators/event.png',
                    show: false},
                new DecoratorViewModel new Backbone.Model {
                    name: "fault",
                    imgSrc: '/assets/images/devices/decorators/warning.png',
                    show: false},
                new DecoratorViewModel new Backbone.Model {
                    name: "activated",
                    imgSrc: '/assets/images/devices/decorators/play.png',
                    show: true},
                new DecoratorViewModel new Backbone.Model {
                    name: "on-top",
                    imgSrc: '/assets/images/devices/pesePersonnePieds.png',
                    width: 32,
                    height: 32,
                    positionX: 1,
                    positionY: 1,
                    show: false}
           ]);
           @statusWindowVisible = ko.observable(false);
           @updateWidgetImg= (newValue) =>
                activatedState = false;
                if ("activated" == @state())
                    activatedState = true;
                faultState = false;
                if ("yes" == @fault())
                    faultState = true;

                ko.utils.arrayForEach(@decorators(), (decorator) ->
                    if (decorator.name() == "activated")
                        decorator.show(activatedState && !faultState);
                    if (decorator.name() == "fault")
                        decorator.show(activatedState && faultState);
                );
           @isSizeHighlightEnabled = ko.observable(true);
           @isHighlighted = ko.observable(false);
           @addHighlight= () =>
                @isHighlighted(true);
                @updateSize(@isSizeHighlightEnabled());
           @removeHighlight= () =>
                @isHighlighted(false);
                @updateSize(@isSizeHighlightEnabled());
           @updateSize= (isSizeHighlightEnabledVal) =>
                if (isSizeHighlightEnabledVal && @isHighlighted())
                  newFactor = 1.2;
                else
                  newFactor = 1.0;
                @sizeFactor(newFactor);
                ko.utils.arrayForEach(@decorators(), (decorator) ->
                  decorator.sizeFactor(newFactor);
                  containerSizeDelta = 0;
                  if (newFactor != 1.0)
                    containerSizeDelta = 32 * (newFactor - 1.0);
                  decorator.containerSizeDelta(containerSizeDelta);
                );
           @isSizeHighlightEnabled.subscribe(@updateSize);

           # location change saving
           @saveLocation= ko.observable(false);
           @saveLocationChanges= (data, event) =>
                @location('bedroom');
                @saveChanges();
           @saveChanges= () =>
                @.model().saveChanges();

          
           # init
           @updateBathroomScaleDecorator= (newValue) =>
                presence = @properties().presence_detected;
                ko.utils.arrayForEach(@decorators(), (decorator) ->
                     if (decorator.name() == "on-top")
                          if (presence == true)
                               decorator.show(true);
                          else
                               decorator.show(false);
                );
          
           @initBahtroomScale= () =>
                if (@type() == "iCASA.BathroomScale")
                     ko.utils.arrayForEach(@decorators(), (decorator) ->
                         if (decorator.name() == "on-top")
                             decorator.show(true);
                     );
                     @statusWindowTemplate(bathroomScaleStatusWindowTemplateHtml);
                     @properties.subscribe(@updateBathroomScaleDecorator);
           @initBahtroomScale();
           
           @state.subscribe(@updateWidgetImg);
           @fault.subscribe(@updateWidgetImg);
           @updateWidgetImg();


    class PersonTypeViewModel extends NamedViewModel
        constructor: (model) ->
           super(model);


    class PersonViewModel extends kb.ViewModel
        constructor: (model) ->
           super(model);

           @location = kb.observable(model, 'location');
           @positionX = kb.defaultObservable(kb.observable(model, 'positionX'), 0);
           @positionY = kb.defaultObservable(kb.observable(model, 'positionY'), 0);
           @sizeFactor=ko.observable(1.0);
           @widgetWidth = ko.computed({
              read: () =>
                effWidth = 50 * @sizeFactor();
                return effWidth;
              owner: @
           }
           , @);
           @widgetHeight = ko.computed({
              read: () =>
                effHeight = 50 * @sizeFactor();
                return effHeight;
              owner: @
           }
           , @);
           @styleWidth = ko.computed({
              read: () =>
                return @widgetWidth() + "px";
              owner: @
           }
           , @);
           @styleHeight = ko.computed({
              read: () =>
                return @widgetHeight() + "px";
              owner: @
           }
           , @);
           @styleLeft = ko.computed({
              read: () =>
                effPositionX = @positionX() - (@widgetWidth() / 2);
                return effPositionX + "px";
              owner: @
           }
           , @);
           @styleTop = ko.computed({
              read: () =>
                effPositionY = @positionY() - (@widgetHeight() / 2);
                return effPositionY + "px";
              owner: @
           }
           , @);
           @zones = kb.collectionObservable(DataModel.collections.zones, {view_model: ZoneViewModel});
           @locationZone = ko.computed({
              read: () =>
                zoneModel = @zones.collection().get(@location());
                if (zoneModel == undefined)
                  return null;
                return @zones.viewModelByModel(zoneModel);
              owner: @
           }
           );
           @type = kb.observable(model, 'type');
           @personTypes = kb.collectionObservable(DataModel.collections.personTypes, {view_model: PersonTypeViewModel});
           @typeObj = ko.computed({
              read: () =>
                typeModel = @personTypes.collection().get(@type());
                if (typeModel == undefined)
                  return null;
                return @personTypes.viewModelByModel(typeModel);
              owner: @
           }
           );
           @tooltipContent = ko.computed( () =>
              return @name() + " /n" + @id();
           , @);
           @statusWindowTitle = ko.computed( () =>
              return @name();
           , @);
           @statusWindowTemplate = personStatusWindowTemplateHtml;
           @statusWindowVisible = kb.defaultObservable(@_statusWindowVisible, false);
           @imgSrc = ko.computed(() =>
              imgName = "user2";
              if (@type() == "Grandmother")
                imgName = "user1";
              if (@type() == "Grandfather")
                imgName = "user2";
              if (@type() == "Father")
                imgName = "user3";
              if (@type() == "Mother")
                imgName = "user4";
              if (@type() == "Girl")
                imgName = "user5";
              if (@type() == "Boy")
                imgName = "user6";
              if (@type() == "Sherlock")
                imgName = "user7";

              return "/assets/images/users/" + imgName + ".png";
           , @);
           @decorators = ko.observableArray([  ]);
           @isSizeHighlightEnabled = ko.observable(true);
           @isHighlighted = ko.observable(false);
           @addHighlight= () =>
              @isHighlighted(true);
              updateSize(@isSizeHighlightEnabled());
           @removeHighlight= () =>
              @isHighlighted(false);
              updateSize(@isSizeHighlightEnabled());
           @updateSize= (isSizeHighlightEnabledVal) =>
              if (isSizeHighlightEnabledVal && @isHighlighted())
                newFactor = 1.2;
              else
                newFactor = 1.0;
              @sizeFactor(newFactor);
              ko.utils.arrayForEach(@decorators(), (decorator) ->
                decorator.sizeFactor(newFactor);
                containerSizeDelta = 0;
                if (newFactor != 1.0)
                  containerSizeDelta = 50 * (newFactor - 1.0);
                decorator.containerSizeDelta(containerSizeDelta);
              );
           @isSizeHighlightEnabled.subscribe(@updateSize);
           @saveChanges= () =>
               @.model().saveChanges();

    class TabViewModel extends kb.ViewModel
        constructor: (model) ->
           @id = model.id;
           @name = ko.observable(model.name);
           @tabTemplate = model.template;
           @tabHtmlId = ko.computed( () =>
               return "tab-" + @id;
           , @);
           @tabHtmlIdRef = ko.computed( () =>
               return "#" + @tabHtmlId();
           , @);

    class ICasaViewModel extends kb.ViewModel
        constructor : (model) ->

           @deviceTypes = kb.collectionObservable(DataModel.collections.deviceTypes, {view_model: DeviceTypeViewModel});

           @devices = kb.collectionObservable(DataModel.collections.devices, {view_model: DeviceViewModel} );

           @persons = kb.collectionObservable(DataModel.collections.persons, {view_model: PersonViewModel});

           @personTypes = kb.collectionObservable(DataModel.collections.personTypes, {view_model: PersonTypeViewModel});

           @zones = kb.collectionObservable(DataModel.collections.zones, {view_model: ZoneViewModel});

           @notRoomZones = ko.computed(() =>
                return ko.utils.arrayFilter(@zones, (zone) ->
                    return !zone.isRoom();
                );
           , @);

           @rooms = ko.computed(() =>
                return ko.utils.arrayFilter(@zones, (zone) ->
                    return zone.isRoom();
                );
           , @);

           @scripts = kb.collectionObservable(DataModel.collections.scripts, {view_model: ScriptViewModel});

           @tabs = ko.observableArray([
                new TabViewModel {
                    id: "devices",
                    name: "Devices",
                    template: devTabHtml},
                new TabViewModel {
                    id: "zones",
                    name: "Zones" ,
                    template: zoneTabHtml},
                new TabViewModel {
                    id: "persons",
                    name: "Persons" ,
                    template: personTabHtml},
                new TabViewModel {
                    id: "script-player",
                    name: "Script Player" ,
                    template: scriptPlayerHtml}
           ]);


           # device management

           @newDeviceType = ko.observable("");

#           @deviceTypes.subscribe(@.selectFirstDeviceType)
#           selectFirstDeviceType: (models) =>
#              if models.length > 0
#                firstModel = models[0]
#                this.newDeviceType(firstModel)

#           @mapSize = ko.computed({
#              height: $("mapImg").height()
#              width: $("mapImg").width()
#           });

           @deviceActivationStates = ko.observableArray([
              'activated',
              'deactivated',
              'unknown'
           ]);

           @deviceFaults = ko.observableArray([
              'yes',
              'no',
              'unknown'
           ]);

           @newDeviceName = ko.observable("");

           @createDevice = () =>
              newDevice = new DataModel.Models.Device({ deviceId: @newDeviceName(), name: @newDeviceName(), "type": @newDeviceType(), positionX: 1, positionY: 1 });
              DataModel.collections.devices.push(newDevice);
              newDevice.save();

           @removeDevice = (device) =>
              device.model().destroy();

           @showDeviceWindow = (device) =>
              device.statusWindowVisible(false);
              device.statusWindowVisible(true);


           # person management

           @newPersonName = ko.observable("");

           @newPersonType = ko.observable("Father");

           @createPerson = () =>
              newPerson = new DataModel.Models.Person({ personId: @newPersonName(), name: @newPersonName(), "type": @newPersonType(), positionX: 1, positionY: 1 });
              DataModel.collections.persons.push(newPerson);
              newPerson.save();

           @removePerson = (person) =>
              person.model().destroy();

           @showPersonWindow = (person) =>
              person.statusWindowVisible(false);
              person.statusWindowVisible(true);


           # zone management

           @newZoneName = ko.observable("");

           @createZone = () =>
              newZone = new DataModel.Models.Zone({ deviceId: @newZoneName(), name: @newZoneName(), isRoom: false, leftX: 1, topY: 1, rightX : 21, bottomY: 21 });
              DataModel.collections.zones.push(newZone);
              newZone.save();

           @newRoomName = ko.observable("");

           @createRoom = () =>
              newZone = new DataModel.Models.Zone({ zoneId: @newRoomName(), name: @newRoomName(), isRoom: true, leftX: 1, topY: 1, rightX : 21, bottomY: 21 });
              @zones.push(new ZoneViewModel(newZone));
              newZone.save();

           @removeZone = (zone) =>
              zone.model().destroy();


           # script management

           @selectedScript = ko.observable();

           @selectedScriptState = ko.computed( () =>
              if (@selectedScript())
                  return @selectedScript().state();
              else
                  return 'undefined';
           );

           @scriptStates = ko.observableArray([
              'started',
              'stopped',
              'paused',
              'undefined'
           ]);

           @startScript = () =>
              @selectedScript().state('started');
              @selectedScript().model().save();

           @stopScript = () =>
              @selectedScript().state('stopped');
              @selectedScript().model().save();

           @pauseScript = () =>
              @selectedScript().state('paused');
              @selectedScript().model().save();

    return ICasaViewModel;
);
