/** Este es el corazón de la aplicación Node
**/
//Dependencias
var net = require('net');
var webServer = require('./webServer/webServer');
var app = net.createServer();
var handler = require('./Handler'); 
//Iniciamos el servidor TCP.
var Graficas = [];
var server_precios, server_op;

app.on('connection', function(client) {

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
        if (error.search('This socket is closed'));
			
    });
});

function formatStr(string){
	
    var str = string.toString();
    //console.log(str);
    var n = str.search('{');
    str = str.substring(n,str.length);
    return str;
}
app.listen(8080);

//Evaluamos todos los mensajes entrantes (JSON);
function evaluar(msj, socket){
    //convertimos la cadena entrante a JSON
    try{
        var income = JSON.parse(msj);
        
        switch (income.type){
            //Un cliente conectado
            case 'login':
                if(income.name === 'SERVIDOR_PRECIOS'){
                    server_precios = socket;
                    console.log('Servidor de precios conectado: ' + server_precios.name);	
                }else
                if (income.name === 'SERVIDOR_OP'){
                    server_op = socket;
                    console.log('Servidor de Operaciones conectado: ' + server_op.name);
                }else if( income.name === 'CLIENT_TCP'){
					
                    //clients.push([income.symbol,socket,income.settings]);
                    console.log('Cliente tcp conectado ' + income.symbol);

                    //Añadimos una grafica al array de graficas, con el Symbol, un ID de graafica 
                    //el socket desde el cual recibimos conexion y los settings del expert que controla
                    //esa grafica.
                    handler.createGrafica(income.symbol, socket, income.settings);
                    webServer.addGrafica(income.settings);
                    if (!server_precios){
                        console.log('Servidor de precios desconectado');
                    }
                }
                break;

            //un open es un precio de apertura de minuto.
            case 'open':
                handler.notify('open',income.data.Moneda, income.data.Open);
                break;
                
            //Cada que se recibe un precio.	
            case 'tick':
                if(handler.symbolExists(income.symbol)){
                    msj = {
                        "values":{
                            "symbol": income.symbol,
                            "tipo": income.entry, 
                            "precio": income.precio
                            }
                        };
                webServer.onTick(msj);
                handler.notify(income.entry,income.symbol, income.precio)
                
            }
            break;
            //cuando un cliente se desconecta.
            case 'close':

                if(server_precios === socket){

                    serverPrecios = null;
                    console.log('Servidor de precios desconectado.');
                }else{
                    webServer.closeGrafica(handler.getGrafica(socket).settings.ID);
                    handler.closeGrafica(socket);
                }
                break;	
            //al recibir un evento onTick de un cliente conectado.
            case 'onOpen':
                id = handler.getGrafica(socket).settings.ID;
                msj = {
                    "values":{
                        "id":id , 
                        "precio": income.precio
                        }
                    };
            webServer.onOpen(msj);     
            break;
            //al recibir un evento onClandle de un cliente conectado.
            case 'onCandle':
                id = handler.getGrafica(socket).settings.ID;
                msj = {
                    "values":{
                        "id":id , 
                        "vars":income.variables
                        }
                    };
            webServer.onCandle(msj);
            break;
        case 'expert-state':
            //handler.state(income.variables);
            msj = {
                "values":{
                    "id":income.id, 
                    "vars": income.variables
                    }
                };
            console.log('Estate');
            webServer.expertState(msj);
            break;
        case 'onOrderInit':
            webServer.onOrderInit(income.data);
            break;
        case 'onOrder':
            webServer.onOrder(income.data,income.length);
            break;
        case 'onOrderClose':
            webServer.onOrderClose(income.data);
            break;
        case 'orderModify':
            webServer.orderModify(income.data);
    }
    //Cachamos cualquier error y lo imprimimos.
    }catch(error){
        console.log(error + msj);
    }
}
//-------------------------------------------------------------------/
