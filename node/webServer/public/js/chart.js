//formato de el array['Date','low','open','close','high']
var arr_precios_pool = [['Wen', 1.29766, 1.29798, 1.29775, 1.29800]];
var arr_precios_temp=[];
var chart;
var dobl_open = 0;
var dashboard = new google.visualization.Dashboard(
       document.getElementById('dashboard'));

   var control = new google.visualization.ControlWrapper({
     'controlType': 'ChartRangeFilter',
     'containerId': 'control',
     'options': {
       // Filter by the date axis.
       'filterColumnIndex': 0,
       'ui': {
         'chartType': 'LineChart',
         'chartOptions': {
           'chartArea': {'width': '90%'},
           'hAxis': {'baselineColor': 'none'}
         },
         // Display a single series that shows the closing value of the stock.
         // Thus, this view has two columns: the date (axis) and the stock value (line series).
         'chartView': {
           'columns': [0, 3]
         },
         // 1 day in milliseconds = 24 * 60 * 60 * 1000 = 86,400,000
         'minRangeSize': 86400000
       }
     },
     // Initial range: 2012-02-09 to 2012-03-20.
     'state': {'range': {'start': new Date(2012, 1, 9), 'end': new Date(2012, 2, 20)}}
   });

   var chart = new google.visualization.ChartWrapper({
     'chartType': 'CandlestickChart',
     'containerId': 'chart',
     'options': {
       // Use the same chart area width as the control for axis alignment.
       'chartArea': {'height': '80%', 'width': '90%'},
       'hAxis': {'slantedText': false},
       'vAxis': {'viewWindow': {'min': 0, 'max': 2000}},
       'legend': {'position': 'none'}
     },
     // Convert the first column from 'date' to 'string'.
     'view': {
       'columns': [
         {
           'calc': function(dataTable, rowIndex) {
             return dataTable.getFormattedValue(rowIndex, 0);
           },
           'type': 'string'
         }, 1, 2, 3, 4]
     }
   });
function drawChart(data) {
   // Populate the data table.

    var dataTable = google.visualization.arrayToDataTable(
       data,
    // Treat first row as data as well.
     true);
    var height=
    // Draw the chart.
    chart.draw(dataTable, {legend:'none', width:600, height:400});
}

function openVela(open){
  dobl_open = redondear(open);
  if(arr_precios_temp.length == 0 ){
    arr_precios_temp.push('Tue');  
    arr_precios_temp.push(redondear(open));
    arr_precios_temp.push(redondear(open));
    arr_precios_temp.push(redondear(open));
    arr_precios_temp.push(redondear(open));
    arr_precios_pool.push(arr_precios_temp);
  }
  
  console.log(arr_precios_temp);
}

function tickVela(tick){
  var dobl_low=0;
  var dobl_high=0;
  var dobl_close =0;

  if(arr_precios_temp.length > 1){
    if(tick > arr_precios_temp[5]){
      arr_precios_temp[5] = tick;
    }
    if(tick< arr_precios_temp[1]){
      arr_precios_temp[1] = tick;
    }
    arr_precios_temp[2] = dobl_open;
    arr_precios_temp[3] = tick;
    arr_precios_pool[arr_precios_pool.length-1] = arr_precios_temp;
    console.log(arr_precios_pool);
    drawChart(arr_precios_pool);

  }
}

//este es como un require para la api de google.
google.load('visualization', '1', {packages: ['corechart']});
///este es el equivalente a "$(document).ready" de Jquery para la api de google.
google.setOnLoadCallback(function(){
  //alert(document.location.href);
  //arr_precios_pool.push(arr_precios_temp);
  //drawChart(arr_precios_pool);

  var socket = io.connect(document.location.host);
    //Al cargar la página enviamos señal de inicio.
    socket.on('connect', function () {
        chart = new google.visualization.CandlestickChart(document.getElementById('chart_div'));
        socket.emit('handshake', {
           id: 'data'
        });
        drawChart(arr_precios_pool);
    });  
    socket.on('grafica-tick', function(data){
        if(data.values.symbol== 'EUR/USD' && data.values.tipo=='bid'){
          tickVela(redondear(data.values.precio));
          //console.log(data.values.precio);
        }
    });
    socket.on('grafica-open', function(data){  
      if(data.values.id == 'EURUSD-CUAX'){        
        arr_precios_temp =[];
        console.log(data.values.precio);
        openVela(data.values.precio);
      }
    });

    socket.on('grafica-candle', function(data){
        
    });
});

function redondear( precio){
        
    return Math.round(precio*100000)/100000;
}