//Manejador de eventos en cliente...
var ids= [];
var contOp =0;
var logs = [];
var id = Math.floor(Math.random()*101);
$(document).ready(function(){
	
    var socket = io.connect(document.location.href);
    //Al cargar la página enviamos señal de inicio.
    socket.on('connect', function () {
        console.log(id);
        socket.emit('log-in', {
           num: id
        });
    });
    
    socket.on('grafica-ini', function(data){
        //pedimos el esta de la grafica hasta este momento.
        socket.emit('grafica-state',{
            id: data.setts.ID
            });
        buildGrafica(data);
    //$("#estrategias").replaceWith(data.toString());
    });

    //Recibimos un mensaje genérico.
    socket.on('message', function(msg) {
        json = JSON.parse(msg);
        //formateamos el string por que colapsa si usamos /
        select ="ul ."+ unSlash(json.symbol) + " p"; 
        $(select).empty().append(json.precio);
    });
    //Mensaje de que el socket se desconecto.
    socket.on('disconnect', function () {
        $(".icons #estado").empty().append('L');
        $(".icons #estado").css("color","red");
        $(".icons #estado").replaceWith('<span">L</span>');
    });
    //Datos de cambio de vela.
    socket.on('grafica-candle', function(data){
        var id = unSlash(data.values.id);
        $.each(data.values.vars, function(key, val){
            $('#'+id+' .content-graf .promedios ul #' + key + ' span').empty().append(val);
        });
    });
    //Datos del tick.
    socket.on('grafica-tick', function(data){
        
        symbol = unSlash(data.values.symbol);
        for(i=0; i<ids.length;i++){
            if(ids[i].search(symbol)>=0){
                //primero borramos lo que este y después ponemos el precio.
                if(data.values.tipo==="ask")
                    $("#estrategias #"+ids[i]+" .content-graf h2 .ask").empty().append(data.values.precio);
                else
                    $("#estrategias #"+ids[i]+" .content-graf h2 .bid").empty().append(data.values.precio);
            }
        }
    });
    //Estado actual de la grafica/expert
    socket.on('expert-state', function(data){
        id= unSlash(data.values.id);
        $.each(data.values.vars, function(key, val){
            $("#"+id+' .promedios ul').append('<li id='+key + '>' + key +' : <span>'+ val + '</span></li>');
        });
    });
    //cada que hay un precio de apertura de vela.
    socket.on('grafica-open', function(data){
        var id = unSlash(data.values.id);
        $("#"+id+" .content-graf .promedios h3 span").empty().append(data.values.precio);
    });
    //Entro una orden.
    socket.on('grafica-order', function(data){
        
       var graf =  unSlash(data.id);
       var ord = data.ordid;
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
       $("#"+data.ordid).append('<td><span class=\'cerrar\' title="Cerrar operacion" onClick="closeOrder(\''+graf+'\',\''+ord+'\')">x</span></td>');
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
});

estadoClick = function(grafica){
        
}
//Mostramos informacion acerca de esta 
logClick = function(grafica){
    $('.grafica').css("box-shadow", "1px 2px 4px #666");
    $('.grafica').css("-webkit-box-shadow", "1px 2px 4px #666");
    $('.grafica').css("-moz-box-shadow", "1px 2px 4px #666");
    for(i=0; i<logs.length;i++){
        if(logs[i][0]==grafica) {  
            $('#inputs ul').empty();
            console.log(logs[i][1]);
            $.each(logs[i][1], function(key, val){
                $('#inputs ul').append('<li id='+key + '>' + key +' : '+ val + '</li>');
            });
            $('#'+grafica).css("box-shadow", "2px 3px 4px #FFF");
            $('#'+grafica).css("-webkit-box-shadow", "2px 3px 4px #FFF");
            $('#'+grafica).css("-moz-box-shadow", "1px 2px 4px #FFF");
        }
    }
}
//Al recibir graficas-ini construimos la grafica recibida. 
function buildGrafica(data){
    var setts = data.setts;
    temp = [];
    //quitamos diagonal de I
    var id = unSlash(setts.ID);
    temp.push(id);
    temp.push(data.setts);
    logs.push(temp);
    
    //guardamos los id de cada grafica.
    ids.push(id);
    //Creamos html de grafica.
    $("#estrategias").append('<div class=\'grafica\' id=' + id +'></div>');
    $("#"+id).append('<div class=\'content-graf\'></div>');
    $("#"+id).append('<div class=\'menu-graf\'><div class=\'icons\'></div></div>');
    $("#"+id +" .content-graf").append('<h2>'+ setts.symbol +' bid: <span class="bid">--------</span> ask: <span class="ask">-------</span></h2>');
    $("#"+id +" .content-graf").append('<div id="log-data" style="display:none;"></div>');
    //$("#"+id).append('<div class=\'settings\'></<div>');
    //$("#" +id+ " .settings").append('<ul><h3>Datos del expert</h3></ul>');
    $("#"+id +" .content-graf").append('<div class=\'promedios\'></<div>');
    $("#"+id +" .content-graf").append('<div class=\'operaciones\'></<div>');     
    $("#"+id +" .content-graf .promedios").append('<h3>Apertura Minuto <span class=apertura>-------</span></h3>');
    $("#"+id +" .content-graf .promedios").append('<h3>Promedios</h3><ul></ul>');
    $("#"+id +" .content-graf .operaciones").append('<table></table>');
    $("#"+id +" .content-graf .operaciones table").append('<tr><th>Orden</th><th>Tipo</th><th>Lotes</th><th>Símbolo</th><th>Precio</th><th>SL</th><th>TP</th></tr>');
    $("#"+id +" .menu-graf .icons").append('<span id=\'estado\' title="Conectado" onClick=estadoClick(\''+id+'\')>l</span><span id=\'log\' title="Datos Expert" onClick=logClick(\''+ id +'\')>K</span>');
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
