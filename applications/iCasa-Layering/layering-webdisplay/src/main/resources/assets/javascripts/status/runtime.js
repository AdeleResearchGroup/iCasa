function runtime(){
	document.getElementById("runtime").innerHTML = "";
    $.ajax({url: "http://localhost:9000/icasa/layers/context", type: "GET"}).done(function(resources){
        //console.log(resources);//every context Entity
        var numberOfbubbles=resources.length;
		var bubbleRadius=rSize(RaC(numberOfbubbles).length);
		var bubbleSeparation=bubbleRadius*5;
		var coordinates=IcG(PoE(numberOfbubbles,bubbleSeparation),bubbleRadius);
		//console.log(coordinates);
		var globalIndex=0,bubbleIndex=0, smallBubbleIndex=0,Bubbles=[],BubbleBig=[],bubbleSmall=[];
		var bubbleInfo=[];
		var lines=[];
		var lineindex=0;
		var yoffset=0.8;
		//fill the bubble info except the coordinates, color and radius.
        for(i=0;i<numberOfbubbles;i++){
            //var A=resources[i]["functional_extensions"][0];
            var functionalExtRelations=resources[i]["extensions"][0];

            if (functionalExtRelations!=undefined){
                //A=A["connections"];
                if(functionalExtRelations["relations"].length>0){
                    functionalExtRelations=functionalExtRelations["relations"][0]["sourcesId"];
                }else{
                    functionalExtRelations=[];
                }

            }else{
                functionalExtRelations=[];
            }

           function matchImplementedSpecs(coreImplSpc,extensions){//mekes a list with ALL the implemented specifications
               var list=[];
               list=list.concat(coreImplSpc);
               for(e=0;e<extensions.length;e++){
                   list=list.concat(extensions[e].implementedSpecifications);
               }
               return list;
            }

           if(resources[i]["state"]=="valid"){
               BubbleBig[i]={
                   id:resources[i]["id"],
                   layers:matchImplementedSpecs(resources[i]["core"].implementedSpecifications,resources[i]["extensions"]),
                   status:resources[i]["state"],
                   stroke:"black",
                   functionalCore:resources[i]["core"],
                   functionalExtensions:resources[i]["extensions"],
                   con1:resources[i]["core"].relations,
                   con2:functionalExtRelations
               };
           } else{
               BubbleBig[i]={
                   id:resources[i]["id"],
                   layers:matchImplementedSpecs(resources[i]["core"].implementedSpecifications,resources[i]["extensions"]),
                   status:resources[i]["state"],
                   stroke:"red",
                   "strokewidth":5,
                   functionalCore:resources[i]["core"],
                   functionalExtensions:resources[i]["extensions"],
                   con1:resources[i]["core"].relations,
                   con2:functionalExtRelations
               };
           }

        }
        //console.log(BubbleBig);  //sorted resources or Context entities
        //sorts by the id
        BubbleBig.sort(sortObj);
        for(i=0;i<coordinates[0].length;i++){
            var addCircle=false;
            var layers=BubbleBig[bubbleIndex].layers;

            if(((i+1)%5==0)  ){//multiples of 5 (big bubble)
                //verifies the level
                if(findnemo(layers,"fr.liglab.adele.icasa.layering.services.global.icasaLayer")){
                    BubbleBig[bubbleIndex].color="rgb(50,50,50)";
                    addCircle=true;
                } else if(findnemo(layers,"fr.liglab.adele.iop.device.api.IOPService")&&findnemo(layers,"fr.liglab.adele.icasa.device.GenericDevice")){//if it's a remote device--PURPLE
                    BubbleBig[bubbleIndex].color="rgb(209,197,235)";

                    addCircle=true;
                }else if(findnemo(layers,"fr.liglab.adele.iop.device.api.IOPService")){//if it's a remote service--TURKEY
                    BubbleBig[bubbleIndex].color="rgb(70,240,210)";
                    addCircle=true;
                }else if(findnemo(layers,"fr.liglab.adele.icasa.device.GenericDevice")){//if it's a device--GREEN
                    BubbleBig[bubbleIndex].color="rgb(190,212,144)";
                    addCircle=true;
                }else if(findnemo(layers,"fr.liglab.adele.icasa.layering.services.api.ServiceLayer")){//services --ORANGE
                    BubbleBig[bubbleIndex].color="rgb(250,231,212)";
                    addCircle=true;
                }else if(findnemo(layers,"fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer")){//applications--BLUE
                    BubbleBig[bubbleIndex].color="rgb(169,208,217)";
                    addCircle=true;
                }else if(findnemo(layers,"fr.liglab.adele.icasa.location.Zone")){//zone--BROWN
                    BubbleBig[bubbleIndex].color="rgb(212,200,144)";
                    addCircle=true;
                }else{
                    BubbleBig[bubbleIndex].color="rgb(81,81,81)";
                    //console.log("no bubble");

                }
                BubbleBig[bubbleIndex].x=coordinates[0][i];
                BubbleBig[bubbleIndex].y=coordinates[1][i];
                BubbleBig[bubbleIndex].radius=coordinates[2][i];
                BubbleBig[bubbleIndex].xTxt=coordinates[0][i]-(bubbleRadius*2);
                BubbleBig[bubbleIndex].yTxt=coordinates[1][i];
                Bubbles[globalIndex]=BubbleBig[bubbleIndex];
                //console.log(Bubbles)
                bubbleIndex+=1;


            }else if((((i+1)%5)-1)==0 ){//first small bubble
                bubbleSmall[smallBubbleIndex]={
                    x:coordinates[0][i],
                    y:coordinates[1][i],
                    color:coordinates[3][i],
                    radius:coordinates[2][i],
                    functionalCore:BubbleBig[bubbleIndex].functionalCore};
                Bubbles[globalIndex]=bubbleSmall[smallBubbleIndex];
                smallBubbleIndex+=1;
            }else if((((i+1)%5)-2)==0  ){ //&& addCircle
                bubbleSmall[smallBubbleIndex]={
                    x:coordinates[0][i],
                    y:coordinates[1][i],
                    color:coordinates[3][i],
                    radius:coordinates[2][i]};
                if (BubbleBig[bubbleIndex].functionalExtensions.length==0){
                    bubbleSmall[smallBubbleIndex].color="rgb(81,81,81)";
                }else{
                    bubbleSmall[smallBubbleIndex].functionalExtensions=BubbleBig[bubbleIndex].functionalExtensions
                }

                Bubbles[i]=bubbleSmall[smallBubbleIndex];
                smallBubbleIndex+=1;
            }else if((((i+1)%5)-3)==0){
                bubbleSmall[smallBubbleIndex]={
                    x:coordinates[0][i],
                    y:coordinates[1][i],
                    color:coordinates[3][i],
                    radius:coordinates[2][i]
                };

                Bubbles[globalIndex]=bubbleSmall[smallBubbleIndex];
                smallBubbleIndex+=1;
            }
            else if((((i+1)%5)-4)==0){
                bubbleSmall[smallBubbleIndex]={
                    x:coordinates[0][i],
                    y:coordinates[1][i],
                    color:coordinates[3][i],
                    radius:coordinates[2][i]
                };

                Bubbles[globalIndex]=bubbleSmall[smallBubbleIndex];
                smallBubbleIndex+=1;
            }
            globalIndex=globalIndex+1;
        }

        for(var i=0;i<BubbleBig.length;i++){
            bubbleInfo[i]=[BubbleBig[i].id, BubbleBig[i].x, BubbleBig[i].y];
        }
       // console.log(bubbleInfo);
        function findcoordinates(name){
            for(var a=0;a<bubbleInfo.length;a++){
               //console.log("is "+name+" equal to "+bubbleInfo[a][0] +" ?");
              // console.log(a+" out of "+bubbleInfo.length);
                if(bubbleInfo[a][0]==name){
                    return [bubbleInfo[a][1],bubbleInfo[a][2]];
                }

            }
        }
//console.log(BubbleBig);
        for(var i=0;i<BubbleBig.length;i++){
            if(BubbleBig[i].con1.length>0){//if an element has functional core

                for(var p=0;p<BubbleBig[i].con1.length;p++){//loop though them
                    var top=BubbleBig[i].con1[p].sourcesId.length;
                    for(var e=0;e<top;e++){
                        lines[lineindex]=[{x1:BubbleBig[i].x,y1:BubbleBig[i].y,x2:findcoordinates(BubbleBig[i].con1[p].sourcesId[e])[0],y2:findcoordinates(BubbleBig[i].con1[p].sourcesId[e])[1],stroke:"rgb(102,102,102)","strokewidth":6}];
                        lineindex+=1;
                    }


                }
            }
        }
        for(var i=0;i<BubbleBig.length;i++){

            if(BubbleBig[i].con2.length>0){//if an element has functional core
                for(p=0;p<BubbleBig[i].con2.length;p++){//loop though them
                    lines[lineindex]=[{x1:BubbleBig[i].x,y1:BubbleBig[i].y,x2:findcoordinates(BubbleBig[i].con2[p])[0],y2:findcoordinates(BubbleBig[i].con2[p])[1],stroke:"rgb(254,166,33)","strokewidth":1}];
                    lineindex+=1;
                }
            }
        }

		svg1
		.attr("class","contextMap")
		.style('position','absolute');
		var svg = svg1.append("svg");
        d3.select(".d3-tip").remove();
        tip = d3.tip().attr('class', 'd3-tip').html(function(d) {
            //console.log(d);
            if (d.color=="rgb(190,137,74)"){//brown
                var mystring = d.functionalCore.relations.toString();
                var spaced=mystring.replace(/,/g , "<br>");
                return "Functional Core<br>"+"name: <span style='color:maroon'>" + d.functionalCore.implementation+"</span>"+
                    "<br>services: <span style='color:maroon'>"+spaced+"</span>";
            }
            if(d.color=="rgb(255,128,0)"){//orange
                return "Functional Extensions<br>"+"id: <span style='color:maroon'>" + d.functionalExtensions[0].id+"</span>"+
                    "<br>status: <span style='color:maroon'>"+d.status+"</span>";
            }
            if(d.color=="rgb(1,160,198)"){//blue
                return "Use<br>"+"id: <span style='color:maroon'>" + d.id+"</span>"+
                    "<br>status: <span style='color:maroon'>"+d.status+"</span>";
            }
            if(d.color=="rgb(128,196,28)"){//green
                return "Origin<br>"+"id: <span style='color:maroon'>" + d.id+"</span>"+
                    "<br>status: <span style='color:maroon'>"+d.status+"</span>";
            }
            if(d.color=="rgb(255,255,255)"){//white
                return "id: <span style='color:maroon'>" + d.id+"</span>"+
                    "<br>status: <span style='color:maroon'>"+d.status+"</span>";
            }//else{functionalExtensions}

        });
        svg.call(tip);
        lines.forEach(drawL);
		drawC(Bubbles);

        function drawL(data) {
            //console.log(data);
            var wid = PageWidth/2;
            var hei = 5*PageHeight/12;
            //console.log(wid+"<wid hei>"+hei);
            var screenElem=svg.selectAll("g")
                .data(data,function(d){/*console.log(d);*/ } );

            var lain = svg.append("line")
                .data(data)
                .attr("x1", function(d){return d.x1+wid;})
                .attr("y1", function(d){return d.y1+(hei*yoffset);})
                .attr("x2", function(d){return d.x2+wid;})
                .attr("y2", function(d){return d.y2+(hei*yoffset);})
                .attr("stroke", function(d){return d.stroke;})
                .attr("stroke-width", function(d){return d.strokewidth;});
        }

		function drawC(data){
			var wid = PageWidth/2;
			var hei = 5*PageHeight/12;
            var screenElem=svg.selectAll("g bubbleText")

                .data(data,function(d){/*console.log(d);*/ } );
            var elemEnter = screenElem.enter()
                .append("g")
                .attr("transform", function(d){return "translate("+(d.x+wid)+","+(d.y+(hei*(yoffset*.99)))+")";});
            var ldata=[
                {x:631,y:535},
                {x:831,y:735}
            ];
            var line = d3.line()
                .x(function (d){return d.x;})
                .y(function (d){return d.y;});

            var lines = elemEnter.append("path")
                .data([ldata])
                .enter()
                .append("path")
                .attr("d",line)
                .attr("fill","none")
                .attr("stroke","#000")
                .attr("stroke-width",5);

            var circle = elemEnter.append("circle")
                .attr("r", function(d){return d.radius;} )
                .attr("stroke",function(d){return d.stroke;})
                .attr("fill", function(d){return d.color;})
                .on('mouseover', tip.show)
                .on('mouseout', tip.hide);
                elemEnter.append("text")
                    .style('fill', 'darkOrange')
                    .text(function(d){return d.id;});
            circle.on("click", function(d){
                console.log(d);
                console.log(d.con1.length+": "+d.con1);
                console.log(d.con2.length+": "+d.con2);

               /* for(var z=0; z=d.functionalCore.states.length; z++){

                }*/

                var infoText;
                //var servicesTxt = d.functionalCore.services.toString();
                var servicesTxt = d.functionalCore.implementedSpecifications.toString();
                var statesTxt = d.functionalCore.contextStates.toString();
                var conn1="[fnc_core]";
                for (p=0;p<d.con1.length;p++){
                    console.log(d.con1[p].sourcesId.toString())
                    conn1=conn1+d.con1[p].sourcesId.toString();
                }
                var connectionsTxt = conn1+"[fnc_ext]"+d.con2.toString();



                var serviceHtml=servicesTxt.replace(/,/g , '<br>');
                var statesHtml=statesTxt.replace(/,/g , '<br>');
                var connectionsHtml=connectionsTxt.replace(/,/g , '<br>');

                infoText= '<div class="idtitle">id: '+d.id+'</div>';
                infoText=infoText+'<div><b>Status:</b> '+d.status+'</div>';
                infoText=infoText+'<div><hr></hr><b>Functional Core:</b>'
                infoText=infoText+'<br>name (class): <div style=\"color:maroon\"> '+d.functionalCore.implementation+'</br>';
                infoText=infoText+'</div>services (implementations): <div style="color:maroon">'+serviceHtml+'</div></div>';
                infoText=infoText+'</div>connections: <div style="color:maroon">'+connectionsHtml+'</div></div>';
                infoText=infoText+'</div>states:<div style="color:maroon">';
                var statesArray = $.map(d.functionalCore.states, function(value, index) {
                    return [index, value];
                });
                for(var i=0;i<statesArray.length;i=i+2){
                    infoText=infoText+'<br>'+statesArray[i]+':'+statesArray[i+1];

                    console.log(statesArray[i]+":"+statesArray[i+1]);
                }
                infoText=infoText+'</span></div>';
                infoText=infoText+'<div><hr></hr><b>Functional Extensions:</b>';
                if(d.functionalExtensions.length>0){
                    infoText=infoText+'<br>name: <span style=\"color:maroon\"> '+d.functionalExtensions[0].id+'</br></span>';
                }
                infoText=infoText+'<div><b>Status:</b> <span style="color:maroon">'+d.status+'</span></div>';

                var infoBox = document.getElementById('infop');
                console.log(infoBox);
                console.log(infoBox.style);
                console.log(infoBox.style.display);
                console.log("stage 1 reached");
                infoBox.style.display = "block";
                infoBox.innerHTML = infoText;
                d3.select(this)
                    .transition()
                    .duration(500)
                    .style("background","lightblue");
            })

						}
		});
	}

var infoBox = document.getElementById('infop');
var infoBoxClose = document.getElementById('infop');
	
runtime();
