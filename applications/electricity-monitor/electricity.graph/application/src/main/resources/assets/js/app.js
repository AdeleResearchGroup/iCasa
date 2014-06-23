/*
 This file is part of Ext JS 4

 Copyright (c) 2011 Sencha Inc

 Contact:  http://www.sencha.com/contact

 GNU General Public License Usage
 This file may be used under the terms of the GNU General Public License version 3.0 as published by
 the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.
 Please review the following information to ensure the GNU General Public License version 3.0
 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

 If you are unsure which license is appropriate for your use,
 please contact the sales department at http://www.sencha.com/contact.
 */

var currentConsumption = 0;
var selectedVal = "total";

function changedVal(selected) {
    selectedVal = selected;
    currentConsumption = 0;
}

Ext.require('Ext.chart.*');

Ext.onReady(function () {
    var chart;
    var consumptionVal;
    var dropDown = document.getElementById("DropDownList");

    var Sock = function () {
        var socket;
        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }

        if (window.WebSocket) {
            // Compute the web socket url.
            // window.location.host includes the port
            var url = "ws://" + window.location.host + "/graph/update";
            socket = new WebSocket(url);
            socket.onopen = onopen;
            socket.onmessage = onmessage;
            socket.onclose = onclose;
        } else {
            alert("Your browser does not support Web Socket.");
        }

        function onopen(event) {
            console.log("Connected to the Monitoring WebSocket")
        }

        function onmessage(event) {
            consumptionVal = JSON.parse(event.data);

            if(consumptionVal.type == "sample")
            {
                if(selectedVal == consumptionVal.zone)
                {
                    currentConsumption = consumptionVal.consumption;
                }
            }else if(consumptionVal.type == "AddZone")
            {
                AddItem(consumptionVal.zones, consumptionVal.zones);
            }else if(consumptionVal.type == "RemoveZone")
            {
                RemoveItem(consumptionVal.zones, consumptionVal.zones);
            }else if(consumptionVal.type == "totalConsumption")
            {
                if(selectedVal == "total")
                {
                    currentConsumption = consumptionVal.total;
                }
            }
            generateData();
        }

        function onclose(event) {
            console.log("Disconnected from the Monitoring WebSocket")
        }
    };

    function AddItem(Text,Value)
    {
        // Create an Option object
        var opt = document.createElement("option");
        // Assign text and value to Option object
        opt.text = Text;
        opt.value = Value;
        // Add an Option object to Drop Down/List Box
        dropDown.options.add(opt);
    }

    function RemoveItem(Text,Value)
    {
        var i;

        for (i=0;i<dropDown.length;  i++) {
            if (dropDown.options[i].value==Value) {
                dropDown.remove(i);
            }
        }
    }

    var generateData = (function() {
        new Sock();
        var data = [], i = 0,
            last = false,
            date = new Date(2011, 1, 1),
            seconds = +date,
            min = Math.min,
            max = Math.max,
            random = Math.random;
        return function() {
            data = data.slice();
            data.push({
                date: Ext.Date.add(date, Ext.Date.DAY, i++),
                visits: currentConsumption
            });
            last = data[data.length -1];
            return data;
        };
    })();

    var group = false,
        groupOp = [{
            dateFormat: 'M d',
            groupBy: 'year,month,day'
        }, {
            dateFormat: 'M',
            groupBy: 'year,month'
        }];

    function regroup() {
        group = !group;
        var axis = chart.axes.get(1),
            selectedGroup = groupOp[+group];
        axis.dateFormat = selectedGroup.dateFormat;
        axis.groupBy = selectedGroup.groupBy;
        chart.redraw();
    }

    var store = Ext.create('Ext.data.JsonStore', {
        fields: ['date', 'visits'],
        data: generateData()
    });

    var intr = setInterval(function() {
        var gs = generateData();
        var toDate = timeAxis.toDate,
            lastDate = gs[gs.length - 1].date,
            markerIndex = chart.markerIndex || 0;
        if (+toDate < +lastDate) {
            markerIndex = 1;
            timeAxis.toDate = lastDate;
            timeAxis.fromDate = Ext.Date.add(Ext.Date.clone(timeAxis.fromDate), Ext.Date.DAY, 1);
            chart.markerIndex = markerIndex;
        }
        store.loadData(gs);
    }, 1000);

    Ext.create('Ext.Window', {
        width: 800,
        height: 600,
        minHeight: 400,
        minWidth: 550,
        hidden: false,
        maximizable: true,
        title: 'Electricity Consumption',
        renderTo: Ext.getBody(),
        layout: 'fit',
        items: [{
            xtype: 'chart',
            style: 'background:#fff',
            id: 'chartCmp',
            store: store,
            shadow: false,
            animate: true,
            axes: [{
                type: 'Numeric',
                minimum: 0,
                maximum: 1000,
                position: 'left',
                fields: ['visits'],
                title: 'Consumption [W]',
                grid: {
                    odd: {
                        fill: '#dedede',
                        stroke: '#ddd',
                        'stroke-width': 0.5
                    }
                }
            }, {
                type: 'Time',
                position: 'bottom',
                fields: 'date',
                title: 'Time',
                dateFormat: 'd',
                groupBy: 'year,month,day',
                aggregateOp: 'sum',

                constrain: true,
                fromDate: new Date(2011, 1, 1),
                toDate: new Date(2011, 1, 7),
                grid: true
            }],
            series: [{
                type: 'line',
                smooth: false,
                axis: ['left', 'bottom'],
                xField: 'date',
                yField: 'visits',
                label: {
                    display: 'none',
                    field: 'visits',
                    renderer: function(v) { return v >> 0; },
                    'text-anchor': 'middle'
                },
                markerConfig: {
                    radius: 5,
                    size: 5
                }
            }]
        }]
    });
    chart = Ext.getCmp('chartCmp');
    var timeAxis = chart.axes.get(1);
});

