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
   var pip;
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
   }else if(order.Symbol =='USD/CHF'){
       pip = (order.Size * 0.0001)/order.Price;
       console.log(pip);
    }
    else if(order.Symbol == 'USD/JPY'){
       pip = (order.Size * 0.01)/order.Price;
       profit = (order.Price
       
   }else if(order.Symbol == 'EUR/GBP'){
       
   }
   
   $('#trade table #'+order.ExecID).append('<td>'+ redondear(profit)+'</td>');
   
    
}           


function redondear(precio){
        
    return Math.round(precio*100000)/100000;
}