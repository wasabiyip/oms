//Dependencias:
var cls = require("./lib/class"),
_ = require('underscore'),
Stream = require("./stream"),
Chart = require("./chart"),
OMS = require("./oms"),
//Intancias:
oms = null,
stream = null,
socket_server = null;

module.exports = App = cls.Class.extend({
	init: function(){
		//"Constructor" vacío.
	},

	addClient: function(conn){
		conn.onTcpConnect(function(conn, setts, state){
			var chart = new Chart(conn, setts, state);

			chart.onExpertState(function(state){
				socket_server.expertState(state);
			});

			chart.onCandle(function(candle){
				socket_server.Candle(candle);
			});

			oms.addChart(chart);
			socket_server.addChart(chart);
		});

		conn.onOmsConnect(function(conn){
			
			oms = new OMS(conn);
			//Eventos de OMS.
			oms.onJournal(function(journal){
				console.log('journal');
			});

			oms.onLog( function(log){
				console.log('log');
			});

			oms.onOrder(function(order){
				socket_server.NewOrder(order);
			});

			oms.onOrderClose(function(close){
				socket_server.orderClose(close);
			});

			oms.onOrderModify(function(modify){
				console.log(modify);
			});
			oms.onLog(function(log){
				socket_server.Log(log);
			});

			oms.onJournal(function(msg){
				socket_server.Journal(msg);
			});
			conn.onClose(function(close){
				oms = null;
				if (stream) { //Si hay stream le borramos el OMS.
					stream.removeOms();
				}
				if(socket_server){
					socket_server.removeOms();
				}

				console.log('App => Oms removed.');
			});
			if (stream) { //Si ya Esta el stream le paasamos el OMS.
				stream.addOms(oms);
			}
			if (socket_server){
				oms.addSocketServer(socket_server);
				socket_server.addOms(oms);
				socket_server.onOrderClose(function(order){
					oms.closeOrderFromWeb(order);
				});
			}
		});

		conn.onStreamConnect(function(conn){
			conn.onClose(function(close){
				stream = null;
				console.log('App => Stream close.');
			});
			stream = new Stream(conn);
			if (oms) { //Si ya se conecto el OMS se lo pasamos al stream.
				stream.addOms(oms);	
			}
			if (socket_server){
				console.log('Añadiendo SS a Stream');
				stream.addSocketServer(socket_server);
			}
		}); 
	},

	getOrders: function(){

	},

	onClose: function(callback){
		this.close_callback= callback;
	},

	addSocketServer: function(ss){
		socket_server = ss;
		if (this.stream) {
			this.stream.addSocketServer(this.socket_server);
		}
		if (this.oms) {
			this.oms.addSocketServer(this.socket_server);
		}
	}
});