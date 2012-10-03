/**
  * Este es el objecto gracias al cual podemos representar a una grafica
  */
 function Grafica(data){
     this.data = data;
     
     /*
      * Regresamos el valor de determinada propiedad para esta grafica.
      */
     this.getPropiedad = function(propiedad){
         var temp;
         console.log(data);
         $.each(data,function(key, val){
             if(key == propiedad){
                 temp = val;
             }
         });
         return temp;
     }
     /*
      * Evento de precio de apertura de minuto la gráfica.
      */
     this.onOpen = function(){
         
     }
     /*
      * Evento de cambio de vela de la gráfica.
      */
     this.onCandle = function(){
         
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