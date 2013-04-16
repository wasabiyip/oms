//Dependencias:
var cls = require("./lib/class");

module.exports = Stream = cls.Class.extend({
	init: function(socket){
		console.log('=> Stream Conectado!');
		var self = this;
		this.oms=null;
		this.socketServer;
		this.connection = socket._connection;
		this.socket = socket;
		//Implementacion de evento de mensajes.
		this.socket.onMessage(function(income){
			
			if (income.type === "tick") {
				self.onTick(income);
				
			} else if (income.type === "open") {
				self.onMinuto(income);
			}
		});
	},
	
	onTick: function(tick){
		if (this.oms && !this.oms.connection.destroyed) {
			this.oms.onTick(tick);
		}
		if(this.socketServer){
			this.socketServer.tick(tick);
		}
	},
	
	onMinuto: function(min){
		if (this.oms && !this.oms.connection.destroyed ){
			this.oms.onMinuto(min);
		}
		if(this.socketServer){
			this.socketServer.minuto(min);
		}
	},
	
	addOms: function(oms){
		this.oms = oms;
	},

	removeOms: function(){
		this.oms = null;
	},
	
	addSocketServer:  function(socket){
		console.log('Stream añadiendo S_S');
		this.socketServer = socket;
	},

	removeSocketServer: function(){
		this.socketServer = null;
	}
});