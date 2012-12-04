var last_prices_arr = [["EURUSD",0],["GBPUSD",0],["USDCHF",0],["USDJPY",0],["EURGBP",0]];

google.load("visualization", "1", {packages:["corechart"]});
var monedas_arr = [];
$(document).ready(function(){
  for(var i =0; i< last_prices_arr.length;i++){
    monedas_arr[i]= new Moneda(last_prices_arr[i][0]);
    //console.log(last_prices_arr[i][0]);
    console.log(monedas_arr[i].symbol);
  }
    var socket = io.connect(document.location.host);
    socket.on('connect', function () {
        socket.emit('handshake', {
           id: 'data'
        });
    });
	socket.on('grafica-tick', function(data){
		var date = new Date();
        var symbol = unSlash(data.values.symbol);
        var selector;
        var hora = date.getHours()<10 ? '0'+date.getHours():date.getHours();
        var min = date.getMinutes()<10 ? '0'+date.getMinutes():date.getMinutes();
        var segs = date.getSeconds()<10 ? '0'+date.getSeconds():date.getSeconds();
        //sconsole.log(data.values.precio);
        $('#market-hora').empty().append('  '+ hora + ':' + min + ':'+ segs);

        //primero borramos lo que este y después ponemos el precio.
        if(data.values.tipo == "ask"){
			$('#'+ symbol + " .ask").empty().append(data.values.precio);
			for(i=0; i<monedas_arr.length;i++){
        if(monedas_arr[i].symbol == symbol){
        	monedas_arr[i].onTick('ask',data.values.precio);
        }
      }

	    }else if(data.values.tipo == "bid"){
     	
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
        for(i=0; i<monedas_arr.length;i++){
          if(monedas_arr[i].symbol == symbol){
            monedas_arr[i].onTick('bid',data.values.precio);
          }
        }
		  }    
		
	});    
    
});

//Quitamos un / de el symbolo generalmente USD/JPY es igual a USDJPY
function unSlash(cadena){
    return cadena.replace("/","");
}
function getLastPrices(symbol){
	var temp=[];
	for (var i=0; i<last_prices_arr.length;i++){
		if(last_prices_arr[i][0] == symbol){
			temp.push(last_prices_arr[i][1]);
			temp.push(last_prices_arr[i][2]);
		}
	}
	return temp;
}
function setLastPrice(symbol,tipo,precio){
	
	for (var i=0; i<last_prices_arr.length;i++){
		if(last_prices_arr[i][0] == symbol){
			if(tipo == 'bid'){
				last_prices_arr[i][1] = precio;
			}else if(tipo == 'ask'){
				last_prices_arr[i][2] = precio;
			}
		}
	}
}
function getDate(){
    var date = new Date();
    return date.getHours() + ':'+ date.getMinutes();
}
//Objeto que representa una moneda.
function Moneda(symbol){
  this.lastBid;
  this.lastAsk;
  this.data_arr = [['Date','Bid', 'Ask']];
  this.symbol = symbol;
  this.chart_id = document.getElementById(this.symbol+'-chart');
  
  this.onTick = function(tipo, precio){
    
    if(tipo == "bid"){
      this.lastBid = precio;
    }else if(tipo == "ask"){
      this.lastAsk = precio;
    }
    this.drawChart();
  };
  
  this.drawChart = function() {
    var temp;
    
    if(this.data_arr.length >40){
      this.data_arr.splice(1,1);
      this.data_arr[this.data_arr.length] = [getDate(),this.lastBid,this.lastAsk];
    }else{
      this.data_arr[this.data_arr.length] = [getDate(),this.lastBid,this.lastAsk];
    }
    var data = google.visualization.arrayToDataTable(this.data_arr);
    var options = {
      title: 'Grafica de prueba - ' +this.symbol
    };
    
    this.chart = new google.visualization.LineChart(this.chart_id);
    this.chart.draw(data, options);
  }
}