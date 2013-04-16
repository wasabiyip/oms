var fs = require('fs');

function main(config){
	//Dependencias:
	//OS == Oms - Server && WS == Web - Server.
	var OS = require("./omsServer"),
	WS = require("./ws/SocketServer"),
	_ = require('underscore'),
	Log = require('log'),
	App = require("./app");
	//Instancias.
	//TODO hacer este correo accesible para todas las clases.
	log = new Log(Log.INFO);

	oms_server = new OS.Server(config.OS);
	app = new  App();
	log.info('Iniciando OMS - Services');
	socket_server = new WS.SocketServer(config.WS);
	socket_server.Connect(function(ss){
		app.addSocketServer(ss);
	});
	
	oms_server.onConnect(function(connection){
		app.addClient(connection);
	});
	app.onClose(function(){
		oms =  null;
	});
}

function getConfigFile(path, callback){
	fs.readFile(path, 'utf8', function(err, json_string){
		if (err) {
			log.error('No se pudo abrir el archivo:', err.path);
			callback(null);
		} else {
			callback(JSON.parse(json_string));
		}
	});
}

var configFile = './config/config.json';
//Todo comienza aquí.
getConfigFile(configFile, function(configFile){
	if (configFile) {//Si existe el archivo de configuración.
		main(configFile);
	} else {
		console.error("El servidor no puede iniciar sin un archivo de configuracion.");
		process.exit(1);
	}
});
