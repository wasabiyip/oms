//Manejador de eventos en cliente...
var ids= [];
var contOp =0;
var logs = [];
var id = Math.floor(Math.random()*101);
var graficas=[];
var ordLock = false;
//Eventualmente debemos buscar la forma de que esto se cree dinámicamente.
var last_prices_arr = [["EURUSD",0],["GBPUSD",0],["USDCHF",0],["USDJPY",0]];
var grafica = function(id){  
    this.grafid = id;
};
google.load("visualization", "1", {packages:["corechart"]});

$(document).ready(function(){
  //drawSemaforo();

  var socket = io.connect(document.location.host);
  $('.promedios').tooltip({animation:true,html:true});
  //Todo lo que tenga socket.on quiere decir que es el server nos
  //esta notificando algún evento.

  //Al cargar la página enviamos señal de inicio.
  socket.on('connect', function () {
    socket.emit('handshake', {
       id: 'oms'
    });
    socket.emit('getOrders',{
      //Lo voy a dejar asi por que es muy cagado!
      msj : 'ola k ase!'
    });
  });
  
  socket.on('grafica-ini', function(data){
    var temp="";
    ids.push(data.setts.ID);
    //pedimos el esta de la grafica hasta este momento.     
    socket.emit('grafica-state',{
        id: data.setts.ID
    }); 
    
    graficas.push(new Grafica(data.setts));
    
    $.each(data.setts, function(key, val){
      if(key != 'ID' && key != 'symbol'){
        if(key== 'TP' || key == 'SL')
            val = redondear(val);
        temp += key + ": " + val + " </br><hr>";
      }
    });
    //Añadimos controlador de popover.
    $(function(){
      //Para que la primer .rowfluid tenga el menu de los inputs diferente a todos los demas
      if(graficas.length <= 2 ){
        $("#"+data.setts.ID+" #inputs").popover({html: true,content:temp,trigger:"hover",placement:"bottom"});
      }else
        $("#"+data.setts.ID+" #inputs").popover({html: true,content:temp,trigger:"hover", placement:"left"});
    }); 
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
    //Actualizamos los íconos
    updateIcons(data);
  });
  //Datos del tick.
  socket.on('grafica-tick', function(data){

    var date = new Date();
    var symbol = unSlash(data.values.symbol);
    var selector;                
    //
    var hora = date.getHours()+9>=24 ? (date.getHours()+AJUSTE)-24:date.getHours()+AJUSTE;
    //este desmadre es para que no imprima las valores si el valor es < 10.
    var min = date.getMinutes()<10 ? '0'+date.getMinutes():date.getMinutes();
    var segs = date.getSeconds()<10 ? '0'+date.getSeconds():date.getSeconds();;
    $('#market-hora').empty().append('  '+ hora + ':' + min + ':'+ segs);

    for(var i in graficas){
      if(graficas[i].symbol == data.values.symbol){
          //primero borramos lo que este y después ponemos el precio.
        if(data.values.tipo == "ask"){
            $('#'+ symbol + " .ask").empty().append(data.values.precio);
            graficas[i].onTick("ask", parseFloat(data.values.precio));

        }else if(data.values.tipo == "bid"){

          graficas[i].onTick("bid", parseFloat(data.values.precio));
          for(i=0; i<last_prices_arr.length;i++){

            if(last_prices_arr[i][0] == symbol){
               /**
              /*si el precio entrante es mayor al anterior entonces pintamos los precios 
              /*de azul, si no los pintamos de rojo.
              **/
              if(data.values.precio>last_prices_arr[i][1]){
                selector = 'blue';
                last_prices_arr[i][1] = data.values.precio;
              }else{ 
                if(data.values.precio<last_prices_arr[i][1]){
                  selector = 'red';
                  last_prices_arr[i][1] = data.values.precio;
                }
              }
            }
          }    
          $('#'+ symbol + " .bid").empty().append(data.values.precio);
          $('#'+ symbol + " .bid").css('color',selector);
          $('#'+ symbol + " .ask").css('color',selector);
        }
      }
    }
  });
  //Estado actual de la grafica/expert
  socket.on('expert-state', function(data){

    id= unSlash(data.values.id);
    var temp = getGrafica(id);
    temp.initState(data.values.vars);
    //Actualizamos los iconos en la seccion de vars.
    updateIcons(data);
    
  });
  //cada que hay un precio de apertura de minuto.
  socket.on('grafica-open', function(data){
    
    function setOpen(grafica,open){
      grafica.onOpen(open);
    }
    for(var i in graficas){
      if(graficas[i].symbol == data.values.symbol){
        setOpen(graficas[i],data.values.precio);
      }  
    }
  });
  //Entro una orden.
  socket.on('grafica-order', function(data){
    //playOrder(); 
    document.title = '('+ ++contOp +') Operaciones';
    var id =  data.id;
    var ord = data.ordid;
    console.log(data);
    $("#trade .log").prepend('<tr class="success" id='+ord+'></tr>');
    $.each(data, function(key,val){
      if(key == 'precio'){
        $("#" + ord).append('<td><span id='+key+ '>'+val+'<span id="par"></span></span></td>');
      }else if(key == 'id') {
        //para que no ponga el id de la grafica en la tabla
      }else if(key =='tipo'){
         if(val ==1)
             $("#" + ord).append('<td><span id='+key+ '>Compra</span></td>');
         else
             $("#" + ord).append('<td><span id='+key+ '>Venta</span></td>');
      }else $("#" + ord).append('<td><span id='+key+ '>'+val+'</span></td>');
    });
    $("#" + ord).append('<td><button class="btn" onClick="closeOrder(\''+data.id+'\',\''+ord+'\')">cerrar</button></td>');

    getGrafica(id).onOrderOpen(data);
  });
  //Salio una orden
  socket.on('grafica-orderClose', function(data) {
  	
  	for(var i in graficas){
  		
  		if(graficas[i].order.ordid == data.id){
  			graficas[i].onOrderClose();
  		}
  	}
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

  socket.on('journal-msj',function(data){ 
    $('#terminal-journal-title').css('color','red');
   	$('#journal .log').prepend('<tr class='+ data.label +'>'+
    '<td class="time">'+ new Date().toUTCString()+'</td><td>'+ data.msj 
    +'</td></tr>');
  });
  socket.on('log-msj', function(data){
    $('#terminal-exp-title').css('color','red');
    $('#experts .log').prepend('<tr class='+ data.label +'>'+
    '<td class="time">'+ new Date().toUTCString()+'</td><td>'+ data.msj 
    +'</td></tr>');      
  });	
  
//Cerramos una orden desde el cliente web.
closeOrder= function(grafica,orden){ 
  var ask = confirm("¿Estas seguro que quieres cerrar la orden "+orden+"?");
  var str = {
	  "type":"order-close",
	  "grafica":unSlash(grafica),
	  "id":orden
  };
  if (ask==true){
    console.log('drama!');
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
getGraficasID = function(){
  var temp = [];
  $(function(){
    $('.grafica').each(function(){
      //temp.push($(this).attr('id'));
      temp.push($(this).attr("id"));
    });
  });
  return temp;
}
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
function updateIcons(data){
  var id=data.values.id;
  $('#'+id+' .bollx abbr').attr({
      'title': 'Dif: '+ redondear(data.values.vars.bollDif)
    });
  //Cambiamos los iconos en la parte de vars.
  if(data.values.vars.limite)
    $('#'+id+' .limite-cruce .var-ico').removeClass().addClass('icon-ok');
  else
    $('#'+id+' .limite-cruce .var-ico').removeClass().addClass('icon-remove');

  if(data.values.vars.hora)
    $('#'+id+' .hora .var-ico').removeClass().addClass('icon-ok');
  else
    $('#'+id+' .hora .var-ico').removeClass().addClass('icon-remove');

  if(data.values.vars.bollX)
    $('#'+id+' .bollx .var-ico').removeClass().addClass('icon-ok');
  else
    $('#'+id+' .bollx .var-ico').removeClass().addClass('icon-remove');  
  /**
  **/
  //Añadimos el icono de el pulgar en el menu de opciones si la grafica esta activa o no.
  /*if(!data.values.vars.Active)
      $('#'+id+' .content-graf #active').removeClass().addClass('icon-thumbs-down icon-white');
    else 
      $('#'+id+' .content-graf #active').removeClass().addClass('icon-thumbs-up icon-white');*/
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
function getInputs(){
  var temp;
  for(var grafica in graficas){
    console.log(grafica);
  }
}
