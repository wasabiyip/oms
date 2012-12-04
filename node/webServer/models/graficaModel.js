//-------------------------**MODEL GRAFICA**----------------------------//****
/**
*** Si nos ponemos muy estrictos con el MVC en teoria este no es un modelo ya que no trabaja con DBs, pero 
*** pretende tener la función de un modelo sólo que con datos alimentados en tiempo de ejecución.
**/
var graf_arr = [];
var monedas_arr = ['EURUSD','GBPUSD','USDCHF','USDJPY', 'EURGBP'];
//Un poco de administracion de nuestro array de graficas.
//añadimos
var addGrafica = function(grafica){if(last_prices_arr[i][0] == symbol){
	             /**
		            /*si el precio entrante es mayor al anterior entonces pintamos los precios 
		            /*de azul, si no los pintamos de rojo.
		            **/
		            if(data.values.precio>last_prices_arr[i][1]){
		                selector = 'blue';
		                last_prices_arr[i][1] = data.values.precio;
		            }else{ 
		                if(data.values.precio<last_prices_arr[i][1]){
		                    selector = 'red';
		                    last_prices_arr[i][1] = data.values.precio;
		                }
		            }
	            }
	grafica.setts.ID = unSlash(grafica.setts.ID);
	graf_arr.push(grafica);
};
//Borramos
var closeGrafica = function(grafica){
	console.log('cerrando grafica ' + grafica);
	for(var i=0; i<graf_arr.length; i++){
		if(graf_arr[i].setts.ID == grafica){
			graf_arr.splice(i,1);
		}
	}
};
module.exports.monedas_arr = monedas_arr;
module.exports.graf_arr = graf_arr;
module.exports.addGrafica = addGrafica;
module.exports.closeGrafica = closeGrafica;
function unSlash(cadena){
    return cadena.replace("/","");
}
