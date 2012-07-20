
//Este es el servidor web que maneja la comunicación entre la aplicación y el usuario.

//Dependecias->
var sys = require('util'),
express = require('express'),
app         = express.createServer('localhost'),
io          = require('../node_modules/socket.io');
app.use(express.static(__dirname + '/public'));

var webClients = [];
var handler =  require('./Handler');

app.get('/', function (req, res) {
    
    });

app.listen(3000);

var server = io.listen(app); 

server.sockets.on('connection', function (client){ 
    
    // new client is here!
    client.send ( 'Conectado con servidor!' );
    webClients.push(client);

    client.on('message', function () {
        console.log('nuevo mensaje del browser');
    });

    client.on('disconnect', function () {
		
        });

    client.on('ready', function(){
        //client.send(handler.get)
        var setts = handler.getSetts();

        for(i=0;i<setts.length;i++){
            client.emit('grafica-ini',{
                setts : setts[i]
                });
        //client.emit('graficas-ini',{hello :'data'});
        }
    });

    client.on('grafica-state', function(msj){
        console.log(msj);
        handler.expertState(msj);
    });
});

exports.clientsLength = function(){
    return webClients.length;
}

exports.onStream = function(data){

    notify('grafica-stream', data);
}
/*
recibimos un evento onCandle.
*/
exports.onCandle = function(data){
    console.log('onCandle webserver');
    notify('grafica-candle', data);
}
/*
recibimos un evento onTick.
*/
exports.onTick= function(data){
    console.log(''+data);
    notify('grafica-tick', data);
}

exports.expertState = function(data){
    notify('expert-state', data);
}
/*
al recibir algun evento mandamos llamar a este método
para que emita un mensaje determinado a los clientes conectados.
*/
function notify(mensaje, data){

    for (i=0; i<webClients.length; i++){
        webClients[i].emit(mensaje,data);
    }
}

