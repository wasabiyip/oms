/** Este es el corazón de la aplicación Node
**/
//Dependencias
var net = require('net');
var webServer = require('./webServer/webServer.js');
var app = net.createServer();
var handler = require('./Handler'); 
//Iniciamos el servidor TCP.
var Graficas = [];

var server_precios, server_op; 
//Stream de precios.
/*var dealear = net.connect({port:7000}, function(){
    console.log('conectando');
    delear.write({"type" : "login","name" : "SERVIDOR_PRECIOS" });
});*/

//Mensage de la aplicaciones.
app.on('connection', function(client) {

    client.name = client.remotePort;
    client.setEncoding('utf8');
    //Método que se ejecuta cuando se recibe un precio desde java	
    client.on('data', function(data) {
        //Emitimos el precio.
        
        evaluar(formatStr(data), client);
    });
	
    //Cuando se cierra la conexion con algun cliente.
    client.on('end', function(end){
        console.log(client.name);
        if(server_op == client){
            console.log('app desconectada...');
            app = null;
            handler.resetStuff();
            webServer.resetStuff();
        } 
    });
    //Se pierde conexcion con el cliente.
    client.on('timeout', function(timeout){
        console.log('Tiempo de conexion expirado');
    });

    client.on('error', function(err){
        console.error(err.stack);
    });
});

function formatStr(string){
	
    var str = string.toString();
    //console.log(str);
    var n = str.search('{');
    str = str.substring(n,str.length);
    return str;
}
app.listen(1305);
function unSlash(cadena){
  return cadena.replace("/","");
}
//Evaluamos todos los mensajes entrantes (JSON);
function evaluar(msj, socket){
    //convertimos la cadena entrante a JSON
    try{
        var income = JSON.parse(msj);
        switch (income.type){
            //Un cliente conectado.
            case 'login':
                console.log(socket.name);
                if(income.name === 'app'){
                    console.log('app conectada perfil:');

                    var temp={
                        type: 'journal',
                        label: 'error'                     
                    }
                    server_op = socket;
                    handler.setApp(socket);
                    webServer.masterInit(income);
                }else if(income.name === 'SERVIDOR_PRECIOS'){
                    server_precios = socket;
                    webServer.journal(temp);
                    console.log('Servidor de precios conectado: ' + server_precios.name);	
                }else if( income.name === 'CLIENT_TCP'){
                    //Añadimos una grafica al array de graficas, con el Symbol, un ID de graafica 
                    //el socket desde el cual recibimos conexion y los settings del expert que controla
                    //esa grafica.
                    handler.createGrafica(income.symbol, socket, income.settings);
                    webServer.addGrafica(income.settings);
                    console.log(income.settings.Magicma);
                    msj = JSON.stringify({
                        "msj" :{
                            "type":"logged"
                        }
                    });
                    socket.write(msj + '\n');
                }
                break;

            //un open es un precio de apertura de minuto.
            case 'open':
               
                handler.notify('open',unSlash(income.data.Moneda), income.data.Open);
                msj = {
                    "values":{
                        "symbol":unSlash(income.data.Moneda) , 
                        "precio": income.data.Open
                        }
                    };
                webServer.onOpen(msj);
                break;
            //Cada que se recibe un precio.	
            case 'tick':
                msj = 
                {
                    "values":{
                        "symbol": income.symbol,
                        "tipo": income.entry, 
                        "precio": income.precio
                    }
                };
               
                if(handler.symbolExists(income.symbol)){
                    handler.notify(income.entry,income.symbol, income.precio);
                }
                webServer.onTick(msj);
            break;
            //cuando un cliente se desconecta.
            case 'close':
                var temp={
                        type: 'journal',
                        label: 'error'                     
                    }
                if(server_precios == socket){
                    serverPrecios = null;
                    temp.msj = 'El horror -> ¡El streaming de precios se desconecto!';                    
                    webServer.journal(temp);
                }/*else{
                    temp.msj = 'Grafica ' + handler.getGrafica(socket).settings.ID+' fue desconectada :|';
                    webServer.closeGrafica(handler.getGrafica(socket).settings.ID);
                    handler.closeGrafica(socket);
                    webServer.journal(temp);
                }*/
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
            handler.expertState(income.variables);
            msj = {
                "values":{
                    "id":income.id, 
                    "vars": income.variables
                    }
                };
            webServer.expertState(msj);
            break;
        case 'onOrderInit':
            webServer.onOrderInit(income.data);
            console.log('Order INIT');
            break;
        case 'onOrder':
            webServer.onOrder(income.data,income.length);
            break;
        case 'onOrderClose':
            webServer.onOrderClose(income.data);
            break;
        case 'orderModify':
            webServer.orderModify(income.data);
            break;
        case 'journal':
            webServer.journal(income);
            break;
        case 'log':
            webServer.log(income);
    }
    //Cachamos cualquier error y lo imprimimos.
    }catch(error){
        
        if(error == 'SyntaxError'){
            console.log('error de envio esperado!');
        }
    }
}
//-------------------------------------------------------------------/
