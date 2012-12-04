/*var data_arr = [['Date','Bid']];
var charts = [];
function drawChart(bid) {
  handleData(bid);
  var data = google.visualization.arrayToDataTable(data_arr);
  var options = {
    title: 'Grafica de prueba - EURUSD'
  };
  
  var chart = new google.visualization.LineChart(document.getElementById('EUR/USD-chart'));
  chart.draw(data, options);
}

function handleData(bid){
  var temp;
  if(data_arr.length >40){
    data_arr.splice(1,1);
    data_arr[data_arr.length] = [getDate(),bid];
  }else{
    data_arr[data_arr.length] = [getDate(),bid];
  }
}*/