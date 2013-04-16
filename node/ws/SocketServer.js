var cls = require("../lib/class"),
Server = require("../lib/server"),
Connection = require("../lib/connection"),
WebServer = require("./webServer"),
_ = require('underscore');

var WS = {};

module.exports = WS;

WS.SocketServer= Server.extend({
	init: function(port){
		var self = this;
		this._super(port);
		this._counter=0;
		this.io = {};
		this.waiting = []; //clientes en espera de evento.
		this.webServer = new WebServer(port);
		this.oms = null;
	},

	Connect: function(callback){
		var self = this;
		this.webServer.create(function(io){
			self.io = io;
		});
		
		this.io.sockets.on('connection', function (socket) {
			var c = new WS.Socket(self._createId(), socket, self);

			c.onOrderClose(function(orden){
				order_close_callback(orden);
			});
			c.onGetOrders(function(){

			})
			self.addConnection(c);
		    console.log('A socket connected: '+_.size(self._connection) + ' ' +c.id );
		});
		if(callback){
			callback(self);
		}
	},

	addOms: function(oms){
		this.oms = oms;
	},

	removeOms: function(){
		this.webServer.resetCharts();
	},

	addChart: function(chart){
		this.webServer.addModelChart(chart);
	},

	tick: function(tick){
		this.forEachConnection(function(client){
			//Enviamos a cada cliente el tick;
			if(client.shaked){
				client.send('onTick', tick);
			}
		});
	},

	minuto: function(minuto){
		this.forEachConnection(function(client){
			//Enviamos a cada cliente el minuto.
			if(client.shaked){
				client.send('grafica-open', minuto);
			}
		});
	},
	onOrderClose: function(callback){
		order_close_callback = callback;
	},
   /*
	* Grupo británico de rock, formado en 1980 por los miembros del disuelto 
	* grupo Joy Division.
	*/
	NewOrder: function(order){
		this.forEachConnection(function(client){
			//Enviamos a cada cliente la orden.
			client.send('grafica-order', order);
		});
	},

	orderClose: function(order){
		this.forEachConnection(function(){
			//Enviamos a cada cliente el cierre.
		});
	},

	expertState: function(state){
		this.forEachConnection(function(client){
			//Enviamos a cada cliente la nueva vela.
			console.log(state);
			client.send('expert-state', state);
		});
	},

	Candle: function(candle){
		this.forEachConnection(function(client){
			client.send('grafica-candle', candle);
		});
	},

	Journal: function(journal){
		this.forEachConnection(function(client){
			client.send('journal-msj',journal)
		});
	},

	Log: function(log){
		this.forEachConnection(function(client){
			client.send('log-msj',log)
		});	
	}
});

WS.Socket = Connection.extend({
	init: function(id, conn, server){
		var self = this;
		this.shaked = false;
		this._super(id, conn, server);
		//Eventos del cliente.
		this._connection.on('disconnect', function(){
			console.log('Client desconectado: '+ self.id);
			self._server.removeConnection(self.id);
		});

		this._connection.on('handshake', function(handshake){
			//Esta listo pa' cotorrear.
			self.shaked = true;
			if(self._server.oms){
				self._server.oms.forEachChart(function(chart){
					self.send('grafica-ini', chart.setts);
				});
			}
		})

		this._connection.on('grafica-state', function(state){
		    self._server.oms.forEachChart(function(chart){
		    	self.send('expert-state', chart.state);
		    });
		});

		this._connection.on('getOrders', function(msj){
		    console.log(msj);
		});

		this._connection.on('order-close', function(msj){
		    on_order_close_callback(msj);
		});
	},
	//Mensajes del cliente
	onHandshake: function(callback){
		on_handshake_callback = callback;
	},

	onGraficaState: function(callback){
		on_grafica_state_callback = callback;
	},

	onGetOrders: function(callback){
		if(this._server.oms)
			this._server.oms.getOrders();
	},

	onOrderClose: function(callback){
		on_order_close_callback = callback;
	},
	
	ExpertState: function(){

	},

	Order: function(){

	},
	
	OrderClose: function(){

	},

	OrderModify: function(){

	},

	Journal: function(){

	},

	Log: function(){

	},
	//Envio de mensajes a este cliente.
	send: function(tipo, message){
		//Convertimos mensaje a String.
		var msj = {
		        "msj" : message
		    };
		//nos aseguramos que la conexión este activa antes de escribirle.
		//console.log(msj);
		if (!this._connection.destroyed) {
			this._connection.emit(tipo, message);
		}
	}

});