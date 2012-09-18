//Este archivo es usado para obtener la relacion de operaciones.
var operaciones = [];
$(document).ready(function(){
    
   var socket = io.connect('192.168.2.114:3000');
   socket.on('connect', function () {
        socket.emit('ops-history');
   });
   socket.on('orders', function(order){
       operaciones.push(order);
       addOperacion(order);
   });
});

addOperacion = function(order){
   var profit;
   $('#trade table').append('<tr id=\''+order.ExecID+'\'></tr>');
   $('#trade table #'+order.ExecID).append('<td>'+order.ExecID+'</td>');
   $('#trade table #'+order.ExecID).append('<td>'+order.horaOpen+'</td>');
   if(order.Type == '1')
      $('#trade table #'+order.ExecID).append('<td>Compra</td>');
   else
      $('#trade table #'+order.ExecID).append('<td>Venta</td>');
   
   $('#trade table #'+order.ExecID).append('<td>'+order.Size/100000+'</td>');
   $('#trade table #'+order.ExecID).append('<td>'+order.Symbol+'</td>');
   $('#trade table #'+order.ExecID).append('<td>'+order.Price+'</td>');
   $('#trade table #'+order.ExecID).append('<td>'+order.StopL+'</td>');
   $('#trade table #'+order.ExecID).append('<td>'+order.TakeP+'</td>');
   $('#trade table #'+order.ExecID).append('<td>'+order.horaClose+'</td>');
   $('#trade table #'+order.ExecID).append('<td>'+order.Close+'</td>');
   $('#trade table #'+order.ExecID).append('<td>'+ 0.00+'</td>');
   
   if(order.Symbol == 'EUR/USD' || order.Symbol == 'GBP/USD'){
       profit = (order.Price - order.Close) * order.Size * 0.0001;
   }
   
   $('#trade table #'+order.ExecID).append('<td>'+ redondear(profit)+'</td>');
   
    
}           


function redondear(precio){
        
    return Math.round(precio*100000)/100000;
}