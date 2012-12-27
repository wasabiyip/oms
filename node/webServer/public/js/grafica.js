/**
* Este es el objecto gracias al cual podemos representar a una grafica
*/
function Grafica(data){
  this.data = data;
  this.id = unSlash(data.ID);
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

    if(this.data_master.length >40){
      //Si tiene mas de 40 datos, quitamos el 1 para que no se acumulen.
      this.data_master.splice(1,1);
      this.data_master[this.data_master.length] = [
        getDate(),this.open_min, this.bollUp, this.bollDn,this.bollUpS,this.bollDnS
      ];
    }else{
      this.data_master[this.data_master.length] = [
        getDate(),this.open_min,this.bollUp, this.bollDn,this.bollUpS,this.bollDnS
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
      title: this.symbol
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