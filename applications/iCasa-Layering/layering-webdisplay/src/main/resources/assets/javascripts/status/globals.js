var svg1 = d3.select("#runtime");
var svg3 = d3.select("#services");
var svg2 = d3.select("#context");
var svg4 = d3.select("#tests");
var PageWidth = window.innerWidth;
var PageHeight = window.innerHeight;
var VisibilityLevel= 0;
var colors={};

colors["icasa"]={rbg:"rgb(50,50,50)",code:"#323232"};
colors["remDevice"]={rbg:"rgb(209,197,235)",code:"#D1C5EB"};
colors["remService"]={rbg:"rgb(70,240,210)",code:"#46F0D2"};
colors["device"]={rbg:"rgb(190,212,144)",code:"#BED490"};
colors["service"]={rbg:"rgb(250,231,212)",code:"#FAE7D4"};
colors["application"]={rbg:"rgb(169,208,217)",code:"#A9D0D9"};
colors["zone"]={rbg:"rgb(212,200,144)",code:"#D4C890"};
colors["person"]={rbg:"rgb(255,255,255)",code:"#FFFFFF"};
colors["unknown"]={rbg:"rgb(81,81,81)",code:"#515151"};

//var myVar = setInterval(reDraw, 3000);

function reDraw(){
	PageWidth = window.innerWidth;
	PageHeight = window.innerHeight;
	servs();
	runtime();
    runtime2();
};

window.onresize = function (event) {
	reDraw();
};

function sortObj(a,b) {
    if (a.id < b.id)
        return -1;
    if (a.id > b.id)
        return 1;
    return 0;
}

function urlResponse(bar){
    //test1=bar;
    console.log(bar);
}

function ajaxGET(url){
    $.ajax({url: url, type: "GET"}).done(function(resource){
        urlResponse(resource);
    });
}

function ajaxGETshow(url){
    $.ajax({url: url, type: "GET"}).done(function(resource){
        var mesOut="";
        for(var t=0;t<resource.length;t++){
           // alert(mesOut);
            mesOut = mesOut + resource[t].app_name +": "+resource[t].registered+"; ";
            //console.log(resource[t].app_name +resource[t].registered);
        }
        if(mesOut.length!=0){
            alert(mesOut);
            urlResponse(resource);
        }else{
            alert("no apps found");
            urlResponse("no apps found");
        }

    });
}



function findnemo(arr,str){
    for(l=0;l<arr.length;l++){
        if(arr[l]==str)return 1;
    }
    return 0;
}

