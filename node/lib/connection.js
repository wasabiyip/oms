//Dependencias:
var cls = require('./class');

//Obj-Abstracto de cliente
module.exports = Connection = cls.Class.extend({
	init: function(id, connection, server){
		this._connection = connection;
		this._server = server;
		this.id = id;
		this.loggedIn = false;
	},

	onOmsConnect: function(callback){
		this.oms_connection_callback = callback;
	},

	onTcpConnect: function(callback){
		this.tcp_connection_callback = callback;
	},

	onStreamConnect: function(callback){
		this.stream_connection_callback = callback;
	},

	onClose: function(callback){
		this.close_callback = callback;
	},

	onMessage: function(callback){
		this.message_callback = callback;
	},

	send: function(message){
		throw "No implementado";
	},

	setUTF8: function(data){
		throw "No implementado";
	},

	close: function(logError){
		console.log("Cerrando cliente: " + this._connection.remoteAddress + " Error: "+logError );
		this_connection.close();
	}
});