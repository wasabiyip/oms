//Dependencias:
var cls = require('./lib/class'),
_ = require('underscore'),
net = require('net'),
Util = require("./util"),
Server = require("./lib/server"),
Connection = require("./lib/connection");
var OS = {};
//Accesible desde afuera.
module.exports = OS;

//Implementación del server.
OS.Server= Server.extend({
	
	init: function(port){
		//hacemos this una variable "global" de este "objeto".
		var self = this;
		this._counter=0;
		this._connetions= {};
		this.app = net.createServer();
		this.app.listen((port), function(){
			console.log("OMS - SERVER escuchando en el puerto => "+ port);
		});
		//Cuando algo se conecta al server.
		this.app.on('connection', function(conn){
			//Añadimos una nueva conexion al server.
			var c = new OS.TcpConnection(self._createId(), conn, self);
			if (self.connection_callback) {
				self.connection_callback(c);
			}
			self.addConnection(c);
		});
	},

	broadcast: function(){
		this.forEachClient(function(conn){
			conn.send(message);
		});
	},
});

//Implementacion de connection.
OS.TcpConnection = Connection.extend({
	init: function(id, connection, server){
		var self = this;
		this._super(id, connection, server);
		this._connection.setEncoding('utf8');

		//Cuando se pierde la conexión.
		this._connection.on('close', function(connection){
			if (self.close_callback) {
				self.close_callback(self.id);
			}
			delete self._server.removeConnection(self.id);
			self._server._counter--;
		});
		//Cuando entra algún mensaje.
		this._connection.on('data', function(message){
			this.income;
			try {
				this.income = JSON.parse(message);
			} catch (e) {
				if (e instanceof SyntaxError) {
					console.log("Mensaje recibido no es JSON válido." + message);
				} else {
					throw e;
					return;
				}
			}
			if (!self.loggedIn) {
				//El primer mensaje debe de ser login.
				if (!self.loggedIn && this.income.type == 'login') {
					//Marcamos el objecto como logeado.
					self.loggedIn = true;
					self.type = this.income.name;
					msj =  JSON.stringify({
		                    "msj" :{
		                        "type":"logged"
		                    }
		                });
					if (self.type === 'OMS') {
						//Enviamos handshake.
						self._connection.write(msj + '\n');
						if(self.oms_connection_callback){
							//Emitimos callback si existe.
							self.oms_connection_callback(self);
						}
					} else if (self.type ==  'CLIENT_TCP') {

						self.ID = this.income.settings.ID,
						self._connection.write(msj + '\n');
						if(self.tcp_connection_callback){
							self.tcp_connection_callback(self, this.income.settings, this.income.variables);
						}
					} else if(self.type == 'SERVIDOR_PRECIOS') {

						self._connection.write(msj + '\n');
						if (self.stream_connection_callback) {
							self.stream_connection_callback(self);
						}
					}
					
				}
			} else {
				if (self.message_callback) {
					//Si es de cualquier tipo llamamos este callback si exite.
					self.message_callback(this.income);
				}
			}
		});
		this._connection.on('close', function(){
			if (self.close_callback) {
				self.close_callback(self)
			}
			delete self._server.removeConnection(self.id);
		});
	},
	//Envio de mensajes a este cliente.
	send: function(message){
		//Convertimos mensaje a String.
		var data = JSON.stringify({
		        "msj" : message
		    });
		//nos aseguramos que la conexión este activa antes de escribirle.
		if (!this._connection.destroyed) {
			this._connection.write(data+"\n");
		}
	}
});