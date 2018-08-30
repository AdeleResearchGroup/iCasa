function runtime2(){
    document.getElementById("rntime").innerHTML = "";

    var width = PageWidth;
    var height = PageHeight;
    var numberOfbubbles=0;
    var showtooltip=false;

    var svg = d3.select(".svg4")
        .attr("width", width)
        .attr("height", height);

    var simulation = d3.forceSimulation()
        .force("charge", d3.forceManyBody().strength(-(width/10)*7).distanceMin(width/10).distanceMax(width))
        .force("link", d3.forceLink().id(function(d) { return d.id }))
        .force("center", d3.forceCenter(width / 2, (height*2 / 5)))
        .force("y", d3.forceY(0.001))
        .force("x", d3.forceX(0.001));

    function dragstarted(d) {
        if (!d3.event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
    }

    function dragended(d) {
        if (!d3.event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    }   

    function jsonSort(json,key){
        var sorter=[];
        json.
        json.forEach(function (contextEntity) {

            sorter.push(contextEntity.id)
        })

    }

    function compare(a, b) {
        var genreA = a.id.toUpperCase();
        var genreB = b.id.toUpperCase();

        comparison = 0;
        if (genreA > genreB) {
            comparison = 1;
        } else if (genreA < genreB) {
            comparison = -1;
        }
        return comparison;
    }


    d3.json("../../../icasa/layers/context.json", function (error, json) {
        if (error)throw error;
        numberOfbubbles=json.length;
    });
//var radius=12;

    var radius=rSize(RaC(numberOfbubbles).length)/4;






//**********************************************//**********************************************
//**********************************************//**********************************************
    d3.select(".d3-tip").remove();
    tip=d3.tip().attr('class','d3-tip').html(function (d) {
        return d.id;
    });
    svg.call(tip);
//**********************************************//**********************************************
//**********************************************//**********************************************

// .force("collide",d3.forceCollide( radius*3).iterations(16) )


    var state = function(status){
        if (status == "valid" ){
            return "black";
        }else{
            return "red";
        }

    }

    var color = function (layers) {

        if(findnemo(layers,"fr.liglab.adele.icasa.layering.services.api.RootService")){
            return colors['icasa'].code;
        } else if(findnemo(layers,"fr.liglab.adele.iop.device.api.IOPService")&&findnemo(layers,"fr.liglab.adele.icasa.device.GenericDevice")){//if it's a remote device--PURPLE
            return colors['remDevice'].code;
        }else if(findnemo(layers,"fr.liglab.adele.iop.device.api.IOPService")){//if it's a remote service--PURPLE
            return colors['remService'].code;
        }else if(findnemo(layers,"fr.liglab.adele.icasa.device.GenericDevice")){//if it's a device--GREEN
            return colors['device'].code;
        }else if(findnemo(layers,"fr.liglab.adele.icasa.layering.services.api.ServiceLayer")){//services --ORANGE
            return colors['service'].code;
        }else if(findnemo(layers,"fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer")){//applications--BLUE
            return colors['application'].code;
        }else if(findnemo(layers,"fr.liglab.adele.icasa.location.Zone")){//zone--BROWN
            return colors['zone'].code;
        }else if(findnemo(layers,"fr.liglab.adele.icasa.simulator.person.Person")){//person--WHITE
            return colors['person'].code;
        }else{
            return colors['unknown'].code;

        }
    }
    var links =[ ];






    d3.json("../../../icasa/layers/context.json", function (error, json) {
        if (error) throw error;
        console.log(json);
        links=linkss(json);

        simulation
            .nodes(json)
            .force("link").links(links);

        var link = svg.selectAll(".link")
            .data(links)
            .enter()
            .append("line")
            .attr("stroke","rgb(102,102,102)")
            .attr("stroke-width",radius/6)
            .attr("class", "link");

        var node = svg.selectAll(".node")
            .data(json)
            .enter().append("g")
            .attr("class", "node")
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended));

        var circle=node.append('circle')
            .attr("data-target","slide-out")
            .attr("class","sidenav-trigger btn")
            .attr('r', radius)
            .attr('stroke',function(d){
                return state(d.state);
            })
            .attr('fill', function (d) {
                return color(d.core.implementedSpecifications);
            });

        circle.on("click",function (d) {
            console.log(d);
            console.log("width:"+width+", height:"+height);


            /* for(var z=0; z=d.functionalCore.states.length; z++){

             }*/

            var infoHeader;
            var infoImplem;
            var infoContextStates;
            var infoImplemSpecs;
            var infocoreRelations;
            var infoText;

            var infoBoxheader = document.getElementById('liheader');
            var infoBoxImplem = document.getElementById('coreimplem');
            var infoBoxContState = document.getElementById('coreContentState');
            var infoBoxImplemSpecs = document.getElementById('coreImpSpec');
            var infoBoxCoreRels = document.getElementById('coreRelations');

            var infoBoxExtid = document.getElementById('extid');
            var infoBoxselImpl = document.getElementById('extselimpl');
            var infoBoxstate = document.getElementById('extstate');
            var infoBoxAltConfig = document.getElementById('extaltconfig');
            //-----------------------------new **REMOVE THIS COMMENT LINE
            var infoBoxExContState = document.getElementById('extctxtstates');
            var infoBoxExImpSpecs = document.getElementById('extimplspecs');
            var infoBoxExtManSpecs = document.getElementById('extmngSpecs');
            var infoBoxExtRels = document.getElementById('extRelations');
            

           infoHeader='<div class="user-view">' +
               '<div class="background">' +
               '<img src="../../../assets/images/backgrounds/wut.png"></img>' +
               '</div>' +
               '<img class="circle" src="'+DevImg(d.core.implementedSpecifications)+'"></img>' +
               '<span class="white-text name"><b>'+d.id+' ('+d.state+')</b></span>' +
               '<span class="white-text email">'+d.core.implementation+'</span>' +
               '</div>';


            //contextStates
            function DevImg(p){
                if (p.length==1){
                    e=p[0];
                }else{
                    p.forEach(function(elem){
                        if((elem==="fr.liglab.adele.icasa.simulator.device.SimulatedDevice")||(elem==="fr.liglab.adele.icasa.device.GenericDevice")||(elem==="fr.liglab.adele.icasa.device.battery.BatteryObservable")){

                        }else{
                            e=elem;
                        }
                    })
                }
                var imgName="";
                switch(e){
                    case "fr.liglab.adele.icasa.device.temperature.Cooler":
                        imgName = "../../../assets/images/devices/cooler-off";
                    break;
                    case "fr.liglab.adele.icasa.device.sound.AudioSource":
                        imgName = "../../../assets/images/devices/musicPlayer";
                        break;
                    case "fr.liglab.adele.icasa.device.sprinkler.Sprinkler":
                        imgName = "../../../assets/images/devices/sprinkler_off";
                        break;
                    case "fr.liglab.adele.icasa.device.light.BinaryLight":
                        imgName = "../../../assets/images/devices/binaryLight_off";
                        break;
                    case "fr.liglab.adele.icasa.device.doorWindow.WindowShutter":
                        imgName = "../../../assets/images/devices/ShutterClose";break;
                    case "fr.liglab.adele.icasa.device.light.DimmerLight":
                        imgName = "../../../assets/images/devices/dimmerLight_off";break;
                    case "fr.liglab.adele.icasa.device.temperature.Thermometer":
                        imgName = "../../../assets/images/devices/thermometer";break;
                    case "fr.liglab.adele.icasa.device.bathroomscale.MedicalThermometer":
                        imgName = "../../../assets/images/devices/medicalThermometer";break;
                    case "fr.liglab.adele.icasa.device.temperature.Heater":
                        imgName = "../../../assets/images/devices/heater";break;
                    case "fr.liglab.adele.icasa.device.light.Photometer":
                        imgName = "../../../assets/images/devices/photometer";break;
                    case "fr.liglab.adele.icasa.device.gazSensor.CarbonMonoxydeSensor":
                        imgName = "../../../assets/images/devices/COGazSensor";break;
                    case "fr.liglab.adele.icasa.device.gazSensor.CarbonDioxydeSensor":
                        imgName = "../../../assets/images/devices/CO2GazSensor";break;
                    case "fr.liglab.adele.icasa.device.motion.MotionSensor":
                        imgName = "../../../assets/images/devices/movementDetector";break;
                    case "fr.liglab.adele.icasa.device.sound.Speaker":
                        imgName = "../../../assets/images/devices/speaker";break;
                    case "fr.liglab.adele.icasa.device.power.PowerSwitch":
                        imgName = "../../../assets/images/devices/power";break;
                    case "fr.liglab.adele.icasa.device.bathroomscale.BathroomScale":
                        imgName = "../../../assets/images/devices/bathroomScale";break;
                    case "fr.liglab.adele.icasa.device.settopbox.SetTopBox":
                        imgName = "../../../assets/images/devices/liveBox";break;
                    case "fr.liglab.adele.icasa.device.box.Box":
                        imgName = "../../../assets/images/devices/liveBox";break;
                    case "fr.liglab.adele.icasa.device.bathroomscale.Sphygmometer":
                        imgName = "../../../assets/images/devices/sphygmometer";break;
                    case "fr.liglab.adele.icasa.device.button.PushButton":
                        imgName = "../../../assets/images/devices/pushButton";break;
                    case "fr.liglab.adele.icasa.device.presence.PresenceSensor":
                        imgName = "../../../assets/images/devices/movementDetector";break;

                    case "fr.liglab.adele.icasa.location.Zone":
                        imgName = "../../../assets/images/maps/apto";break;
                    case "fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer":
                        imgName = "../../../assets/images/devices/app";break;
                    case "fr.liglab.adele.icasa.layering.services.api.ServiceLayer"://service
                        imgName = "../../../assets/images/devices/service";break;
                    case "fr.liglab.adele.iop.device.api.IOPController"://IOP controller
                        imgName = "../../../assets/images/devices/internetBox";break;
                    case "fr.liglab.adele.icasa.layering.services.api.RootService"://root Icasa
                        imgName = "../../../assets/images/rectLogos/logoICasa96x96";break;

                    default:
                        imgName = "../../../assets/images/devices/genericDevice";
                }
                return imgName+".png";
            }
            function getCntxtSt(d){
                var text="";
                d.core.contextStates.forEach(function (states) {
                    var syncper;
                    if(states.synchroPeriod!=null){
                        syncper=states.synchroPeriod.period+', '+states.synchroPeriod.unit;
                    }else{
                        syncper="null"
                    }
                    text=text+'<li><a>id:'+states.id+', value:'+states.value+', syncP:('+syncper+')</a></li>';
                })
                return text;
            }
            function getImpSpec(d){
                var text="";
                d.core.implementedSpecifications.forEach(function (implSpecs) {
                    text=text+'<li><a>'+implSpecs+'</a></li>';
                })
                return text;
            }
            function getCoreRels(d){
                var text="";
                d.core.relations.forEach(function(relations){
                    text=text+'<li><a>state: ('+relations.state+')</a></li>';
                    relations.sourcesId.forEach(function(relation){
                        text=text+'<li><a>'+relation+'</a></li>';
                    })
                })
                return text;
            }


            function getAltConf(d){
                var text="";
                d.extensions.forEach(function(ext){
                    ext.alternativeConfigurations.forEach(function(altcon){
                        text=text+'<li><a>'+altcon+'</a></li>';
                    })
                })
                return text;
            }
            function getImplSpecs(d){
                var text="";
                d.extensions.forEach(function(ext){
                    ext.implementedSpecifications.forEach(function(imSp){
                        text=text+'<li><a>'+imSp+'</a></li>';
                    })
                })
                return text;
            }
            function getManSpecs(d){
                var text="";
                d.extensions.forEach(function(ext){
                    ext.managedSpecifications.forEach(function(maSp){
                        text=text+'<li><a>'+maSp+'</a></li>';
                    })
                })
                return text;
            }

            function getExtCntxtSt(d){
                var text="";
                d.extensions.forEach(function (states) {
                    console.log(states.contextStates);
                    console.log(states.contextStates.length);
                    states.contextStates.forEach(function (states2) {
                        var syncper;
                        if(states2.synchroPeriod!=null){
                            syncper=states2.synchroPeriod.period+', '+states2.synchroPeriod.unit;
                        }else{
                            syncper="null"
                        }
                        text=text+'<li><a>id:'+states2.id+', value:'+states2.value+', syncP:('+syncper+')</a></li>';
                    })
            })
                return text;
            }

            function getExtRels(d){
                var text="";
                d.extensions.forEach(function (coreRels) {
                    coreRels.relations.forEach(function(relations){
                        text=text+'<li><a>state: ('+relations.state+')</a></li>';
                        relations.sourcesId.forEach(function(relation){
                            text=text+'<li><a>'+relation+'</a></li>';
                        })
                    })

                })



               /* d.core.relations.forEach(function(relations){
                    text=text+'<li><a>state: ('+relations.state+')</a></li>';
                    relations.sourcesId.forEach(function(relation){
                        text=text+'<li><a>'+relation+'</a></li>';
                    })
                })*/
                return text;
            }

            //var infoBox = document.getElementById('slide-out');




            infoBoxheader.innerHTML=infoHeader;
            infoBoxImplem.innerHTML=d.core.implementation;
            infoBoxContState.innerHTML=getCntxtSt(d);
            infoBoxImplemSpecs.innerHTML=getImpSpec(d);
            infoBoxCoreRels.innerHTML=getCoreRels(d);



            if(d.extensions.length>0){
                infoBoxExtid.innerHTML=d.extensions[0].id;
                infoBoxselImpl.innerHTML=d.extensions[0].selectedImplementation;
                infoBoxstate.innerHTML=d.extensions[0].state;
                infoBoxAltConfig.innerHTML=getAltConf(d);
                infoBoxExContState.innerHTML=getExtCntxtSt(d);
                infoBoxExImpSpecs.innerHTML=getImplSpecs(d);
                infoBoxExtManSpecs.innerHTML=getManSpecs(d);
                infoBoxExtRels.innerHTML=getExtRels(d);
            }else{
                infoBoxExtid.innerHTML="[no Ext]";
                infoBoxselImpl.innerHTML="[no Ext]";
                infoBoxstate.innerHTML="[no Ext]";
                infoBoxAltConfig.innerHTML="[no Ext]";
                infoBoxExContState.innerHTML="[no Ext]";
                infoBoxExImpSpecs.innerHTML="[no Ext]";
                infoBoxExtManSpecs.innerHTML="[no Ext]";
                infoBoxExtRels.innerHTML="[no Ext]";
            }



        })

        node.append("text")
            .attr("dx", -radius)
            .attr("dy", radius)
            .style('fill', 'darkOrange')
            .style("font-family", "overwatch")
            .style("font-size", "90%")

            .text(function (d) {
                return d.id
            });

        simulation.on("tick", function () {
            link.attr("x1", function (d) {
                return Math.abs(d.source.x);
            })
                .attr("y1", function (d) {
                    return Math.abs(d.source.y);
                })
                .attr("x2", function (d) {
                    return Math.abs(d.target.x);
                })
                .attr("y2", function (d) {
                    return Math.abs(d.target.y);
                });
            node.attr("transform", function (d) {

                var rx;
                var ry;
                if (d.x>width){
                    rx=width-radius;
                }else if (d.x<0){
                    rx=Math.abs(d.x);
                }else{
                    rx=d.x;
                }
                if (d.y>height){
                    ry=height-radius;
                }else if (d.y<0){
                    ry=Math.abs(d.y);
                }else{
                    ry=d.y;
                }
                return "translate(" + rx + "," + ry + ")";
            });
        });



        function ticked() {
            link
                .attr("x1", function(d) { return d.source.x; })
                .attr("y1", function(d) { return d.source.y; })
                .attr("x2", function(d) { return d.target.x; })
                .attr("y2", function(d) { return d.target.y; });

            node
                .attr("cx", function(d) { return d.x; })
                .attr("cy", function(d) { return d.y; });
        }
        function linkss(d){
            var answer=[];
            d.forEach(function(item){
                var src="";
                var trg="";
                item.extensions.forEach(function(ext){
                    ext.relations.forEach(function (relation) {
                        relation.sourcesId.forEach(function (connection) {
                            src=item.id.toString()+"";
                            trg=connection.toString()+"";
                            //console.log("E_src:"+src+", trg:"+trg);
                            answer.push({"source":src,"target":trg,"stroke":"rgb(254,166,33)"});//"strokewidth":1
                        });
                    });

                });
                item.core.relations.forEach(function (relation){
                    relation.sourcesId.forEach(function (connection) {
                        src=item.id.toString();
                        trg=connection.toString();
                       // console.log("C_src:"+src+", trg:"+trg);
                        answer.push({"source":src,"target":trg,"stroke":"rgb(102,102,102)"});//"strokewidth":6
                    });
                });

            });
            return answer;
        }

    });

	}

	function update(){
    d3.json("../../../icasa/layers/context.json", function(error,json){
        var svg = d3.select(".svg4").transition();
    })
    }

var infoBox = document.getElementById('infop');
var infoBoxClose = document.getElementById('infop');
	
runtime2();
