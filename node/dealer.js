var net = require('net');
var junkies = [];
var delear;
var server = net.createServer(function (c){
	console.log('server conectado '+ junkies);
	c.on('end', function(){
		
		for(i=0; i<junkies.length; i++){				
	        if (junkies[i] === c){
	        	console.log('cerrando...');
	            junkies.splice(i,1);
	        }		
	    }
	});
	c.on('data', function(msj){
		messageHandler(msj, c);
	});
});
server.listen(7000,  function(){
	console.log(':)');
});

messageHandler = function(msj,socket){
	try{
		var income = JSON.parse(msj);
        switch (income.type){
        	case 'login':
        		if(income.name === 'SERVIDOR_PRECIOS'){
        			delear = socket;
        		}else if(income.name === 'CLIENT_TCP'){
        			junkies.push(socket);
        		}
        		break;
        	case 'tick':
        		console.log(msj);
        		break;
        	case 'open':
        		console.log(msj);
        		break;

        }
	}catch(error){

	}
}
fordwardMessage = function(msj){
	for(var junkie in junkies){
		junkie.write(msj);
	}
}