//Dependencias:
var cls = require('./class'),
Util = require('../util'),
_ = require('underscore');

module.exports = Server = cls.Class.extend({
	init: function(port){
		this.port = port;
		this._connetions = {};
	},

	onConnect: function(callback){
		this.connection_callback = callback;
	},

	onError: function(callback){
		this.error_callback = callback;
	},

	broadcast: function(message){
		throw "No implemetado";
	},

	forEachConnection: function(callback){
		_.each(this._connetions, callback);
	},

	addConnection: function(connection){
		this._connetions[connection.id] = connection;
	},

	removeConnection: function(id){
		delete this._connetions[id];
	},

	getConnection: function(id){
		return this._connetions[id];
	},

	_createId: function(){
		//TODO generar un ID mas perron con A-B en lugar de un 5.
		return '5'+ Util.random(99) + '-' +(this._counter++);
	}
});