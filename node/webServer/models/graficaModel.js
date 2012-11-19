//-------------------------**MODEL GRAFICA**----------------------------//****
/**
*** Si nos ponemos muy estrictos con el MVC en teoria este no es un modelo ya que no trabaja con DBs, pero 
*** pretende tener la función de un modelo sólo que con datos alimentados en tiempo de ejecución.
**/
var graf_arr = [];
//Un poco de administracion de nuestro array de graficas.
//añadimos
var addGrafica = function(grafica){
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

module.exports.graf_arr = graf_arr;
module.exports.addGrafica = addGrafica;
module.exports.closeGrafica = closeGrafica;
function unSlash(cadena){
    return cadena.replace("/","");
}
