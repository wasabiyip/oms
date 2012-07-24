//Manejador de eventos en cliente...
var ids= [];
$(document).ready(function(){
	
    var socket  = io.connect(document.location.href),
    text = $('#text');
    
    socket.on('connect', function () {
        
        $(".estado").empty().append('Conectado :}');
        socket.emit('ready', {
            ready:'true'
        });
       
    });

    socket.on('message', function(msg) {
        json = JSON.parse(msg);
        //formateamos el string por que colapsa si usamos /
        select ="ul ."+ unSlash(json.symbol) + " p"; 
        $(select).empty().append(json.precio);
    });

    socket.on('disconnect', function () {
        $(".estado").replaceWith('Desconectado :{');
    });

    socket.on('grafica-candle', function(data){
        var id = unSlash(data.values.id);
        //$("#"+id+' .promedios ul').empty();
        $.each(data.values.vars, function(key, val){
            $('#'+id+' .promedios ul #' + key + ' span').empty().append(val);
        });
    });

    socket.on('grafica-tick', function(data){
        
        symbol = unSlash(data.values.symbol);
        for(i=0; i<ids.length;i++){
            
            if(ids[i].search(symbol)>=0){
                //primero borramos lo que este y después ponemos el precio.
                if(data.values.tipo==="ask")
                    $("#estrategias #"+ids[i]+" h2 .ask").empty().append(data.values.precio);
                else
                    $("#estrategias #"+ids[i]+" h2 .bid").empty().append(data.values.precio);
            }
        }
    })

    socket.on('grafica-ini', function(data){
        //pedimos el esta de la grafica hasta este momento.
        socket.emit('grafica-state',{
            id: data.setts.ID
            });
        buildGrafica(data);
        
        
    //console.log({id: data.setts.ID});
    //$("#estrategias").replaceWith(data.toString());
    });

    socket.on('expert-state', function(data){
        //console.log(data.values.vars);
        id= unSlash(data.values.id);
        $.each(data.values.vars, function(key, val){
            $("#"+id+' .promedios ul').append('<li id='+key + '>' + key +' : <span>'+ val + '</span></li>');
        });
    });
    //cada que hay un precio de apertura de vela.
    socket.on('grafica-open', function(data){
        var id = unSlash(data.values.id);

        $("#"+id+" .promedios h3 span").empty().append(data.values.precio);
        
    });
});
//Al recibir graficas-ini construimos la grafica recibida. 
function buildGrafica(data){
    var setts = data.setts;
    var items = [];
    //quitamos diagonal de I
    var id = unSlash(setts.ID);
    //guardamos los id de cada grafica.
    ids.push(id);
    //Creamos html de grafica.
    $("#estrategias").append('<div class=\'grafica\' id=' + id +'></div>');
    $("#"+id).append('<h2>'+ setts.symbol +' bid: <span class="bid"></span> ask: <span class="ask"></span></h2>')
    $("#"+id).append('<div class=\'settings\'></<div>');
    $("#" +id+ " .settings").append('<ul><h3>Datos del expert</h3></ul>');
    $("#" +id).append('<div class=\'promedios\'></<div>');
    $("#" +id+ " .promedios").append('<h3>Apertura Minuto <span class=apertura></span></h3>');
    $("#" +id+ " .promedios").append('<h3>Promedios</h3><ul></ul>');
    //Borramos estos elementos por que no queremos escribirlos en la pagina
    delete  setts['symbol'];
    delete  setts['ID'];

    $.each(setts, function(key, val){
        $('#'+id +' .settings ul').append('<li id='+key + '>' + key +' : '+ val + '</li>');
    });
}

function unSlash(cadena){
    return cadena.replace("/","");
}