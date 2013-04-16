var cls = require("../lib/class"),
	express = require('express'),
  	routes = require('./routes'),
  	http = require('http'),
  	path = require('path'),
	app = express(),
	config = require('./config.js')(app, express),
	models = {};

//Modelos.
models.graf_modl = require('./models/graficaModel');
models.master_modl = require('./models/masterModel');
models.operaciones_modl = require('./models/operacionesModel');
//Routes.
require('./routes')(app, models);

module.exports = webServer = cls.Class.extend({
	init: function(port){
		this.server = http.createServer(app);
		this.io = require('socket.io').listen(this.server);
		this.server.listen(port, function(){
			console.log('Escuchando socket.io');
		});
	},

	onConnet: function(callback){
		this.on_connect_callback = calback;
	},

	create: function(callback){
		if (callback) {
			callback(this.io);
		}
	},

	addModelChart: function(chart){
		models.graf_modl.addChart(chart);
	},

	resetCharts: function(){
		models.graf_modl.resetCharts();
	}
});