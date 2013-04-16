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

  //var socket = io.connect('document.location.host');
  var socket = io.connect('192.168.2.114:1300');
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
  
  socket.on('grafica-ini', function(init){
    var temp="";
    console.log(init);
    ids.push(init.ID);
    //pedimos el esta de la grafica hasta este momento.     
    socket.emit('grafica-state',{
        id: init.ID
    }); 
    
    graficas.push(new Grafica(init));
    
    $.each(init, function(key, val){
      if(key != 'ID' && key != 'symbol'){
        if(key== 'TP' || key == 'SL')
            val = redondear(val);
        temp += key + ": " + val + " </br><hr>";
      }
    });
    //Añadimos controlador de popover.
    $(function(){
      //Para que la primer .rowfluid tenga el menu de los inputs diferente a todos los demas
      $("#"+init.ID+" #inputs").popover({html: true,content:temp,trigger:"hover", placement:"bottom"});
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
  socket.on('grafica-candle', function(candle){
    getGrafica(candle.id).onCandle(candle.variables);
    //Actualizamos los íconos
    updateIcons(candle.variables);
  });
  //Datos del tick.
  socket.on('onTick', function(tick){
    var date = new Date();
    var symbol = tick.symbol;
    var selector;                
    //
    var hora = date.getHours()+9>=24 ? (date.getHours()+AJUSTE)-24:date.getHours()+AJUSTE;
    //este desmadre es para que no imprima las valores si el valor es < 10.
    var min = date.getMinutes()<10 ? '0'+date.getMinutes():date.getMinutes();
    var segs = date.getSeconds()<10 ? '0'+date.getSeconds():date.getSeconds();;
    $('#market-hora').empty().append('  '+ hora + ':' + min + ':'+ segs);
    if(tick.entry == "ask"){
        $('#'+ symbol + " .ask").empty().append(tick.precio);
    }else if(tick.entry == "bid"){
      for(i=0; i<last_prices_arr.length;i++){

            if(last_prices_arr[i][0] == symbol){
               /**
              /*si el precio entrante es mayor al anterior entonces pintamos los precios 
              /*de azul, si no los pintamos de rojo.
              **/
              if(tick.precio>last_prices_arr[i][1]){
                selector = 'blue';
                last_prices_arr[i][1] = tick.precio;
              }else{ 
                if(tick.precio < last_prices_arr[i][1]){
                  selector = 'red';
                  last_prices_arr[i][1] = tick.precio;
                }
              }
            }
          }    
          $('#'+ symbol + " .bid").empty().append(tick.precio);
          $('#'+ symbol + " .bid").css('color',selector);
          $('#'+ symbol + " .ask").css('color',selector);
    }
    for(var i in graficas){
      if(graficas[i].symbol == tick.symbol){
          //primero borramos lo que este y después ponemos el precio.
        if(tick.entry == "ask"){
            graficas[i].onTick("ask", parseFloat(tick.precio));

        }else if(tick.entry == "bid"){

          graficas[i].onTick("bid", parseFloat(tick.precio));
          
        }
      }
    }
  });
  //Estado actual de la grafica/expert
  socket.on('expert-state', function(state){
    console.log(state);
    
    var temp = getGrafica(state.id);
    temp.initState(state);
    //Actualizamos los iconos en la seccion de vars.
    updateIcons(state);
    
  });
  //cada que hay un precio de apertura de minuto.
  socket.on('grafica-open', function(open){

    function setOpen(grafica,open){
      grafica.onOpen(open);
    }
    for(var i in graficas){
      if(graficas[i].symbol == open.symbol){
        setOpen(graficas[i],open.precio);
      }  
    }
  });
  //Entro una orden.
  socket.on('grafica-order', function(order){
   // playOrder(); 
   console.log(order);
    document.title = '('+ ++contOp +') Operaciones';
    var id =  order.id;
    var ord = order.ordid;

    $("#trade .log").prepend('<tr class="success" id='+ord+'></tr>');
    $.each(order, function(key,val){
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
    $("#" + ord).append('<td><button class="btn" onClick="closeOrder(\''+order.id+'\',\''+ord+'\')">cerrar</button></td>');

    getGrafica(id).onOrderOpen(order);
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

  socket.on('journal-msj',function(journal){ 
    $('#terminal-journal-title').css('color','red');
   	$('#journal .log').prepend('<tr class='+ journal.label +'>'+
    '<td class="time">'+ new Date().toUTCString()+'</td><td>'+ journal.msj 
    +'</td></tr>');
  });
  socket.on('log-msj', function(log){
    $('#terminal-exp-title').css('color','red');
    $('#experts .log').prepend('<tr class='+ log.label +'>'+
    '<td class="time">'+ new Date().toUTCString()+'</td><td>'+ log.msj 
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
  var id=data.id;
  $('#'+id+' .bollx abbr').attr({
      'title': 'Dif: '+ redondear(data.bollDif)
    });
  //Cambiamos los iconos en la parte de vars.
  if(data.limite)
    $('#'+id+' .limite-cruce .var-ico').removeClass().addClass('icon-ok');
  else
    $('#'+id+' .limite-cruce .var-ico').removeClass().addClass('icon-remove');

  if(data.hora)
    $('#'+id+' .hora .var-ico').removeClass().addClass('icon-ok');
  else
    $('#'+id+' .hora .var-ico').removeClass().addClass('icon-remove');

  if(data.bollX)
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
/**
* Objeto grafica.
*/
function Grafica(data){
  
  this.data = data;
  this.id = data.ID;
  this.symbol = data.symbol;
  this.open_min;
  this.bollUp = 0;
  this.bollUpDiff = 0;
  this.bollUpS = 0;
  this.bollUpSDiff=0;
  this.bollDnS = 0;
  this.bollDnSDiff =0;
  this.bollDn = 0; 
  this.bollDnDiff =0;
  this.velasS;
  this.lastBid;
  this.lastAsk;
  this.point = (this.symbol == "USD/JPY") ? 0.001 : 0.0001;
  this.order= false;
  //Este array guarda los valores que crean las graficas cuanda hay o no hay
  //operaciones.
  this.data_master = [['Date','Open','bollUp','bollDn','bollUpS','bollDnS']];
  this.chart;
  this.chart_id;
  this.chart_width;
  this.chart_height;
  
  /*
  * Regresamos el valor de determinada propiedad para esta grafica.
  */
  this.getPropiedad = function(propiedad){
    var temp;
    $.each(data,function(key, val){
      if(key == propiedad){
         temp = val;
      }
    });
    return temp;
  }
  this.onTick = function(tipo, precio){
    if(tipo == "bid"){
      this.lastBid = precio;
    }else if(tipo == "ask"){
      this.lastAsk = precio;
    }
  }
  /*
  * Evento de precio de apertura de minuto la gráfica.
  */
  this.onOpen = function(open){
    this.open_min = redondear(open);
    this.bollUpDiff = redondear(this.bollUp - (this.openMin +this.getPropiedad("bollSpecial")));
    this.bollDnDiff = redondear((this.openMin - this.getPropiedad("bollSpecial")) - this.bollDn);
    this.bollUpSDiff = redondear(this.bollUpS - this.openMin);
    this.bollDnSDiff = redondear(this.openMin - this.bollDnS);
    this.setDataOpen();
  }  
  /*
  * Evento de cambio de vela de la gráfica.
  */
  this.onCandle = function(bolls){
    this.bollUp = redondear(parseFloat(bolls.bollUp));
    this.bollDn = redondear(parseFloat(bolls.bollDn));
    this.bollUpS = redondear(parseFloat(bolls.bollUpS));
    this.bollDnS = redondear(parseFloat(bolls.bollDnS));
    $('#'+this.id+'-prom').attr('data-original-title',"+"+this.bollUp +" -"+this.bollDn); 
    velasS = parseInt(bolls.velas);
  }
  //Ponemos el valor inicial a los bollingers.
  this.initState = function(vars){
    
    this.bollUp = vars.bollUp;
    this.bollDn = vars.bollDn;
    this.bollUpS = vars.bollUpS;
    this.bollDnS = vars.bollDnS;
    $('#'+this.id+'-prom').attr('data-original-title',"+"+this.bollUp +" -"+this.bollDn);
    this.chart_id = document.getElementById(this.data.ID+'-chart');
  }
  /*
  * Evento de apertura de operación.
  */
  this.onOrderOpen = function(orden){
    this.order = orden;
    this.drawChart();
  }
  /*
  *Evento de cierre de operación
  */
  this.onOrderClose = function(){
     this.order = false;
     this.drawChart();
  }
  this.setDataOpen = function(open){
    
    if(this.data_master.length >10){
      //Si tiene mas de 40 datos, quitamos el 1 para que no se acumulen.
      this.data_master.splice(1,1);
      this.data_master[this.data_master.length] = [
        getPeriodDate(this.getPropiedad('Periodo')),this.open_min, this.bollUp, this.bollDn,this.bollUpS,this.bollDnS
      ];
    }else{
      this.data_master[this.data_master.length] = [
        getPeriodDate(this.getPropiedad('Periodo')),this.open_min,this.bollUp, this.bollDn,this.bollUpS,this.bollDnS
      ];
    }
    this.drawChart();
  }
  /*
  *Dibujamos la gráfica en su correspondiente div
  */
  this.drawChart = function() {
    var data;
    //Si no hay operaciones entonces, graficamos las entradas, sino las salidas.
    if(this.order){
      data = google.visualization.arrayToDataTable(this.getOutData());
    }else{
      data = google.visualization.arrayToDataTable(this.getInData());
    }

    var options = {
      title: this.symbol,
      pointSize : 3,
      width : 350,
      height : 200,
      legent : {
        position : "none"
      },
      hAxis:{
        textStyle:{
          color:"white"
        }
      } 
    };
    this.chart = new google.visualization.LineChart(this.chart_id);

    this.chart.draw(data, options);
  } 
  /**
  *Regresamos datos de entra de operaciones que serán graficados.
  */
  this.getInData = function(){
    var temp = [];
    for (var i=0; i< this.data_master.length;i++){
      temp[temp.length] = [
        this.data_master[i][0],
        this.data_master[i][1],
        this.data_master[i][2],
        this.data_master[i][3]
      ];
    }
    return temp;
  }
  /**
  *Regresamos datos de entra de operaciones que serán graficados.
  */
  this.getOutData = function(){
    var temp = [['Date','Open','boll-salida', 'SL','TP']];

    for (var i=1; i< this.data_master.length;i++){
      temp[temp.length] = [
        this.data_master[i][0],
        this.data_master[i][1],
        this.order.tipo == "1"? this.data_master[i][4]:this.data_master[i][5],
        parseFloat(this.order.sl),
        parseFloat(this.order.tp)
      ];
    }
    return temp;
  }
}