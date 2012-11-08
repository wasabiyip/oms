/**
 * Module dependencies.
 */
var net = require('net');
var express = require('../../../../../lib/node_modules/express')
  , routes = require('./routes')
  ,io = require('../../../../../lib/node_modules/socket.io');

var config = require('./config.js');
var app = express.createServer();
var webClients = [];
var handler =  require('../Handler');

//Configuration
var settings = new config(app, express);

var models={};
console.log(settings.app());
//Routes
var routes = require('./routes')(app,models);

app.listen(3000, function(){
  console.log("WebServer listening on port %d in %s mode", app.address().port, app.settings.env);
});

var server = io.listen(app); 

server.sockets.on('connection', function (client){ 
    
    client.on('disconnect', function () {
		
     });

    client.on('log-in', function(msj){
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
    });
    
    client.on('grafica-state', function(msj){
        handler.expertState(msj);
    });
    
    client.on('order-close', function(msj){
        console.log(msj);
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
    console.log('onCandle webserver');
    notify('grafica-candle', data);
}
/*
recibimos un evento onOpen.
*/
exports.onOpen= function(data){  
    console.log(data);
    notify('grafica-open', data);
}

exports.expertState = function(data){

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

/*
al recibir algun evento mandamos llamar a este método
para que emita un mensaje determinado a los clientes conectados.
*/
function notify(mensaje, data){
    for (i=0; i<webClients.length; i++){
        webClients[i].emit(mensaje,data);
    }
}
//Este metodo evita que se acumulen clientes  que ya no existen cuando se refresa la pagina.
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