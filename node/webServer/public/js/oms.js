//Manejador de eventos en cliente...
var ids= [];
var contOp =0;
var logs = [];
var id = Math.floor(Math.random()*101);
var graficas=[];
var ordLock = false;
var grafica = function(id){  
    this.grafid = id;
};
$(document).ready(function(){
    
    var socket = io.connect(document.location.href);
    //Al cargar la página enviamos señal de inicio.
    socket.on('connect', function () {
        socket.emit('log-in', {
           id: 'oms'
        });
    });
    
    socket.on('grafica-ini', function(data){
        //pedimos el esta de la grafica hasta este momento.
        socket.emit('grafica-state',{
            id: data.setts.ID
        });
        buildGrafica(data);
    });

    //Recibimos un mensaje genérico.
    socket.on('message', function(msg) {
        json = JSON.parse(msg);
        //formateamos el string por que colapsa si usamos /
        select ="ul ."+ unSlash(json.symbol) + " p"; 
        $(select).empty().append(json.precio);
    });
    //Mensaje de que el socket se desconecto.
    socket.on('disconnect', function(){
        $(".icons #estado").empty().append('L');
        $(".icons #estado").css("color","red");
        $(".icons #estado").replaceWith('<span">L</span>');
    });
                                                                  
    //Datos de cambio de vela.
    socket.on('grafica-candle', function(data){
        var id = unSlash(data.values.id);
        getGrafica(id).onCandle(data.values.vars);
        $.each(data.values.vars, function(key, val){
           
            $('#'+id+' .content-graf .promedios ul #' + key + ' #val').empty().append(val);
        });
        if(data.values.vars.Velas>0){
            $('#'+id+' .content-graf .promedios ul #Velas #resta').empty()
                .append(data.values.vars.Velas - parseInt(getGrafProp(id,'Velas Salida')));
        }else if(data.values.vars.Velas==0){
            $('#'+id+' .content-graf .promedios ul #Velas #resta').empty().append('--No Order--');
        }
    });
    //Datos del tick.
    socket.on('grafica-tick', function(data){
        for(var i in graficas){
            if(graficas[i].symbol == data.values.symbol){
                //primero borramos lo que este y después ponemos el precio.
                if(data.values.tipo == "ask"){
                    $("#estrategias #"+ graficas[i].id +" .content-graf .ask").empty().append(data.values.precio);
                    graficas[i].onTick("ask", parseFloat(data.values.precio));
                }else if(data.values.tipo == "bid"){
                    $("#estrategias #"+ graficas[i].id +" .content-graf .bid").empty().append(data.values.precio);
                    graficas[i].onTick("bid", parseFloat(data.values.precio));
                }
            }
        }
    });
    //Estado actual de la grafica/expert
    socket.on('expert-state', function(data){
        id= unSlash(data.values.id);
        getGrafica(id).onCandle(data.values.vars);
        $.each(data.values.vars, function(key, val){
            $("#"+id+' .promedios ul').append('<li id='+key + '>' + key +' : <span class="text-info" id="val"> '+ val + '</span> > <span class="text-error"  id="resta"></span></li>');
        });
    });
    //cada que hay un precio de apertura de minuto.
    socket.on('grafica-open', function(data){
        var id = unSlash(data.values.id);    
        var temp = getGrafica(id);
        $("#"+id+" .content-graf .promedios h5 span").empty().append(redondear(data.values.precio));
        var up = redondear(temp.bollUp - (data.values.precio + temp.getPropiedad("Boll Special")));
        var dn = redondear((data.values.precio - temp.getPropiedad("Boll Special")) - temp.bollDn );
        var upS = redondear(temp.bollUpS - data.values.precio );
        var dnS = redondear(data.values.precio - temp.bollDnS);
        if(up <= 0 && dn <= 0){
            playWarn();
        }
        $("#"+id+" .content-graf .promedios ul #bollUp #resta").empty().append(up);
        $("#"+id+" .content-graf .promedios ul #bollDn #resta").empty().append(dn);
        $("#"+id+" .content-graf .promedios ul #bollUpS #resta").empty().append(redondear(upS));
        $("#"+id+" .content-graf .promedios ul #bollDnS #resta").empty().append(redondear(dnS));
        hardSorting(id, up, dn, upS, dnS);
    });
    //Entro una orden.
    socket.on('grafica-order', function(data){
       playOrder(); 
       var graf =  unSlash(data.id);
       var ord = data.ordid;
       //getGrafica(data).onOrder(data);
       $("#"+ graf + " .operaciones table").append("<tr id="+ data.ordid +"></tr>");
       delete  data['id'];
       $.each(data, function(key,val){
           if(key =='tipo'){
               if(val ==1)
                   $("#"+data.ordid).append('<td><span id='+key+ '>Compra</span></td>');
               else
                   $("#"+data.ordid).append('<td><span id='+key+ '>Venta</span></td>');
               
           }else $("#"+data.ordid).append('<td><span id='+key+ '>'+val+'</span></td>');
       });
       $("#"+data.ordid).append('<td><a class="btn" href="#"> <i class="icon-remove-circle"></i></td>');
       //$("#"+data.ordid).append('<td><span class=\'cerrar\' title="Cerrar operacion" onClick="closeOrder(\''+graf+'\',\''+ord+'\')">x</span></td>');
       //para que el title del navegador se muestre las operaciones que tenemos.
       document.title = 'Operaciones (' + ++contOp +')';
       
    });
    //Salio una orden
    socket.on('grafica-orderClose', function(data) {
        $("#"+data.id).remove();
        
        if (--contOp > 0)
            document.title = 'Operaciones (' + contOp +')';
        else
            document.title = 'Operaciones';
    });
    //Una orden fue modificada.
    socket.on('grafica-orderModify',function(data){
       $("#"+data.id+ " #tp").empty().append(data.nwTp);
    });
    //Cerramos una orden desde el cliente web.
    closeOrder= function(grafica,order){
        
        var ask = confirm("¿Estas seguro que quieres cerrar la orden " + order+"?");
        var str = {
                    "type":"order-close",
                    "grafica":Slash(grafica),
                    "id":order
                };
        if (ask==true){
            socket.emit('order-close',str);
        }else{
          //nada  
        }        
    }
    getOperaciones = function(){
        window.open('historico.html', 'historico de operaciones','');
        return false;
    }
});
//Mostramos informacion acerca de esta 
logClick = function(grafica){
    var temp = getGrafica(grafica);
    $('.grafica').css("box-shadow", "1px 2px 4px #666");
    $('.grafica').css("-webkit-box-shadow", "1px 2px 4px #666");
    $('.grafica').css("-moz-box-shadow", "1px 2px 4px #666");
    $('#lefty-head').empty().append("<h2>Grafica <span></span></h2>");
    $('#lefty-head span').empty().append(temp.symbol);
    $('#inputs ul').empty();
    $.each(temp.data, function(key, val){
        $('#inputs ul').append('<li id='+key + '>' + key +' : '+ val + '</li>');
    });
    $('#'+grafica).css("box-shadow", "2px 3px 4px #298F00");
    $('#'+grafica).css("-webkit-box-shadow", "2px 3px 4px #298F00");
    $('#'+grafica).css("-moz-box-shadow", "1px 2px 4px #298F00");

    
}
//Al recibir graficas-ini construimos la grafica recibida. 
function buildGrafica(data){
    graficas.push(new Grafica(data.setts));
    var setts = data.setts;
    //quitamos diagonal de I
    var id = unSlash(setts.ID);
    //guardamos los id de cada grafica.
    //Creamos html de grafica.

    if(graficas.length >1 && graficas.length % 2 ==1)
       $("#estrategias").append('<div class="row-fluid"></div>');

    $("#estrategias .row-fluid").last().append('<div class="span6" id='+graficas.length+'></div>');
    $("#estrategias .row-fluid #"+graficas.length).append('<div class=\'grafica\' id=' + id + '></div>');
    $("#"+id).append('<div class=\'content-graf\'></div>'); 
    $("#"+id +" .content-graf").append('<h4>'+ setts.symbol +'</h4> <p id="stream">Bid: <span class="bid">--------</span> Ask: <span class="ask">-------</span></p>');
    $("#"+id +" .content-graf").append('<hr/><div id="log-data" style="display:none;"></div>');
    $("#"+id +" .content-graf").append('<div class=\'promedios\'></<div>');
    $("#"+id +" .content-graf").append('<div class=\'operaciones\'></<div>');     
    $("#"+id +" .content-graf .promedios").append('<h5>Apertura Minuto <span class=apertura>-------</span></h5>');
    $("#"+id +" .content-graf .promedios").append('<h5>Promedios</h5><ul id="calculos"class="unstyled"></ul>');
    $("#"+id +" .content-graf .operaciones").append('<table class="table table-condensed"></table>');
    $("#"+id +" .content-graf .operaciones .table").append('<tr><th>Orden</th><th>Tipo</th><th>Lotes</th><th>Símbolo</th><th>Precio</th><th>SL</th><th>TP</th></tr>');
    //Añadir botones de estado de la grafica y demás
    //$("#"+id +" .menu-graf .icons").append('<span id=\'estado\' title="Conectado" onClick=estadoClick(\''+id+'\')>l</span><span id=\'log\' title="Datos Expert" onClick=logClick(\''+ id +'\')>K</span>');
    
    //Borramos estos elementos por que no queremos escribirlos en la pagina
    delete  setts['symbol'];
    delete  setts['ID'];
    $(".icons #estado").css("color","green");
}
//Quitamos un / de el symbolo generalmente USD/JPY es igual a USDJPY
function unSlash(cadena){
    return cadena.replace("/","");
}
//Lo inverso a lo anterior...
function Slash(cadena){
    var text = cadena.split("");
    var res ="";
    for(i=0; i<text.length; i++){
        res +=text[i];
        if(i==2)
            res += '/';
    }
    return res;
}

function unID(cadena){
    var text = cadena.split("");
    var res ="";
    for(i=0; i<text.length; i++){
        res +=text[i];
        if(i==2)
            res += '/';
    }
    
    return res.slice(0, res.lastIndexOf("-"));
}
function getGrafica(graf){
    var temp = null;
    for(var i in graficas){
        if(graficas[i].id == graf){
            temp = graficas[i];
        }
    }
    return temp;
}
//Obetenemos una propiedad determinada de una grafica.
function getGrafProp(graf, prop){
    var temp = getGrafica(graf);      
    return temp.getPropiedad(prop);
}

function redondear( precio){
        
    return Math.round(precio*10000)/10000;
}

function hardSorting(id, up, dn, upS, dnS){
    var ini = parseInt(getGrafProp(id,'Hora Inicial'));
    var fin = parseInt(getGrafProp(id,'Hora Final'));
    var hora = parseInt(new Date().getHours()+5); //5 es dependiendo de la hora del broker.
    
    if(hora >= ini && hora <= fin){//Si nos encontramos en horario de operacion
    
        if(contOp>0){
            //si tenemos operaciones
        }else{
            
        }     
    }
}

function playOrder() {
   $('#sound_order').html(
        "<embed src=sounds/alert.wav hidden=true autostart=true loop=false>");
 }
function playWarn() {
    $('#sound_warn').html(
        "<embed src=sounds/alert2.wav hidden=true autostart=true loop=false>");
 }
 //Regresamos el Bid actual de determinada grafica.
 function getBid(graf){
     return $('#'+ graf +' .content-graf h2 .bid').text();
 }
 //Regresamos el Ask actual de determinada grafica
 function getAsk(graf){
     return $('#'+ graf +' .content-graf h2 .ask').text();
 }
 //Regresamos los pips de deteminada grafica.
 function getPoint(id){
     if(id.search("USDJPY") >= 0){
         return 0.0001;
     }else{
         return 0.00001;
     }         
 }
function getCurrencySymbol(symbol){
    switch (symbol){
        case 'EUR/USD':
            return '€/$';
            break;
        case 'GBP/USD':
            return '£/$';
            break;
        case 'USD/CHF':
            return '$/₣';
            break;
        case 'USD/JPY':
            return '$/¥';
            break;
    }
}