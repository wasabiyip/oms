

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
     this.data_arr = [['Date','Bid', 'Ask']];
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
        this.drawChart();
     }
     /*
      * Evento de precio de apertura de minuto la gráfica.
      */
     this.onOpen = function(open){
         var openMin = parseFloat(open);
         this.bollUpDiff = redondear(this.bollUp - (openMin +this.getPropiedad("bollSpecial")));
         this.bollDnDiff = redondear((openMin - this.getPropiedad("bollSpecial")) - this.bollDn);
         this.bollUpSDiff = redondear(this.bollUpS - openMin);
         this.bollDnSDiff = redondear(openMin - this.bollDnS);
     }  
     /*
      * Evento de cambio de vela de la gráfica.
      */
     this.onCandle = function(bolls){
         this.bollUp = parseFloat(bolls.bollUp);
         this.bollDn = parseFloat(bolls.bollDn);
         this.bollUpS = parseFloat(bolls.bollUpS);
         this.bollDnS = parseFloat(bolls.bollDnS);
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
          //title: 'Grafica de prueba - ' +this.symbol
        };
        this.chart = new google.visualization.LineChart(this.chart_id);
        this.chart.draw(data, options);
      }     
 }