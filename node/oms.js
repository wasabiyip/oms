//Dependencias:
var cls = require("./lib/class"),
Chart = require("./chart"),
_ = require('underscore');

module.exports = OMS = cls.Class.extend({
	init: function(socket){
		var self = this;
		this.connection = socket._connection;
		this.socket = socket;
		console.log('=> OMS Conectado!');
		this.charts = {};
		this.orders = [];
		this.socketServer = null;
		//Implementacion de evento de mensajes.
		this.socket.onMessage(function(income){
			switch (income.type ){
			case 'journal':
				on_journal_callback(income);	
				break;
			case 'log':
				on_log_callback(income);
				break;
			case 'onOrder':
				order_callback(income.data);
				break;
			case 'onOrderClose':
				order_close_callback(income);
				break;
			case 'orderModify':
				self.order_modify_callback(income);
				break;
			default :
				console.log('OMS => Mensaje no identificado');
			}
		});
	},
	onJournal: function(callback){
		on_journal_callback = callback;
	},

	onLog: function(callback){
		on_log_callback = callback;
	},

	onOrder: function(callback){
		order_callback = callback;
	},

	onOrderClose: function(callback){
		order_close_callback = callback;
	},

	onOrderModify: function(callback){
		order_modify_callback = callback;
	},

	addChart: function(chart){
		this.charts[chart.setts.ID] = chart;
	},

	getOrders: function(){
		msj =  JSON.stringify({
            "msj" :{
                "type":"getOrders"
            }
        });
        if (!this.connection.destroyed) 
        	this.connection.write(msj + '\n');
	},
	
	onTick: function(tick){
		this.forEachChart(function(chart){
			if (chart.getSymbol() === tick.symbol) {
				chart.onTick(tick);
			}
		});
	},

	onMinuto: function(min){
		this.forEachChart(function(chart){
			if (chart.getSymbol() === min.symbol) {
				chart.onMinuto(min);
			}
		});
		
	},

	closeOrderFromWeb: function(order){

		this.forEachChart(function(chart){
			console.log(chart.setts.ID);
			if(chart.setts.ID === order.grafica){
				chart.closeOrder(order);
			}
		});
	},

	addSocketServer:  function(socket){
		this.socketServer = socket;
	},

	removeSocketServer: function(){

	},
		
	forEachChart: function(callback){
		if(_.size(this.charts)>0)
			_.each(this.charts, callback);
	}
});