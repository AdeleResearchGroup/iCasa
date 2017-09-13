
define(['jquery',
        'jquery.ui',
        'backbone',
        'knockout',
        'knockback',
        'handlebars',
        'jquery.ui.touch',
        'contracts/DeviceWidgetContract',
        'contracts/ICasaManager',
        'contracts/ICasaShellManager',
        'dataModels/ICasaDataModel'
        'text!templates/deviceTable.html',
        'text!templates/deviceAppTable.html',
        'text!templates/personTable.html',
        'text!templates/zoneTable.html',
        'text!templates/applicationTable.html',
        'text!templates/scriptPlayer.html',
        'text!templates/tabs.html',
        'text!templates/deviceStatusWindow.html',
        'text!templates/personStatusWindow.html',
        'text!templates/zoneStatusWindow.html',
        'text!templates/applicationStatusWindow.html',
        'i18n!locales/nls/locale',
        'domReady'],
  ($, ui, Backbone, ko, kb, HandleBars, jqueryTouch, DeviceWidgetContract, ICasaManager, ICasaShell, DataModel, devTabHtml, devAppTabHtml, personTabHtml, zoneTabHtml, appTabHtml, scriptPlayerHtml, tabsTemplateHtml, deviceStatusWindowTemplateHtml, personStatusWindowTemplateHtml, zoneStatusWindowTemplateHtml, applicationStatusWindowTemplateHtml, locale) ->

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
    #for dashboard.
    ko.bindingHandlers.checkedAccessRight = {
        init: (element, valueAccessor, allBindingAccessor, viewModel, bindingContext) ->
            valueUnwrapped = ko.utils.unwrapObservable(valueAccessor());
            root = ko.utils.unwrapObservable(valueUnwrapped.root);
            new AccessRightHandler(element, viewModel, root)
            return { controlsDescendantBindings: true };
    };
    class AccessRightHandler
        constructor: (@button, @device, @root) ->
            #save access when clicked.
            $(@button).change( ()=>
                applicationModel = @root.selectedApplication().model()
                if !(applicationModel?) || (applicationModel.get('id') == "NONE")
                    return ;
                list = applicationModel.accessRights.where({'deviceId': @device.model().get('id')})
                if list.length < 1
                    return;
                policy = list[0]
                if policy.get('policy') == "hidden"
                    policy.set({policy:"total"})
                else
                    policy.set({policy: "hidden"})
                policy.save()
            )
            #initialization. check when has device access.
            applicationModel = @root.selectedApplication().model()
            policy = "hidden"
            if !(applicationModel?) || (applicationModel.get('id') == "NONE")
                policy = "total"
            list = applicationModel.accessRights.where({'deviceId': device.model().get('id')})
            if list.length>0
                policy = list[0].get("policy")
            if policy != "hidden"
                device.hasAccessRight(true)
            else
                device.hasAccessRight(false)

    #Carousel binding to retrieve selected application.
    ko.bindingHandlers.carouselHandler = {
        init: (element, valueAccessor, allBindingAccessor, viewModel, bindingContext) ->
            valueUnwrapped = ko.utils.unwrapObservable(valueAccessor());
            root = ko.utils.unwrapObservable(valueUnwrapped.root);
            new CarouselBindingHandler(element, root);
            return { controlsDescendantBindings: false };
    };
    class CarouselBindingHandler
        constructor: (@carousel, @root ) ->
            $(@carousel).bind('slid', ()=>
                items = $(@carousel).find('.item');
                index = items.index(items.filter('.active'));
                application = @root.applications.collection().at(index);
                @root.selectedApplication(@root.applications.viewModelByModel(application));
            )
    #end for dashboard.

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
                  #Zones does not utilise widgetWidth
                  if (viewModel instanceof ZoneViewModel)
                      viewModel.positionX((eventUI.position.left / viewModel.containerWidthRatio()) + (viewModel.width() / 2));
                      viewModel.positionY((eventUI.position.top / viewModel.containerHeightRatio())  + (viewModel.height() / 2));
                  else
                      viewModel.positionX((eventUI.position.left / viewModel.containerWidthRatio()) + (viewModel.widgetWidth() / 2));
                      viewModel.positionY((eventUI.position.top / viewModel.containerHeightRatio())  + (viewModel.widgetHeight() / 2));
                  viewModel.model().save();
                  viewModel.isSizeHighlightEnabled(true);
            });

            return { controlsDescendantBindings: false };
    };

    ko.bindingHandlers.jqueryResizable = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            # This will be called when the binding is first applied to an element

            $(element).resizable( {
                start: (event, eventUI) ->
                  viewModel.isSizeHighlightEnabled(false);
                stop: (event, eventUI) ->
                  #TODO add positionX and positionY for zones
                  rightX = eventUI.size.width / viewModel.containerWidthRatio() + viewModel.leftX();
                  bottomY = eventUI.size.height / viewModel.containerHeightRatio() + viewModel.topY();
                  width = rightX - viewModel.leftX();
                  height = bottomY - viewModel.topY();
                  viewModel.width(width);
                  viewModel.height(height);
                  viewModel.positionX(viewModel.leftX() + width / 2);
                  viewModel.positionY(viewModel.topY() + height / 2);
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
                    viewModel.isSelected(false);
                  else
                    viewModel.addHighlight();
                    viewModel.isSelected(true);
                unselected: (event, eventUI) ->
                  viewModel.removeHighlight();
                  viewModel.isSelected(false);
            });

            return { controlsDescendantBindings: false };
    };

    ko.bindingHandlers.jqueryDialog = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            # This will be called when the binding is first applied to an element
            titleUnwrapped = ko.utils.unwrapObservable(valueAccessor());
            isDialog = $(element).data('dialog')
            if !isDialog
                $(element).dialog({
                    autoOpen: false,
                    title: titleUnwrapped,
                    minWidth: 300,
                    width: "auto"
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
            $(element).resize(() ->
              $(element).tabs("refresh");
            );

            return { controlsDescendantBindings: false };
    };

    ko.bindingHandlers.sortable = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->

            asc = $('<i></i>').addClass("icon-chevron-down pull-right");
            desc = $('<i></i>').addClass("icon-chevron-up pull-right").hide();
            asc.click(()->
                desc.show()
                asc.hide()
                viewModel.sortFields(1, valueAccessor().obj, valueAccessor().column)
            )
            desc.click(()->
                asc.show()
                desc.hide()
                viewModel.sortFields(-1, valueAccessor().obj, valueAccessor().column)
            )
            $(element).append(asc);
            $(element).append(desc);

            return { controlsDescendantBindings: false };
    };

    ko.bindingHandlers.jqFactorSlider = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->
            options = allBindingsAccessor().sliderOpt || {}
            sliderValue = ko.utils.unwrapObservable(valueAccessor());

            options.slide = (e, ui) ->
                sliderValue.value(ui.value)

            options.stop = (e, ui) ->
                viewModel.clock.model().save();

            ko.utils.domNodeDisposal.addDisposeCallback(element, () ->
              $(element).slider("destroy")
            )
            $(element).slider(options)
        update: (element, valueAccessor) ->
            sliderValue = ko.toJS(valueAccessor())
            $(element).slider("value", sliderValue.value);

    }

    ko.bindingHandlers.jqScriptProgress = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->

            label = $("<div></div>").text("0%").width("100%").css("text-align", "center").addClass("pull-left")
            $(element).append(label)
            $(element).progressbar({
              value: valueAccessor().value,
              max: 100,
              change: () ->
                  label.text( $(element).progressbar( "value" ).toFixed(2) + "%" );
              complete: () ->
                label.text( "Complete" );
                viewModel.selectedScript().state('stopped');
            })

        update: (element, valueAccessor) ->
            valueProgress = ko.toJS(valueAccessor())
            $(element).progressbar("value", valueProgress.value);
    }

    ko.bindingHandlers.popOver = {

        init: (element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) ->

          $(element).popover({trigger:'hover'});
    }
    #Knockout Extender to round numbers
    ko.extenders.numeric = (target, precision) =>
        result = ko.computed({
            read: target;
            write: (newValue) =>
                current = target()
                if typeof newValue == "number"
                    roundingMultiplier = Math.pow(10, precision)
                    valueToWrite = Math.round(newValue * roundingMultiplier) / roundingMultiplier
                else
                    valueToWrite = newValue;
                target(valueToWrite);
                target.notifySubscribers(valueToWrite);
        });
        result(target());
        return result;

    # View models
    class PropertyViewModel extends kb.ViewModel
        constructor: (model) ->
            @name = kb.observable(model,'name');
            @value = kb.observable(model,'value').extend({ numeric: 2 });
            @visible = kb.observable(model,'visible');
            @unit = kb.observable(model,'unit');

    class NamedViewModel extends kb.ViewModel
        constructor: (model) ->
           super(model);
           @id = kb.observable(model, 'id');
           @name = kb.observable(model, 'name');
           @saveChanges= () =>
              @.model().saveChanges();


    class ScriptViewModel extends NamedViewModel
        constructor: (model) ->
           super(model)
           @state = kb.observable(model, 'state');


    class PositionedImageViewModel extends NamedViewModel
        constructor: (model) ->
           super(model);
           @imgSrc = kb.observable(model, 'imgSrc');
           @positionX = kb.observable(model, 'positionX');
           @positionY = kb.observable(model, 'positionY');
           @width = kb.defaultObservable(kb.observable(model, 'width'), 0);
           @height = kb.defaultObservable(kb.observable(model, 'height'), 0);
           @sizeFactor=ko.observable(1.0);
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
           @containerWidthRatio = ko.observable(1.0);
           @containerHeightRatio = ko.observable(1.0);


    class DecoratorViewModel extends PositionedImageViewModel
        constructor: (model) ->
           super(model);
           @id(model.name);
           @name = kb.defaultObservable(kb.observable(model, 'name'), 'state');

           @show = kb.defaultObservable(kb.observable(model, 'show'), false);
           @imgSrc = kb.defaultObservable(@imgSrc, '/assets/images/devices/decorators/play.png');
           @positionX = kb.defaultObservable(@positionX, 16);
           @positionY = kb.defaultObservable(@positionY, 16);
           if (@width() <= 0)
              @width(15);
           if (@height() <= 0)
              @height(15);
           @containerSizeDelta=ko.observable(0);
           @styleLeft = ko.computed({
              read: () =>
                if ((@sizeFactor() > 1.0) && ((@widgetWidth() - @width()) >= @containerSizeDelta()))
                  effPositionX = @positionX() + (@containerSizeDelta() / 2) - ((@widgetWidth() - @width()) / 2);
                else
                  effPositionX = @positionX();
                return effPositionX + "px";
              owner: @
           }
           , @);
           @styleTop = ko.computed({
              read: () =>
                if ((@sizeFactor() > 1.0) && ((@widgetHeight() - @height()) >= @containerSizeDelta()))
                  effPositionY = @positionY() + (@containerSizeDelta() / 2)- ((@widgetHeight() - @height()) / 2);
                else
                  effPositionY = @positionY();
                return effPositionY + "px";
              owner: @
           }
           , @);


    class DeviceTypeViewModel extends NamedViewModel
        constructor: (model) ->
           super(model);


    class DraggableStateWidgetViewModel extends PositionedImageViewModel
        constructor: (model) ->
           super(model);

           @styleLeft = ko.computed({
              read: () =>
                effPositionX = (@positionX() * @containerWidthRatio()) - (@widgetWidth() / 2);
                return effPositionX + "px";
              owner: @
           }
           , @);
           @styleTop = ko.computed({
              read: () =>
                effPositionY = (@positionY() * @containerHeightRatio()) - (@widgetHeight() / 2);
                return effPositionY + "px";
              owner: @
           }
           , @);
           @tooltipContent = ko.computed( () =>
              return @name() + " /n" + @id();
           , @);
           @statusWindowTitle = ko.computed( () =>
              return @name();
           , @);
           @isSelected = ko.observable(false);
           @isSizeHighlightEnabled = ko.observable(true);
           @isHighlighted = ko.observable(false);
           @addHighlight= () =>
              @isHighlighted(true);
              @updateSize(@isSizeHighlightEnabled());
           @removeHighlight= () =>
              @isHighlighted(false);
              @updateSize(@isSizeHighlightEnabled());
           @updateSize= (isSizeHighlightEnabledVal) =>
              if ((isSizeHighlightEnabledVal && @isHighlighted()) || @isSelected())
                newFactor = 1.2;
              else
                newFactor = 1.0;
              @sizeFactor(newFactor);
              if (@decorators != undefined)
                ko.utils.arrayForEach(@decorators(), (decorator) =>
                  decorator.sizeFactor(newFactor);
                  containerSizeDelta = 0;
                  if (newFactor != 1.0)
                    containerSizeDelta = @width() * (newFactor - 1.0);
                  decorator.containerSizeDelta(containerSizeDelta);
                );

           @isSizeHighlightEnabled.subscribe(@updateSize);

           @updateSelected=()=>
              if (@isSelected())
                  @addHighlight();
              else
                  @removeHighlight();
           @selectedSubscription = @isSelected.subscribe(@updateSelected)

           # status window management
           @statusWindowTemplate = ko.observable("");
           @statusWindowVisible = ko.observable(false);


    class ZoneViewModel extends DraggableStateWidgetViewModel
      constructor: (model) ->
        super(model)
        @isRoom = kb.observable(model, 'isRoom');
        @leftX = kb.observable(model, 'leftX');
        @rightX = kb.observable(model, 'rightX');
        @bottomY = kb.observable(model, 'bottomY');
        @topY = kb.observable(model, 'topY');
        #Handle variables
        @variables = kb.observable(model, 'variables');
        @variablesModel = new DataModel.Models.Properties(model.get('variables'));
        @collectionVariables = kb.collectionObservable(@variablesModel, {view_model: PropertyViewModel});

        @filterFunction = (model)=>
          return !model.get('visible');
        #Filtered properties
        @filtered_variables = kb.collectionObservable(@variablesModel,{filters:@filterFunction,view_model:PropertyViewModel  });
        @updateVariables = (newValue) =>
            newVariables =  @model().get('variables');
            ko.utils.arrayForEach(newVariables, (variable)=>
                storedVariable = @.variablesModel.get(variable.name);
                #This is done cause when modifying model, extend function is not well handled by kb :(
                if storedVariable?
                    if typeof variable.value == "number"
                        roundingMultiplier = Math.pow(10, 2)
                        valueToWrite = Math.round(variable.value * roundingMultiplier) / roundingMultiplier
                    else
                        valueToWrite = variable.value
                    storedVariable.set('value',valueToWrite );
                else
                    @.variablesModel.add(variable);
            );
        @variables.subscribe(@updateVariables);
        @updateVariables();
        @isSelected(true);

        @variables_name = ko.computed({
          read: () =>
            if (@.variables() instanceof Object)
              return Object.keys(@.variables());
            else
              return [];
          owner: @
        }
        , @);

        # To override the resize event when selected zone.
        @sizeFactor = ko.computed({
          read: () =>
            return 1.0;
          write: (newValue)=>
            return ;
          }, @);
        @width = ko.computed({
           read:()=>
             return (@rightX() - @leftX());
           write:(value)=>
             @rightX(@leftX() + value);
        },@);
        @height = ko.computed({
           read:()=>
             return (@bottomY() - @topY());
           write:(value)=>
             @bottomY(@topY() + value);
        },@);
        @positionX = ko.computed({
           read:()=>
             return (@leftX() + @rightX()) / 2;
           write:(value)=>
             _rightX = value + @width()/2;
             _leftX = value - @width()/2;
             @rightX(_rightX);
             @leftX(_leftX);
        },@);
        @positionY = ko.computed({
           read:()=>
             return (@bottomY() + @topY()) / 2;
           write:(value)=>
             _topY = value - @height()/2;
             _bottomY = value + @height()/2;
             @topY(_topY);
             @bottomY(_bottomY);
        },@);

        @visibility = ko.computed({
          read:()=>
            if (@isSelected())
              return "visible";
            else
              return "hidden";
          }
          , @);
        @styleLeft = ko.computed({
              read: () =>
                effPositionX = (@positionX() * @containerWidthRatio()) - (@width() * @containerWidthRatio() / 2);
                return effPositionX + "px";
              owner: @
          }
          , @);
        @styleTop = ko.computed({
              read: () =>
                effPositionY = (@positionY() * @containerHeightRatio()) - (@height() * @containerHeightRatio() / 2);
                return effPositionY + "px";
              owner: @
          }
          , @);
        @styleWidth = ko.computed({
              read: () =>
                effWidth = @width() * @containerWidthRatio();
                return effWidth + "px";
              owner: @
          }
          , @);
        @styleHeight = ko.computed({
              read: () =>
                effHeight = @height() * @containerHeightRatio();
                return effHeight + "px";
              owner: @
          }
          ,@);
        @background = @.generateBackgroundColor();
        @borderColor = @background;
        @statusWindowTemplate(zoneStatusWindowTemplateHtml);
      getInputEltId:(variableName)->
        return @.id() + "#zoneprop#" + variableName+"";

      generateBackgroundColor:()->
        r = Math.floor(Math.random() * 256);
        g = Math.floor(Math.random() * 256);
        b = Math.floor(Math.random() * 256);
        return "rgba("+r+","+g+","+b+", 0.4)";


    class DeviceViewModel extends DraggableStateWidgetViewModel
        constructor: (model) ->
           super(model);
           @propertiesModel = new DataModel.Models.Properties(model.get('properties'));
           @type = kb.observable(model, 'type');
           @location = kb.observable(model, 'location');
           @services = kb.observable(model, 'services');
           @showNameInMap = ko.observable(false);
           @deviceWidget = ko.observable();
           @hasWidget = ko.computed({
             read: () =>
               return (@deviceWidget() != null) && (@deviceWidget() != undefined);
             owner: @
           }
           , @);
           @hasService = (service) =>
            curServices = @.services();
            if (!curServices)
              return false;
            foundService = ko.utils.arrayFirst(curServices, (curService) ->
              return curService.indexOf(service) == 0;
              #return ko.utils.stringStartsWith(curService, service);
            );
            if (foundService)
              return true;
            else
              return false;
           @collectionProperties = kb.collectionObservable(@propertiesModel, {view_model: PropertyViewModel});
           @properties = kb.observable(model,'properties');

           @filterFunction = (model)=>
                return !model.get('visible');

           @filtered_properties = kb.collectionObservable(@propertiesModel,{filters:@filterFunction,view_model:PropertyViewModel  });


           @state = kb.defaultObservable(kb.observable(model, 'state'), 'activated');
           @isDesactivated = ko.computed({
              read: () =>
                return @state() == "deactivated";
              owner: @
           }
           , @);
           @fault = kb.defaultObservable(kb.observable(model, 'fault'), 'no');
           @positionX = kb.defaultObservable(@positionX, 0);
           @positionY = kb.defaultObservable(@positionY, 0);
           @width(32);
           @height(32);
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
           #Valid only for dashboard.
           @hasAccessRight = ko.observable(true)
           @blurDevice = ko.computed({
              read: () =>
                return !@hasAccessRight();
              owner:@
           });
           #End valid for dashboard.
           @statusWindowTemplate(deviceStatusWindowTemplateHtml);
           @imgSrc = ko.observable(@.getImage())
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
                    show: true}
           ]);
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
                #@imgSrc(@.getImage()); #image has been already setted

           # location change saving
           @saveLocation= ko.observable(false);
           @saveLocationChanges= (data, event) =>
                @location('bedroom'); #TODO manage location property change
                @saveChanges();

           # init
           @updateProperties= (newValue) =>
                newProperties =  @model().get('properties');
                ko.utils.arrayForEach(newProperties, (property)=>
                    storedProperty = @.propertiesModel.get(property.name);
                    #This is done cause when modifying model, extend function is not well handled by kb :(
                    if storedProperty?
                        if typeof property.value == "number"
                            roundingMultiplier = Math.pow(10, 2)
                            valueToWrite = Math.round(property.value * roundingMultiplier) / roundingMultiplier
                        else
                            valueToWrite = property.value
                        storedProperty.set('value',valueToWrite );
                    else
                        @.propertiesModel.add(property);
                );
                if ((@type() == "iCasa.Heater") || @hasService("fr.liglab.adele.icasa.device.temperature.Heater"))
                  powerLevel = @.getPropertyValue("heater.powerLevel");
                  isHot = (powerLevel != null) && (powerLevel > 0.0);
                  ko.utils.arrayForEach(@decorators(), (decorator) ->
                     if (decorator.name() == "hot")
                       decorator.show(isHot == true);
                  );
                if ((@type() == "iCasa.Cooler") || @hasService("fr.liglab.adele.icasa.device.temperature.Cooler"))
                  powerLevel = @.getPropertyValue("cooler.powerLevel");
                  isCold = (powerLevel != null) && (powerLevel > 0.0);
                  ko.utils.arrayForEach(@decorators(), (decorator) ->
                    if (decorator.name() == "cold")
                      decorator.show(isCold == true);
                  );
                if (@type() == "iCasa.BathroomScale" )
                  presence = @.getPropertyValue("presence_detected");
                  ko.utils.arrayForEach(@decorators(), (decorator) ->
                     if (decorator.name() == "foots")
                          decorator.show(presence == true);
                  );
                if (@type() == "iCasa.Sphygmometer" )
                  presence = @.getPropertyValue("presence_detected");
                  ko.utils.arrayForEach(@decorators(), (decorator) ->
                     if (decorator.name() == "sphygmometer_measure")
                       if (presence == true)
                         decorator.show(presence == true);
                  );
                if ((@type() == "iCasa.PresenceSensor") || @hasService("fr.liglab.adele.icasa.device.presence.PresenceSensor"))
                  presence = @.getPropertyValue("presenceSensor.sensedPresence");
                  ko.utils.arrayForEach(@decorators(), (decorator) ->
                    if (decorator.name() == "presence")
                      decorator.show(presence == true);
                  );
                if ((@type() == "iCasa.DimmerLight") || @hasService("fr.liglab.adele.icasa.device.light.DimmerLight"))
                  powerLevel = @.getPropertyValue("dimmerLight.powerLevel");
                  if (powerLevel == null)
                    @imgSrc(@.getImage());
                  else if (powerLevel >= 0.75)
                    @imgSrc(@getImage("dimmerLight_full"));
                  else if (powerLevel >= 0.50)
                    @imgSrc(@getImage("dimmerLight_high"));
                  else if (powerLevel >= 0.25)
                    @imgSrc(@getImage("dimmerLight_medium"));
                  else if (powerLevel > 0.0)
                    @imgSrc(@getImage("dimmerLight_low"));
                  else
                    @imgSrc(@.getImage());
                if ((@type() == "iCasa.BinaryLight") || @hasService("fr.liglab.adele.icasa.device.light.BinaryLight"))
                  powerLevel = @.getPropertyValue("binaryLight.powerStatus");
                  if (powerLevel)
                    @imgSrc(@getImage("binaryLight_on"));
                  else
                    @imgSrc(@.getImage());
                if ((@type() == "iCasa.Sprinkler") || @hasService("fr.liglab.adele.icasa.device.sprinkler.Sprinkler"))
                  powerStatus = @.getPropertyValue("sprinkler.powerStatus");
                  if (powerStatus)
                    @imgSrc(@getImage("sprinkler"));
                  else
                    @imgSrc(@.getImage());
                if ((@type() == "iCasa.COGasSensor") || @hasService("fr.liglab.adele.icasa.device.gasSensor.CarbonMonoxydeSensor"))
                  concentration = @.getPropertyValue("carbonMonoxydeSensor.currentConcentration");
                  ko.utils.arrayForEach(@decorators(), (decorator) ->
                     if (decorator.name() == "redLed")
                       decorator.show(concentration >= 2.0);
                  );
                if ((@type() == "iCasa.CO2GasSensor") || @hasService("fr.liglab.adele.icasa.device.gasSensor.CarbonDioxydeSensor"))
                  concentration = @.getPropertyValue("carbonDioxydeSensor.currentConcentration");
                  ko.utils.arrayForEach(@decorators(), (decorator) ->
                     if (decorator.name() == "redLed")
                       decorator.show(concentration >= 2.0);
                  );
                if (@hasWidget())
                  @deviceWidget().propHasChanged(@);

           @recomputeDecorators = () =>
             ko.utils.arrayForEach(@decorators(), (decorator) =>
               #TODO better manage default decorators
               if ((decorator.name() != "event") && (decorator.name() != "fault") && (decorator.name() != "activated"))
                 @decorators.remove(decorator);
             );
             if (@hasWidget())
               additionalDecorators = @deviceWidget().getDecorators();
               if ((additionalDecorators != null) && (additionalDecorators != undefined))
                 ko.utils.arrayForEach(additionalDecorators, (decoratorDef) =>
                   decorator = new DecoratorViewModel new Backbone.Model {
                     name: decoratorDef.name,
                     show: false
                   };
                   if (decoratorDef.url?)
                     decorator.imgSrc(decoratorDef.url);
                   if (decoratorDef.show?)
                     decorator.show(decoratorDef.show);
                   if (decoratorDef.positionX?)
                     decorator.positionX(decoratorDef.positionX);
                   if (decoratorDef.positionY?)
                     decorator.positionY(decoratorDef.positionY);
                   if (decoratorDef.width?)
                     decorator.width(decoratorDef.width);
                   if (decoratorDef.height?)
                     decorator.height(decoratorDef.height);
                   @decorators.push(decorator);
                 );

           @initDeviceImages= () =>
                if ((@type() == "iCasa.Heater") || @hasService("fr.liglab.adele.icasa.device.temperature.Heater"))
                     @decorators.push(new DecoratorViewModel new Backbone.Model {
                       name: "hot",
                       imgSrc: '/assets/images/devices/decorators/heater-hot-decorator-top.png',
                       width: 28,
                       height: 13,
                       positionX: 2,
                       positionY: 3,
                       show: false
                     });
                 if ((@type() == "iCasa.Cooler") || @hasService("fr.liglab.adele.icasa.device.temperature.Cooler"))
                     @decorators.push(new DecoratorViewModel new Backbone.Model {
                       name: "cold",
                       imgSrc: '/assets/images/devices/decorators/cooler-cold-decorator-top.png',
                       width: 28,
                       height: 13,
                       positionX: 2,
                       positionY: 3,
                       show: false
                     });
                     ko.utils.arrayForEach(@decorators(), (decorator) ->
                        if ((decorator.name() == "activated") || (decorator.name() == "fault"))
                           decorator.positionY(1);
                           decorator.positionX(0);
                     );
                if (@type() == "iCasa.BathroomScale")
                     @decorators.push(new DecoratorViewModel new Backbone.Model {
                       name: "foots",
                       imgSrc: '/assets/images/devices/decorators/foots.png',
                       width: 32,
                       height: 32,
                       positionX: 1,
                       positionY: 1,
                       show: false
                     });
                if (@hasService("fr.liglab.adele.icasa.device.presence.PresenceSensor"))
                     @decorators.push(new DecoratorViewModel new Backbone.Model {
                       name: "presence",
                       imgSrc: '/assets/images/devices/decorators/movementDetector_detected.png',
                       width: 32,
                       height: 32,
                       positionX: 1,
                       positionY: 1,
                       show: false
                     });
                if ((@type() == "iCasa.COGasSensor") || (@type() == "iCasa.CO2GasSensor"))
                     @decorators.push(new DecoratorViewModel new Backbone.Model {
                       name: "redLed",
                       imgSrc: '/assets/images/devices/decorators/redLed.png',
                       width: 8,
                       height: 8,
                       positionX: 4,
                       positionY: 4,
                       show: false
                     });
                     ko.utils.arrayForEach(@decorators(), (decorator) ->
                       if ((decorator.name() == "activated") || (decorator.name() == "fault"))
                         decorator.positionY(1);
                         decorator.positionX(0);
                     );
                if (@type() == "iCasa.Sphygmometer")
                     @decorators.push(new DecoratorViewModel new Backbone.Model {
                       name: "sphygmometer_measure",
                       imgSrc: '/assets/images/devices/decorators/sphygmometer_measure.png',
                       width: 12,
                       height: 9,
                       positionX: 17,
                       positionY: 4,
                       show: false
                     });
                if (@hasWidget())
                  @recomputeDecorators();
                @properties.subscribe(@updateProperties);
                @updateProperties();

           @updateWidgetDecorators = (newValue) =>
             @recomputeDecorators();
             if (@hasWidget())
               @updateProperties();

           @initDeviceImages();

           @state.subscribe(@updateWidgetImg);
           @fault.subscribe(@updateWidgetImg);
           @updateWidgetImg();
           @popoverdata = ()=>
             pop = "Name : "+@.name()
             return pop;

           @deviceWidget.subscribe(@updateWidgetImg);
           @deviceWidget.subscribe(@updateWidgetDecorators);

        getPropertyValue:(property)=>
          value = _.find(@propertiesModel.models, (propertyModel) ->
            if propertyModel.get('name') == property
              return propertyModel;
          );
          if ((value == undefined) || (value == null))
            return null;
          else
            return value.get('value');
        getImage:(imgName)->
          if not imgName?
            imgName = "genericDevice";
            #if (@hasWidget())
            #  return @deviceWidget().getBaseIconURL();
            if ((@type() == "iCasa.Cooler") || @hasService("fr.liglab.adele.icasa.device.temperature.Cooler"))
              imgName = "cooler-off";
            if ((@type() == "iCasa.AudioSource") || @hasService("fr.liglab.adele.icasa.device.sound.AudioSource"))
              imgName = "musicPlayer";
            if ((@type() == "iCasa.Sprinkler") || @hasService("fr.liglab.adele.icasa.device.sprinkler.Sprinkler"))
              imgName = "sprinkler_off";
            if ((@type() == "iCasa.BinaryLight") || @hasService("fr.liglab.adele.icasa.device.light.BinaryLight"))
              imgName = "binaryLight_off";
            if ((@type() == "iCasa.DimmerLight") || @hasService("fr.liglab.adele.icasa.device.light.DimmerLight"))
              imgName = "dimmerLight_off";
            if ((@type() == "iCasa.Thermometer") || @hasService("fr.liglab.adele.icasa.device.temperature.Thermometer"))
              imgName = "thermometer";
            if ((@type() == "iCasa.MedicalThermometer") || @hasService("fr.liglab.adele.icasa.device.bathroomscale.MedicalThermometer"))
              imgName = "medicalThermometer";
            if ((@type() == "iCasa.Heater") || @hasService("fr.liglab.adele.icasa.device.temperature.Heater"))
              imgName = "heater";
            if ((@type() == "iCasa.Photometer") || @hasService("fr.liglab.adele.icasa.device.light.Photometer"))
              imgName = "photometer";
            if ((@type() == "iCasa.COGasSensor") || @hasService("fr.liglab.adele.icasa.device.gazSensor.CarbonMonoxydeSensor"))
              imgName = "COGazSensor";
            if ((@type() == "iCasa.CO2GasSensor") || @hasService("fr.liglab.adele.icasa.device.gazSensor.CarbonDioxydeSensor"))
              imgName = "CO2GazSensor";
            if ((@type() == "iCasa.MotionSensor") || @hasService("fr.liglab.adele.icasa.device.motion.MotionSensor"))
              imgName = "movementDetector";
            if ((@type() == "iCasa.Speaker") || @hasService("fr.liglab.adele.icasa.device.sound.Speaker"))
              imgName = "speaker";
            if (@type() == "iCasa.Power")
              imgName = "power";
            if ((@type() == "iCasa.BathroomScale") || @hasService("fr.liglab.adele.icasa.device.bathroomscale.BathroomScale"))
              imgName = "bathroomScale";
            if (@type() == "iCasa.Tablet")
              imgName = "tablet";
            if (@type() == "iCasa.Desktop")
              imgName = "desktop";
            if ((@type() == "iCasa.SettopBox") || @hasService("fr.liglab.adele.icasa.device.settopbox.SetTopBox") || @hasService("fr.liglab.adele.icasa.device.box.Box"))
              imgName = "liveBox";
            if (@type() == "iCasa.RollingShutter")
              imgName = "rollingShutter";
            if (@type() == "iCasa.FloodSensor")
              imgName = "liquidDetector";
            if (@type() == "iCasa.DoorWindowSensor")
              imgName = "doorMagneticSensor";
            if (@type() == "iCasa.SmartPhone")
              imgName = "smartPhone";
            if (@type() == "iCasa.FlatTV")
              imgName = "flatTV";
            if (@type() == "iCasa.RFIDReader")
              imgName = "rfidReader";
            if (@type() == "iCasa.Accelerometer")
              imgName = "accelerometer";
            if ((@type() == "iCasa.ToggleSwitch") || @hasService("fr.liglab.adele.icasa.device.power.PowerSwitch"))
              imgName = "toggleSwitch";
            if (@type() == "iCasa.DoorDetector")
              imgName = "doorDetector";
            if (@type() == "iCasa.SettopBox")
              imgName = "liveBox"; #override settopbox icon
            if ((@type() == "iCasa.Sphygmometer") || @hasService("fr.liglab.adele.icasa.device.bathroomscale.Sphygmometer"))
              imgName = "sphygmometer";
            if ((@type() == "iCasa.PushButton") || @hasService("fr.liglab.adele.icasa.device.button.PushButton"))
              imgName = "pushButton";
            if (@type() == "iCasa.Camera")
              imgName = "camera";
            if (@type() == "iCasa.Siren")
              imgName = "siren";
            if (@hasService("fr.liglab.adele.icasa.device.presence.PresenceSensor"))
              imgName = "movementDetector";
          return "/assets/images/devices/" + imgName + ".png";

    #Valid only for dashboard.
    class ApplicationViewModel extends NamedViewModel
        constructor: (model) ->
            super(model)
            # status window management
            @statusWindowTemplate = ko.observable(applicationStatusWindowTemplateHtml);
            @statusWindowVisible = ko.observable(false);
            @applicationStatus = kb.observable(model,'status')
            @isRunning = ko.computed(() =>
                if @applicationStatus() == "Started"
                    return true;
                return false;
            )
            @changeStatus = () =>
                console.log "changing app status"
                if @applicationStatus() == "Started"
                    #@applicationStatus("Stopped");
                    model.set({status: "Stopped"})
                else
                    #@applicationStatus("Started");
                    model.set({status: "Started"})

            @imgName = ko.computed(() =>
                imgName = "unknown"
                if @id() == "binary.light.follow.me"
                    imgName = "follow1"
                if @id() == "dimmer.light.follow.me"
                    imgName = "follow1"
                if @id() ==  "test.light.follow.me"
                    imgName = "follow1"
                if @id() ==  "light.follow.me.with.motion.sensor"
                    imgName = "follow1"
                if @id() ==  "light.follow.me.with.motion.sensor.application"
                    imgName = "follow1"
                if @id() ==  "LightFollowMeWithMotionSensor"
                    imgName = "follow1"
                if @id() ==  "FollowMeWithPhotometerApplication"
                  imgName = "follow1"
                if @id() ==  "actimetrics"
                    imgName = "actimetrics"
                if @id() ==  "ActimetricManager"
                    imgName = "actimetrie"
                if @id() ==  "habits.monitoring"
                    imgName = "actimetrics"
                if @id() ==  "light.follow.me.with.motion.sensor.and.photometer"
                    imgName = "follow1"
                if @id() == "health.management"
                	  imgName= "gererSante1"
                return imgName;
            );
            @imgIcon = ko.computed(() =>
                return "/assets/images/applications/#{@imgName()}_48.png";
            );
            @imgSrc = ko.computed(() =>
                return "/assets/images/applications/#{@imgName()}.png";
            );


     #End valid only for dashboard

    class PersonTypeViewModel extends NamedViewModel
        constructor: (model) ->
           super(model);


    class PersonViewModel extends DraggableStateWidgetViewModel
        constructor: (model) ->
           super(model);

           @location = kb.observable(model, 'location');
           @positionX = kb.defaultObservable(@positionX, 0);
           @positionY = kb.defaultObservable(@positionY, 0);
           #width and height are in 48 instead of 50 to avoid weird bug in firefox-driver in selenium
           #when selecting objects
           @width(48);
           @height(48);
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
           @statusWindowTemplate(personStatusWindowTemplateHtml);
           @imgSrc = ko.computed(() =>
              imgName = "user2";
              if (@type() == "Grandmother" || @type() == "Old woman")
                imgName = "user1";
              if (@type() == "Grandfather" || @type() == "Old man")
                imgName = "user2";
              if (@type() == "Father" || @type() == "Man")
                imgName = "user3";
              if (@type() == "Mother" || @type() == "Woman")
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

    class ClockViewModel extends kb.ViewModel
        constructor: (@model) ->
          super(model);

          @currentTime = kb.observable(model, 'currentTime');
          @startDate = kb.observable(model, 'startDate');
          @pause = kb.observable(model, 'pause');
          @factor = kb.observable(model, 'factor');
          @factorTmp = kb.observable(model,'factor');
          @factorEditing = ko.observable(false);
          @updateFactor = ()=>
                if !@factorEditing() && @factorTmp() != @.model().previous('factor')
                    @.model().save();

          @factorEditing.subscribe(@updateFactor);
          @minutes = ko.computed({
             read: =>
               d = new Date(@currentTime())
               return d.getMinutes()

             write: (val)=>
               d = new Date(@currentTime())
               d.setMinutes(val)
               @currentTime(d.getTime())
          }, @)

          @hours = ko.computed({
             read: =>
               d = new Date(@currentTime())
               return d.getHours()

             write: (val)=>
               d = new Date(@currentTime())
               d.setHours(val)
               @currentTime(d.getTime())
          }, @)

          @date = ko.computed({
             read: =>
               d = new Date(@currentTime())
               day = if d.getDate() < 10 then '0'+ d.getDate() else d.getDate()
               month = if d.getMonth() < 9 then '0'+ (d.getMonth()+1) else (d.getMonth()+1)
               return day + "/" + month + "/" + d.getFullYear();

             write: (val)=>
               d = new Date(@currentTime())
               d.setFullYear(val.substring(6,10))
               d.setMonth(val.substring(3,5)-1)
               d.setDate(val.substring(0,2))
               @currentTime(d.getTime())
          }, @)



    class ICasaViewModel extends kb.ViewModel

        #
        # HUBU component implementation
        #
        hub: null;
        name: null;
        shell: null;

        getComponentName: ->
          return "iCasaViewModel-" + @name;

        configure: (theHub, config) ->
          @hub = theHub;

          @hub.requireService({
             component: @,
             contract:  DeviceWidgetContract,
             bind:      "bindDeviceWidget",
             unbind:    "unbindDeviceWidget",
             aggregate : true,
             optional : true
          });
          @hub.requireService({
             component: @,
             contract:  ICasaShell,
             bind:      "bindShell",
             unbind:    "unbindShell",
             aggregate : false,
             optional : false
          });

          @hub.provideService({
            component: @,
            contract: ICasaManager
          });

        #shell
        bindShell: (svc) ->
          @shell = svc;

        unbindShell: (svc) ->
          @shell = null;

        #device widget
        bindDeviceWidget: (svc) ->
          console.log("bindDeviceWidget");
          @deviceWidgets.push(svc);

        unbindDeviceWidget: (svc) ->
          console.log("unbindDeviceWidget");
          @deviceWidgets.remove(svc);

        start: ->
             console.log("start iCasaViewModel");
             null; #workaround for Coffeescript compilation issue
        stop: ->
             console.log("stop iCasaViewModel");
             null; #workaround for Coffeescript compilation issue

        getBackendVersion: () ->
            return @backendVersion();

        getFrontendVersion: () ->
            return @frontendVersion();

        #
        # ViewModel implementation
        #

        constructor : (model) ->
           @name = model.id;
           #to determine if is a dashboard or a simulator.
           @servletType = model.servletType;
           #to handle localization.
           @getLocaleMessage = (name) ->
              return locale[name];
           #add tabs.
           tabsItems = [];
           if (@servletType == "dashboard")
             deviceTab = new TabViewModel ({
                id: "devices",
                name: @getLocaleMessage('Devices'),
                template: devAppTabHtml});
             zoneTab = new TabViewModel ({
              id: "zones",
              name: @getLocaleMessage('Zones') ,
              template: zoneTabHtml});
             appTab = new TabViewModel ({
              id: "applications",
              name: @getLocaleMessage('Applications') ,
              template: appTabHtml});

             tabsItems.push(deviceTab);
             tabsItems.push(zoneTab);
             tabsItems.push(appTab);
           else #simulator
             deviceTab = new TabViewModel ({
               id: "devices",
               name: @getLocaleMessage('Devices'),
               template: devTabHtml});
             zoneTab = new TabViewModel ({
               id: "zones",
               name: @getLocaleMessage('Zones') ,
               template: zoneTabHtml});
             personTab = new TabViewModel ({
               id: "persons",
               name: @getLocaleMessage('Persons') ,
               template: personTabHtml});
             scriptTab = new TabViewModel ({
               id: "script-player",
               name: @getLocaleMessage('Script.Player') ,
               template: scriptPlayerHtml});
             tabsItems.push(deviceTab);
             tabsItems.push(zoneTab);
             tabsItems.push(personTab);
             tabsItems.push(scriptTab);


           @tabs = ko.observableArray(tabsItems);

           @deviceWidgets = ko.observableArray();


           @imgSrc = ko.observable(model.imgSrc);
           @mapWidth = ko.observable(0);
           @mapHeight = ko.observable(0);
           @computeMapImgSize = () =>
             imgSrcNoCache = @imgSrc() + '?cache=' + Date.now();
             iCasaViewModel = @;
             $('<img/>').attr('src', imgSrcNoCache).load(() ->
               iCasaViewModel.mapWidth(this.width);
               iCasaViewModel.mapHeight(this.height);
             );
           @computeMapImgSize();
           @mapWidthRatio = ko.observable(1.0);
           @mapHeightRatio = ko.observable(1.0);
           @updateMapSize = () =>
              mapElt = $("#mapImg")
              mapEffWidth = mapElt.width();
              mapEffHeight = mapElt.height();
              if (@mapWidth() > 0)
                @mapWidthRatio(mapEffWidth / @mapWidth());
              if (@mapHeight() > 0)
                @mapHeightRatio(mapEffHeight / @mapHeight());

           @deviceTypes = kb.collectionObservable(DataModel.collections.deviceTypes, {view_model: DeviceTypeViewModel});

           @devices = kb.collectionObservable(DataModel.collections.devices, {view_model: DeviceViewModel} );
           @updateDeviceWidgets= (newValue) =>
             ko.utils.arrayForEach(@devices(), (device) =>
               if (device == undefined)
                 return;

               if (!device.hasWidget())
                 ko.utils.arrayForEach(@deviceWidgets(), (deviceWidget) =>
                   if (deviceWidget.manageDevice(device))
                     device.deviceWidget(deviceWidget);
                 );
             );
           @.devices.subscribe(@.updateDeviceWidgets);

           @persons = kb.collectionObservable(DataModel.collections.persons, {view_model: PersonViewModel});

           @personTypes = kb.collectionObservable(DataModel.collections.personTypes, {view_model: PersonTypeViewModel});

           @zones = kb.collectionObservable(DataModel.collections.zones, {view_model: ZoneViewModel});

           @clock = new ClockViewModel(DataModel.models.clock);

           @scripts = kb.collectionObservable(DataModel.collections.scripts, {view_model: ScriptViewModel});

           @updateExecutingScript = (newValue) =>
             ko.utils.arrayForEach(@scripts(), (script) =>
               if (script == undefined)
                 return;
               if (script.state() == "started")
                 @selectedScript(script);

             );

           @.scripts.subscribe(@.updateExecutingScript);

           #valid only for dashboard
           if @servletType == "dashboard"
             @selectedApplication = ko.observable("")
             @applications = kb.collectionObservable(DataModel.collections.applications, {view_model: ApplicationViewModel});
             @updateSelectedApplication = ()=>
                console.log "calculating selected application rights"
                ko.utils.arrayForEach(@.devices(), (device) =>
                    applicationModel = @selectedApplication().model()
                    if (applicationModel.get('id') == "NONE")
                        device.hasAccessRight(true)
                    else
                        list = applicationModel.accessRights.where({'deviceId': device.model().get('id')})
                        policy = "hidden"
                        if list.length>0
                            policy = list[0].get("policy")
                        if policy != "hidden"
                            device.hasAccessRight(true)
                        else
                            device.hasAccessRight(false)
                );
             @updatingAccessRightInSelectedApplication = kb.observable(DataModel.collections.applications.updateAccessRights,'update')
           #@devices.subscribe(@updateSelectedApplication);
           #end valid only for dashboard


           # device management

           @newDeviceType = ko.observable("");

#           @deviceTypes.subscribe(@.selectFirstDeviceType)
#           selectFirstDeviceType: (models) =>
#              if models.length > 0
#                firstModel = models[0]
#                this.newDeviceType(firstModel)



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
              generatedId = @.createRandomId(DataModel.collections.devices, @newDeviceType().name());
              newDevice = new DataModel.Models.Device({ deviceId: generatedId, name: @newDeviceName(), "type": @newDeviceType().name(), positionX: 1, positionY: 1, properties: []});
              newDevice.save();
              #newDevice.set(id: generatedId)
              #DataModel.collections.devices.push(newDevice);

           @removeDevice = (device) =>
              device.model().destroy();

           @removeSelectedDevices = () =>
             toRemoveModels = []
             ko.utils.arrayForEach(@devices(), (device) =>
               if (device == undefined)
                 return;

               if (device.isSelected())
                  toRemoveModels.push device.model()
             );
             for toRemoveModel in toRemoveModels
                toRemoveModel.destroy()

           @showDeviceWindow = (device) =>
              device.statusWindowVisible(false);
              device.statusWindowVisible(true);

           @deviceFilter = ko.observable("");
           @zoneFilter = ko.observable("");
           @personFilter = ko.observable("");
           #Valid only for dashboard.
           @applicationFilter = ko.observable("");
           @showApplicationWindow = (application) =>
             application.statusWindowVisible(false);
             application.statusWindowVisible(true);
           #End valid for dashboard.

           @sortFields = (order, list, property) =>
             @[list+"s"]().sort((a,b) ->
               if a[property]() > b[property]()
                 return 1*order
               else if b[property]() > a[property]()
                 return -1*order
               else
                 return 0
             )
             tps = @[list+"Filter"]();
             @[list+"Filter"]("\\");
             @[list+"Filter"](tps);

           @filteredList = (list) =>
              console.log "filtering " + list
              opt = @[list+"Filter"]().split("=");
              if opt.length <= 1
                attr = "name";
                search = opt[0];
              else
                attr = opt[0];
                search = opt[1];

              try
                filter = new RegExp(search, "i");
              catch err
                console.log("Bad regexp : "+err.message);
                filter = new RegExp("", "i")
              return ko.utils.arrayFilter( @[list+"s"](), (element) ->
                try
                  if attr == "name" or attr == "state" or attr == "location" or attr == "fault" or attr == "id"
                    return filter.test( element[attr]() )
                  else
                    return filter.test( element.getPropertyValue(attr) )
                catch err
                  return false
              )

           @filteredDevices = ko.computed(() =>
              if @servletType == "dashboard"
                  #Valid for dashboard.
                  return ko.utils.arrayFilter(@devices(),(device) =>
                    applicationModel = DataModel.collections.applications.get(@selectedApplication().model().get('id'))
                    if !(applicationModel?) || (@selectedApplication().model().get('id') == "NONE")
                        return true;
                    list = applicationModel.accessRights.where({'deviceId': device.model().get('id')})
                    if(list.length>0)
                        return true;
                    return false;
                  )
              else
                #for simulator
                return @filteredList("device") #commented for dashboard.
           , @)

           @filteredZones = ko.computed(() =>
              return @filteredList("zone")
           , @)

           @filteredPersons = ko.computed(() =>
              return @filteredList("person")
           , @)

           @checkAllDevices = ko.computed({
              read: =>
                selected = 0
                for device in @devices()
                  if device.isSelected()
                    selected++
                return @devices().length == selected

              write: (val) =>
                for device in @devices()
                  device.isSelected(val)
           }, @)

           #Valid only for dashboard.
           if @servletType == "dashboard"
             @filteredApplications = ko.computed(() =>
               return @filteredList("application")
             , @)
             @disableAccessRight = ko.computed(() =>
               if (!(@selectedApplication().model?) )
                 return true;
               if (@selectedApplication().model().get('id') == "NONE")
                 return true;
               return false;
             , @)
           #End valid for dashboard.

           # person management

           @newPersonName = ko.observable("");

           @newPersonType = ko.observable("Father");

           @createPerson = () =>
              newPerson = new DataModel.Models.Person({ personId: @newPersonName(), name: @newPersonName(), "type": @newPersonType().name(), positionX: 1, positionY: 1 });
              newPerson.save();
              #newPerson.set(id: @newPersonName())
              #DataModel.collections.persons.push(newPerson);
              @newPersonName("");



           @removeSelectedPersons = () =>
              toRemoveModels = []
              ko.utils.arrayForEach(@persons(), (person) =>
                if (person == undefined)
                  return;

                if (person.isSelected())
                  toRemoveModels.push person.model();
              );
              for toRemoveModel in toRemoveModels
                toRemoveModel.destroy()

           @removePerson = (person) =>
              person.model().destroy();

           @showPersonWindow = (person) =>
              person.statusWindowVisible(false);
              person.statusWindowVisible(true);

           @checkAllPersons = ko.computed({
             read: =>
               selected = 0
               for person in @persons()
                 if person.isSelected()
                   selected++
               return @persons().length == selected
             write: (val) =>
               for person in @persons()
                 person.isSelected(val)
           }, @)

           # zone management

           @newZoneName = ko.observable("");

           @createZone = () =>
              newZone = new DataModel.Models.Zone({ zoneId: @newZoneName(), name: @newZoneName(), isRoom: false, leftX: 1, topY: 1, rightX : 50, bottomY: 50 , isSelected: true, variables: []});
              newZone.save();
              #newZone.set(id: @newZoneName());
              #DataModel.collections.zones.push(newZone);


           @removeSelectedZones = () =>
              toRemoveModels = []
              ko.utils.arrayForEach(@zones(), (zone) =>
                if (zone == undefined)
                  return;

                if (zone.isSelected())
                  toRemoveModels.push zone.model();
              );
              for toRemoveModel in toRemoveModels
                toRemoveModel.destroy();

           @removeZone = (zone) =>
              zone.model().destroy();

           @showZoneWindow = (zone) =>
              zone.statusWindowVisible(false);
              zone.statusWindowVisible(true);

           @checkAllZones = ko.computed({
             read: =>
               selected = 0
               for zone in @zones()
                 if zone.isSelected()
                   selected++
               return @zones().length == selected
             write: (val) =>
               for zone in @zones()
                 zone.isSelected(val)
           }, @)

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
              if (@selectedScript())
                startDate =  @selectedScript().startDate()
                d = new Date(startDate.substring(6,10),startDate.substring(3,5)-1,startDate.substring(0,2),startDate.substring(11,13),startDate.substring(14,16),startDate.substring(17,19),0)
                @clock.startDate(d.getTime())
                @clock.currentTime(d.getTime())
                @selectedScript().state('started');
                @selectedScript().model().save();

           @stopScript = () =>
              if (@selectedScript())
                @selectedScript().state('stopped');
                @selectedScript().model().save();

           @pauseScript = () =>
              if (@selectedScript())
                @selectedScript().state('paused');
                @selectedScript().model().save();

           @newScriptName = ko.observable("");

           @saveScript = ()=>
              newName = @newScriptName().replace(".bhv", "") + ".bhv";
              newScript = new DataModel.Models.Script({ scriptId: newName, name: newName, state: "stopped"});
              newScript.save();
              newScript.set(id: newName);
              DataModel.collections.scripts.push(newScript);
              @newScriptName("");
           @resetState = ()=>
              DataModel.resetState();

           @scriptStartDate = ko.computed({
              read: =>
                 if (@selectedScript())
                   return @selectedScript().startDate().substring(0,10)
                 else
                   d = new Date()
                   day = if d.getDate() < 10 then '0'+ d.getDate() else d.getDate()
                   month = if d.getMonth() < 9 then '0'+ d.getMonth() else d.getMonth()
                   return day + "/" + month + "/" + d.getFullYear();
              write: (date)=>
                @selectedScript().startDate(date+"-00:00:00")
           }, @)

           @scriptEndDate = ko.computed( () =>
              if @selectedScript()
                dateStr = @selectedScript().startDate()
                d = new Date(dateStr.substring(6,10),dateStr.substring(3,5)-1,dateStr.substring(0,2),dateStr.substring(11,13),dateStr.substring(14,16),dateStr.substring(17,19),0)
                timestamp = d.getTime() + (@selectedScript().executionTime() ) # * 60 * 1000. It is currently in ms
                d = new Date(timestamp)
                day = if d.getDate() < 10 then '0'+ d.getDate() else d.getDate()
                return day + "/" + (d.getMonth()+1) + "/" + d.getFullYear();
              else
                d = new Date()
                day = if d.getDate() < 10 then '0'+ d.getDate() else d.getDate()
                return day + "/" + (d.getMonth()+1) + "/" + d.getFullYear();
           );

           # NOT USED for now
           @selectedScriptFactor = ko.computed({
              read: =>
                if (@selectedScript())
                  return @selectedScript().factor()
                else
                  return 1;

              write: (val)=>
                if(@selectedScript())
                  @selectedScript().factor(val)
           }, @)

           @maxFactor = ko.computed( ()=>
             clockFactor = @clock.factor();
             if (clockFactor)
               if (clockFactor > 15000)
                 return clockFactor;
               else
                 return 15000;
             else
               return 15000;
           )

           @scriptProgress = ko.computed( ()=>
              if (@selectedScript())
                if ( (@selectedScript().executionTime() != 0) && ( (@selectedScript().state() == 'started') || (@selectedScript().state() == 'paused')))
                  scriptTime = @clock.currentTime() - @clock.startDate()
                  dateStr = @selectedScript().startDate()
                  d = new Date(dateStr.substring(6,10),dateStr.substring(3,5)-1,dateStr.substring(0,2),dateStr.substring(11,13),dateStr.substring(14,16),dateStr.substring(17,19),0)
                  executionTimeMs = d.getTime() + (@selectedScript().executionTime() ) # * 60 * 1000. It is currently in ms
                  #executionTimeMs = (@selectedScript().executionTime() ) ##* 60 * 1000 It is currently in ms
                  return ((scriptTime * 1000) / executionTimeMs) ;
                  #return (1 - ( (executionTimeMs - scriptTime) / executionTimeMs) ) * 100
                else
                  return 0
              else
                return 0
           );

           @startClockTimer = ()=>
              timer = ()=>
                if (! @clock.pause())
                  currentTime = @clock.currentTime()
                  @clock.currentTime(currentTime + (500 * @clock.factor())) # multiply for 500 'cause it will be executed each 500ms
              setInterval(timer, 500);
              #timer();

           @pauseClock = ()=>
              @clock.pause(true);
              @clock.model().save();

           @startClock = ()=>
              @clock.pause(false);
              @clock.model().save();

           # init clock state
           #@updateClockTimer= (newValue) =>
           #  clockPaused = newValue;
           #  if (! clockPaused)
           #    @startClockTimer();

           #@clock.pause.subscribe(@updateClockTimer);
           @startClockTimer();

           # managing map size change (must update position of persons, zones and devices)
           @updateModelPosition=(model)=>
             model.containerWidthRatio(@mapWidthRatio());
             model.containerHeightRatio(@mapHeightRatio());

           @updateWidgetPositions= (newValue) =>
             #TODO should use a merged list of positioned objects
             ko.utils.arrayForEach(@devices(), (device) =>
              @updateModelPosition(device);
             );
             ko.utils.arrayForEach(@persons(), (person) =>
              @updateModelPosition(person);
             );
             ko.utils.arrayForEach(@zones(), (zone) =>
               @updateModelPosition(zone);
             );
           @mapWidthRatio.subscribe(@updateWidgetPositions);
           @mapHeightRatio.subscribe(@updateWidgetPositions);

           @.persons.subscribe(@.updateWidgetPositions)
           @.devices.subscribe(@.updateWidgetPositions)
           @.zones.subscribe(@.updateWidgetPositions)
           #shell command.
           @command = ko.observable("help")
           @executeShellCommand = () =>
             params = @.command().split(" ");
             name = params[0];
             params.shift();#remove first element(command name).
             @shell.exec(name,params);

           @showHelp = () =>
             @shell.exec("help",[]);

           @executeShellCommandEvent = (data, event) =>
             if event.keyCode == 13
               event.preventDefault();
               @executeShellCommand();
             return true;

           #valid for dashboard
           if @servletType == "dashboard"
             @updatingAccessRightInSelectedApplication.subscribe(@updateSelectedApplication);
             @selectedApplication.subscribe(@updateSelectedApplication);
             @.devices.subscribe(@updateSelectedApplication)
             @updateSelectedApplication();
           #end valid for dashboard

        createRandomId:(collection, type)->
           number = Math.floor((Math.random()*Number.MAX_VALUE)+1);
           hexaNumner = number.toString(16).substr(0,10);
           nid = type.replace("iCasa.", "") + "-" + hexaNumner;
           testExistance = collection.get(nid);
           if (testExistance != undefined && testExistance != null)
              return createRandomId(collection, type);
           return nid;


    return ICasaViewModel;
);
