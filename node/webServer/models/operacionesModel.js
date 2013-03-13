
var mongo = require('mongojs');
var db = mongo('history')
var colls = db.collection('operaciones');

var getOperaciones = function(){
	var ops;
	colls.find(function(err,docs){
		ops= docs;
	});
	console.log(ops);
	return ops;
}

module.exports.getOperaciones = getOperaciones;