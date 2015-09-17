
var nodes, edges, network,statePanel;

function registerWebSocket() {
    var ws = $.easyWebSocket("ws://" + window.location.host + "/temperature");
    ws.onmessage = function (event) {
        var messageFromServer = event.data;
        $("#temperature").html(messageFromServer);
    };

}

function updateNodeStatePanel(data){
    var panel = $("<div></div>").attr('class',"panel panel-primary");
    var panelHeading =  $("<div>Node State</div>").attr('class',"panel-heading");
    var panelBody =  $("<div></div>").attr('class',"panel-body");
    var stateGroupList =  $("<ul></ul>").attr('class',"list-group");
    $.each(data,function(key,val){
        console.log("KEY : " + key + " , VAL : " + val);
        var stateGroupListItem =  $("<li>"+key+"</li>").attr('class',"list-group-item");
        var stateGroupListValue =  $("<span>"+val+"</span>").attr('class',"badge");
        stateGroupListValue.appendTo(stateGroupListItem);
        stateGroupListItem.appendTo(stateGroupList);
        stateGroupList.appendTo(panelBody);
    });

    /** Append all to panel**/
    panelHeading.appendTo(panel);
    panelBody.appendTo(panel);
    panel.appendTo($("#statePanel"));
}

function draw(){
    // create a network
    var container = document.getElementById('mynetwork');
    statePanel = $("statePanel");
    // provide the data in the vis format
    var data = {
        nodes: nodes,
        edges: edges
    };
    var options = {interaction:{hover:true}};
    network = new vis.Network(container, data, options);

    network.on("selectEdge", function (params) {
        var edgeToUpdate =  params["edges"];
        console.log('selectEdge Event:', edgeToUpdate);
        edgeToUpdate.forEach(function(y){
            console.log("Edge Name " + edges.get(y).name);
            edges.update({id: y,label: edges.get(y).name});
            var url = "/context/relations/"+y;
            var t = $.get(url,function(data) {
                console.log('Edge Event Get Response:', data);
            });
        });
    });

    network.on("deselectEdge", function (params) {
        var edgeToUpdate =  params["previousSelection"]["edges"];
        console.log('deselectEdge Event:', edgeToUpdate);
        edgeToUpdate.forEach(function(y){
            edges.update({id: y,label: ""});
        });
    });

    network.on("selectNode", function (params) {
        var nodeToUpdate =  params["nodes"];
        console.log('selectNode Event:', nodeToUpdate);
        nodeToUpdate.forEach(function(y) {
            var url = "/context/entities/" + y;
            var t = $.get(url, function (data) {
                updateNodeStatePanel(data);
            });
        });
    });

    network.on("deselectNode", function (params) {
        var nodesToUpdate =  params["previousSelection"]["nodes"];
        console.log('deselectNode Event:', nodesToUpdate);
    });
}


function init() {
    //   registerWebSocket();
// create an array with nodes
    nodes = new vis.DataSet();
    edges = new vis.DataSet();
    var t = $.get("/context/entities",function(data) {
        var numberOfEntities = data.size;
        console.log(data.size);
        for(var i = 0;i<numberOfEntities;i ++){
            console.log(i);
            var entityId = "entity"+i;
            console.log(entityId);
            console.log(data[entityId]);
            var ContextEntityName = data[entityId];
            nodes.add({
                id: ContextEntityName,
                label: ContextEntityName
            });
        }
        draw();
    });

    var y = $.get("/context/relations",function(data) {
        var numberOfRelations = data.size;
        console.log(data.size);
        for(var i = 0;i<numberOfRelations;i ++){
            console.log(data["relation"+i+"name"]);
            console.log(data["relation"+i+"source"]);
            console.log(data["relation"+i+"end"]);
            var nodeId = data["relation"+i+"name"]+data["relation"+i+"source"]+data["relation"+i+"end"];
            edges.add({
                id: nodeId,
                from: data["relation"+i+"source"],
                to: data["relation"+i+"end"],
                arrows:'to',
                name: data["relation"+i+"name"]
            });
        }
        draw();
    });

}