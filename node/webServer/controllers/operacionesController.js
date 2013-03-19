var fs = require('fs');
var operacionesModel = require('../models/operacionesModel.js');

exports.getOperaciones = function(req, res){
	
	operacionesModel.find(function(err, query){
		writeFile(query);
		res.render('OperacionesView',{
			title : 'Operaciones',
			profile : 'operaciones'
		});
	})
}

var writeFile = function(query){
	fs.writeFile('webServer/temp_data/_ops.json', query, function(err){
		if(err){
			console.log(err);
		}else{
			//nada
		}
	});
}