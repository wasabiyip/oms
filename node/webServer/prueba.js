var express = require('express')
  , routes = require('routes')
  , http = require('http')
  , path = require('path');

var app = express();
var server = http.createServer(app);
var io = require('socket.io').listen(server);
var obj_Config = require('./config.js');
var models = {}
var webClients = [];
//Configuration.
var config = new obj_Config(app, express);
models.graf_modl = require('./models/graficaModel');
models.master_modl = require('./models/masterModel');
//Routes
require('./routes')(app, models);

server.listen(8000);

io.sockets.on('connection', function(client) {

    client.name = client.remotePort;
    //Método que se ejecuta cuando se recibe un precio desde java 
    client.on('data', function(data) {
        //Emitimos el precio.
        //console.log(''+data);
        evaluar(formatStr(data), client);
    });
  
    //Cuando se cierra la conexion con algun cliente.
    client.on('end', function(end){
        var str = '{ "type" : "close"}';
        evaluar(formatStr(str), client);
    });
    //Se pierde conexcion con el cliente.
    client.on('timeout', function(timeout){
        console.log('Tiempo de conexion expirado');
    });

    client.on('error', function(error){
        console.log('Colapso: ', error);
        console.log(handler.getGrafica(client).settings.ID);
        //if (error.search('This socket is closed'));
      
    });
});

server.listen(app.get('port'), function(){
  console.log("Express server listening on port " + app.get('port'));
});

exports.log = function(data){
    notify('log-msj', data);
}
//mensajes que seran enviados a la pestaña de journal.
exports.journal = function(data){
    notify('journal-msj', data)
}

exports.clientsLength = function(){
    return webClients.length;
}

exports.onTick = function(data){
    notify('grafica-tick', data);
}
/*
recibimos un evento onCandle.
*/
exports.onCandle = function(data){
    
    notify('grafica-candle', data);
}
/*
recibimos un evento onOpen.
*/
exports.onOpen= function(data){  
    notify('grafica-open', data);
}

exports.expertState = function(data){
    var contState;
    console.log('State vueta');
    if(waitState !== null){
        waitState.emit('expert-state',data);
        if(contState>=handler.graficasLength){
            waitState = null;
            contState =0;
        }
        contState++;
    }
    else
        notify('expert-state', data);
}
//Estado inicial de las operaciones.
exports.onOrderInit = function(data){
        waitOps.emit('grafica-order', data)
}
//Evento de una orden entrante
exports.onOrder = function(data){
        notify('grafica-order', data)
}
//Evento de una orden saliente.
exports.onOrderClose = function(data){
    notify('grafica-orderClose', data)
}

exports.orderModify = function(data){
    notify('grafica-orderModify', data);    
}
//Al recibir un login de una grafica, almacenamos e
exports.addGrafica = function(settings){
    var str = {
        setts : settings
    }
    models.graf_modl.addGrafica(str);
}
exports.closeGrafica = function(grafica){
    models.graf_modl.closeGrafica(grafica); 
    
}
exports.grafInit = function(data){
    
}
//Estado inicial de app master de graficas.
exports.masterInit = function(data){
    models.master_modl.setProfile(data.profile);
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
//Este metodo evita que se acumulen clientes  que ya no existen cuando se refresca la pagina.
function checkClientsActivos(){
    temp = [];
    for(i=0; i<webClients.length;i++){
        if(!webClients[i].disconnected){
           temp.push(webClients[i]);
        }
    }
    if(temp.length>0){
        webClients = null;
        webClients = temp;
    }
}