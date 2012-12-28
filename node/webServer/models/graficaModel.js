//-------------------------**MODEL GRAFICA**----------------------------//****
/**
*** Si nos ponemos muy estrictos con el MVC en teoria este no es un modelo ya que no trabaja con DBs, pero 
*** pretende tener la función de un modelo sólo que con datos alimentados en tiempo de ejecución.
**/
var graf_arr = [];
var monedas_arr = ['EURUSD','GBPUSD','USDCHF','USDJPY', 'EURGBP'];
//Un poco de administracion de nuestro array de graficas.
//añadimos
var addGrafica = function(grafica){
	grafica.setts.ID = unSlash(grafica.setts.ID);
	sortChart(grafica);
};
//Borramos
var closeGrafica = function(grafica){
	
	for(var i=0; i<graf_arr.length; i++){
		var temp = graf_arr[i];
		for( var j=1;j<temp.length;j++){
			if(temp[j].setts.ID == grafica){
				graf_arr[i].splice(j,1);
			}
		}
	}
};
/*
*ordenamos la grafica entrante de acuerdo a su tipo de moneda y período.
*/
var sortChart = function(chart){
	
	var temp = [];
	//Si el symbol no existe el index lo añadimos
	if(!exists(chart.setts.symbol)){
		temp.push(chart.setts.symbol);
		temp.push(chart);
		graf_arr.push(temp);
	}else{
		for(var i=0; i<graf_arr.length; i++){
			if(graf_arr[i][0] == chart.setts.symbol){
				graf_arr[i].push(chart);
			}
		}
	}
}
function exists(symbol){
	for (var i=0; i< graf_arr.length;i++){
		if(graf_arr[i][0] == symbol){
			return true
		}
	}
}
var getCharts = function(){
	return graf_arr;
}
module.exports.monedas_arr = monedas_arr;
module.exports.getCharts = getCharts;
module.exports.addGrafica = addGrafica;
module.exports.closeGrafica = closeGrafica;
function unSlash(cadena){
    return cadena.replace("/","");
}
