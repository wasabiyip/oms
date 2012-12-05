/**
* Este es el objecto gracias al cual podemos representar a una grafica
*/
function Grafica(data){
  this.data = data;
  this.id = unSlash(data.ID);
  this.symbol = data.symbol;
  this.openMin;
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
  this.orders = []
  this.data_in_arr = [['Date','bollUp','Open','bollDn']];
  this.data_temp_arr = [['Date','bollUpS','Open','bollDnS']];
  this.chart;
  this.chart_id;
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
    this.openMin = redondear(parseFloat(open));
    this.bollUpDiff = redondear(this.bollUp - (this.openMin +this.getPropiedad("bollSpecial")));
    this.bollDnDiff = redondear((this.openMin - this.getPropiedad("bollSpecial")) - this.bollDn);
    this.bollUpSDiff = redondear(this.bollUpS - this.openMin);
    this.bollDnSDiff = redondear(this.openMin - this.bollDnS);
    this.drawChart();
  }  
  /*
  * Evento de cambio de vela de la gráfica.
  */
  this.onCandle = function(bolls){
    this.bollUp = redondear(parseFloat(bolls.bollUp) + this.getPropiedad("bollSpecial"));
    this.bollDn = redondear(parseFloat(bolls.bollDn) - this.getPropiedad("bollSpecial"));
    this.bollUpS = redondear(parseFloat(bolls.bollUpS));
    this.bollDnS = redondear(parseFloat(bolls.bollDnS));
    
    velasS = parseInt(bolls.velas);
  }
  //Ponemos el valor inicial a los bollingers.
  this.initState = function(vars){
    
    this.bollUp = vars.bollUp;
    this.bollDn = vars.bollDn;
    this.bollUpS = vars.bollUpS;
    this.bollDnS = vars.bollDnS;
    this.chart_id = document.getElementById(this.symbol+'-chart');
  }
  /*
  * Evento de apertura de operación.
  */
  this.onOrderOpen = function(orden){
    this.orders.push(order);
  }
  /*
  *Evento de cierre de operación
  */
  this.onOrderClose = function(){
     
  }
  /*
  *Dibujamos la gráfica en su correspondiente div
  */
  this.drawChart = function() {
    var temp;

    if(this.data_in_arr.length >40){
      this.data_in_arr.splice(1,1);
      this.data_temp_arr.splice(1,1);
      this.data_temp_arr[this.data_temp_arr.length] = [getDate(),this.bollUpS,this.openMin,this.bollDnS];
      this.data_in_arr[this.data_in_arr.length] = [getDate(),this.bollUp,this.openMin,this.bollDn];
    }else{
      this.data_in_arr[this.data_in_arr.length] = [getDate(),this.bollUp,this.openMin,this.bollDn];
      this.data_temp_arr[this.data_temp_arr.length] = [getDate(),this.bollUpS,this.openMin,this.bollDnS];
    }
    var data = google.visualization.arrayToDataTable(this.data_temp_arr);
    var options = {
      title: this.symbol
    };
    this.chart = new google.visualization.LineChart(this.chart_id);
    this.chart.draw(data, options);
  } 
}