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
//Si el socket de la grafica se cerró entonces debemos borrarlo,
//ademas si todas las graficas de una moneda ya fueron cerradas
//Borramos ese elemento de el primer nivel de graf_arr
var closeGrafica = function(grafica){
	
	for(var i=0; i<graf_arr.length; i++){
		for( var j=1;j<graf_arr[i].length;j++){
			for(var k=0;k<graf_arr[i][j].length;k++){
				if(graf_arr[i][j][k].setts.ID == grafica){
					graf_arr[i][j].splice(k,1);
				}
			} 
		}
	}
	clearGraficas(grafica);
};
//Revisamos si al borrar las graficas el graf_arr queda completamente vacío
var clearGraficas = function(grafica){
	var empty= true;
	/*for(var i=0; i<graf_arr.length; i++){
		for( var j=1;j<graf_arr[i].length;j++){
			if(graf_arr[i][j].length>0){
				empty = false;
			}
		}
	}
	if(empty){
		//si esta completamente lo redefinimos.
		graf_arr = [];
	}*/
	graf_arr = [];
}
/*
*ordenamos la grafica entrante de acuerdo a su tipo de moneda y período, 
*esto lo hacemos en pares para tener una correcta presentacion en la vista.
*/
var sortChart = function(chart){
	
	var temp = [];
	//Si el symbol no existe, lo añadimos
	if(!exists(chart.setts.symbol)){
		temp.push(chart.setts.symbol);
		temp.push([chart]);
		graf_arr.push(temp);
		
	//Si ya existe, vamos añadiendo arrays que alamacenan gráficas de dos en dos.
	}else{
		for(var i=0; i<graf_arr.length; i++){
			if(graf_arr[i][0] == chart.setts.symbol){
				//Si el último array del array tiene menos de 2 elementos
				//Entonces añadimos la grafica
				
				if(graf_arr[i][(graf_arr[i].length-1)].length<2){
					graf_arr[i][(graf_arr[i].length-1)].push(chart);

				//si tiene mas de 2 elementos entonces, añadimos un nuevo array.
				}else{
					graf_arr[i].push(temp);
					graf_arr[i][(graf_arr[i].length-1)].push(chart);
				}
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
