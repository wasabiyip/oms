/**
  * Este es el objecto gracias al cual podemos representar a una grafica
  */
 function Grafica(data){
     this.data = data;
     this.id = unSlash(data.ID);
     this.symbol = data.symbol;
     this.openMin;
     this.bollUp;
     this.bollUpS;
     this.bollDnS;
     this.bollDn;
     this.velasS;
     this.lastBid;
     this.lastAsk;
     this.point = (this.symbol == "USD/JPY") ? 0.001 : 0.0001;
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
         var openMin = parseFloat(open);
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
     /*
      * Evento de apertura de operación.
      */
     this.onOrderOpen = function(){
         
     }
     /*
      *Evento de cierre de operación
      */
     this.onOrderClose = function(){
         
     }
     
 }