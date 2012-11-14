//-------------------------**MODEL GRAFICA**----------------------------//****
var graf_arr = [];
var addGrafica = function(grafica){
	grafica.setts.ID = unSlash(grafica.setts.ID);
	graf_arr.push(grafica);
};

module.exports.graf_arr = graf_arr;
module.exports.addGrafica = addGrafica;
function unSlash(cadena){
    return cadena.replace("/","");
}
