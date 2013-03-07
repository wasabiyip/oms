//-------------------------**MODEL GRAFICA**----------------------------//****
/**
*** Si nos ponemos muy estrictos con el MVC en teoria este no es un modelo ya que no trabaja con DBs, pero 
*** pretende tener la función de un modelo sólo que con datos alimentados en tiempo de ejecución.
**/
var graf_arr = [];
var monedas_arr = ['EURUSD','GBPUSD','USDCHF','USDJPY', 'EURGBP'];
var sorted_arr = [['EURUSD',[]],['GBPUSD',[]],['USDCHF',[]],['USDJPY',[]], ['EURGBP',[]]];
//Un poco de administracion de nuestro array de graficas.
//añadimos
var addGrafica = function(grafica){
	grafica.setts.ID = unSlash(grafica.setts.ID);
	sortChart(grafica);
};
//Si el socket de la grafica se cerró entonces debemos borrarlo,
//ademas si todas las graficas de una moneda ya fueron cerradas
//Borramos ese elemento de el primer nivel de graf_arr
var resetStuff = function(){
	graf_arr = [];
	sorted_arr = [['EURUSD',[]],['GBPUSD',[]],['USDCHF',[]],['USDJPY',[]], ['EURGBP',[]]];
};

/*
*ordenamos la grafica entrante de acuerdo a su tipo de moneda y período, 
*esto lo hacemos en pares para tener una correcta presentacion en la vista.
*/
var estructurarChart = function(chart){
	
	var hora_actual = new Date().getHours();
	var temp = [];
	var last_chart;
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

//Ordenamos las graficas de acuerdo a su horaInicial (aunque pueden ser otras).
function sortChart(chart){
	
	for(var i=0; i< sorted_arr.length;i++){
		if(sorted_arr[i][0] == chart.setts.symbol){
			sorted_arr[i][1].push(chart);
			//Debes de amar esta funcion ya que ordena los arrays con el elemento 
			//de setts que queramos, en este caso con horaInicial pero puede ser cualquiera.
			sorted_arr[i][1].sort(function(a,b){
				return a.setts.horaInicial - b.setts.horaInicial;
			});
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
//Retornamos las graficas ordenadas y con su respectiva estructura, listas para ser
//procesadas por Jade.
var getCharts = function(){
	graf_arr =[];
	for(var i=0; i< sorted_arr.length;i++){
		for(var j=0; j< sorted_arr[i][1].length;j++){
			//Enviamos cada grafica ordenada para que sea estructurada en binas.
			estructurarChart(sorted_arr[i][1][j]);
		}
	}
	return graf_arr;
}
module.exports.monedas_arr = monedas_arr;
module.exports.getCharts = getCharts;
module.exports.addGrafica = addGrafica;
module.exports.resetStuff = resetStuff;
function unSlash(cadena){
    return cadena.replace("/","");
}
