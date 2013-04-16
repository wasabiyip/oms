//Dependencias:
var cls = require("./lib/class");

module.exports = Chart = cls.Class.extend({
	init: function(socket, settings, state){
		var self = this;
		this.connection = socket._connection;
		this.socket = socket;
		this.setts = settings;
		this.state = state;
		this.state.id = settings.ID;
		//Implementacion de evento de mensajes.
		this.socket.onMessage(function(income){
			switch (income.type) {
			case 'expert-state':
				expert_state_callback(income);
				break;
			case 'onCandle':
				income.id = self.state.id;
				candle_callback(income);
				break;
			default :
				console.log('Chart => Mensaje no identificado '+ income.type);
			}
		});
	},

	onExpertState: function(callback){
		expert_state_callback = callback;
	},
	
	onCandle: function(callback){
		candle_callback = callback;
	},
	//
	onTick: function(tick){
		this.socket.send(tick);
	},
	
	onMinuto: function(open){
		this.socket.send(open);
	},
	
	closeOrder: function(close){
		console.log('cerrando Orden chart');
		console.log(close);
		this.socket.send(close);
	},

	onOrderClose: function(callback){
		on_order_close_callback = callback;
	},

	getSymbol: function(){
		return this.setts.symbol;
	}
});