var handler = require('./handler');
var eurusd = [];
var usdjpy = [];
var gbpusd = [];
var usdchf = [];
var eurgbp = [];
var lock= false;
var clients = [];
var serverPrecios = [];
function broadcast(message, client){
			
	if(message.search('@')>0){
		lock = true;
		serverPrecios = client.name;
		console.log(serverPrecios);
	}
	if (lock && message.charAt(5) === '/'){

		if(message.search('USD/JPY')>0)
			notify('USD/JPY', message);
		else if (message.search('EUR/USD')>0)
				notify('EUR/USD', message);
		else if (message.search('GBP/USD')>0)
				notify('GBP/USD', message);
		else if (message.search('USD/CHF')>0)
				notify('USD/CHF', message);
		else if(message.search('EUR/GBP')>0)
				notify('EUR/GBP', message);
			
	}else if (lock && message.search('$')>0){
		
		if (!lock){
			console.log('No hay conexion con servidor de precios');
		}	
		else{
	 		console.log('Conectado con servidor de precios ');
		}
		if (message.search('USD/JPY')>0)
				clients.push(['USD/JPY', client]);
				
			else if (message.search('EUR/USD')>0){
				console.log('aceptamos eurusd');
				clients.push(['EUR/USD', client]);
			}
			else if (message.search('GBP/USD')>0)
				
				clients.push(['GBP/USD', client]);
			else if ( message.search('USD/CHF')>0)
				
				clients.push(['USD/CHF', client]);
			else if ( message.search('EUR/GBP')>0)
				
				clients.push(['EUR/GBP', client]);
			else null
		}
}

function shutDownSocket(socket){
	
	if(socket.name == serverPrecios){
		lock=false;
		serverPrecios = null;
		console.log('Servidor de precios desconectado');
	}
	for(i=0; i<clients.length; i++){
		
		if (clients[i][1] === socket)
			clients.splice(i,1);
	}
}
function notify(moneda, message){

	for(var i=0; i<clients.length; i++) {
	    
	    if(clients[i][0] === moneda){
			console.log('notificando a' + moneda);   
			//Es muy importante enviar '\n' ya que java espera un fin de 
			//linea. readLine().
			clients[i][1].write(removeSymbol(message) + '\n');
		}
	}
}

function removeSymbol(message){
	var cadena = new String(message);
	return cadena.substr(10);
}
exports.shutDownSocket = shutDownSocket;
exports.broadcast = broadcast;
