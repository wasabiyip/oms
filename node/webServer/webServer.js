/**
 * Module dependencies.
 */
 var express = require('express')
  , routes = require('routes')
  , http = require('http')
  , path = require('path');

  var app = express();
var server = http.createServer(app);
var io = require('socket.io').listen(server);

var models = {};
var obj_Config = require('./config.js');
var webClients = [];
var handler =  require('../Handler');

//Configuration.
var config = new obj_Config(app, express);
models.graf_modl = require('./models/graficaModel');
models.master_modl = require('./models/masterModel');
//Routes
require('./routes')(app, models);
//Inicializando
server.listen(8000);

io.sockets.on('connection', function (client){ 
//Todo lo que esta aqui adentro maneja mensajes recibidos desde 
//el navegador conectado.    
    client.on('disconnect', function () {
		
     });

    client.on('handshake', function(msj){
        //client.send(handler.get)
        if(msj.id == 'oms'){
            var setts = handler.getSetts();
            webClients.push(client);
            checkClientsActivos();
            for(i=0;i<setts.length;i++){
                client.emit('grafica-ini',{
                    setts : setts[i]
                    });
                waitState = client;
                waitOps = client;
            }
        }
        if(msj.id == 'data'){
            console.log('cliente data');
            webClients.push(client);
            checkClientsActivos();
        }
    });
    
    client.on('grafica-state', function(msj){
        handler.expertState(msj);
    });
    
    client.on('order-close', function(msj){
        
        handler.closeOrder(msj);
    });
    
    client.on('ops-history', function(){
        db.operaciones.find({Status:0}, function(err,operaciones){
           if(err || !operaciones)
               console.log('Da Fuck!');
           else
               operaciones.forEach(function (op){
                   client.emit('orders',op);
               });
        });
    });
    client.on('ops-grafic', function(){
        
    });
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
    console.log('webServer: añadiendo grafica...');
    models.graf_modl.addGrafica(str);
}

exports.resetStuff = function (){
    models.graf_modl.resetStuff();
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
