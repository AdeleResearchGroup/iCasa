function applications(){
    $.ajax({url: "http://localhost:9000/icasa/layerapps/layerapps", type: "GET"}).done(function(resources){

        numberOfApps=resources.length;
        resources.forEach(addRow);

        function showname(item){
            console.log(item.id+" : "+item.name);
        }


        function addTab(){
            addRow("filteredLayAppsTable");
        }

        function addRow(appinfo){
            //todo appinfo.version is actually appinfo.instances but was left like that because of coffeescript
            var tbody=document.getElementById("filteredLayAppsTable");
            var rowCount = tbody.rows.length;
            var row = tbody.insertRow(rowCount);

            var col1=row.insertCell(0);
            var checkbx = document.createElement("input");
            checkbx.type = "checkbox";
            if(appinfo.state=="Started"){
                checkbx.checked=true;
            }
            checkbx.onchange=function(){toggleState(checkbx, appinfo.name,appinfo.version,row)};
            col1.appendChild(checkbx);

            var col2=row.insertCell(1);
            col2.innerHTML=appinfo.name;

            var col3=row.insertCell(2);
            col3.innerHTML=appinfo.version;

            var col4=row.insertCell(3);
            col4.innerHTML=appinfo.state;
        }

    });
}
function toggleState(chkbox, appName,instances,row){
    if(chkbox.checked){
        if(instances=="[]"){
            alert("Impossible to start, resolve Provider requirements first")
            chkbox.checked=false;
        }else{
            startApp(appName);
            row.cells[3].innerHTML="Started";
        }
    }else{
        stopApp(appName);
        row.cells[3].innerHTML="Stopped";
    }

}

function startApp(implementation){
    console.log("starting "+implementation)
    $.ajax({url: "http://localhost:9000/icasa/layerapps/layerapps/enable/"+implementation, type: "GET"}).done(function(resources){
        //console.log(resources);
    });
}

function stopApp(implementation){
    console.log("stopping "+implementation)
    $.ajax({url: "http://localhost:9000/icasa/layerapps/layerapps/disable/"+implementation, type: "GET"}).done(function(resources){
        //console.log(resources);
    });
}
applications();