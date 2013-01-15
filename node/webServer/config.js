//-------------------------**CONFIG**-------------------------------------//****
module.exports = function (app, express){
	
	//Configuracion etandar
	app.configure(function(){
		app.set('views', __dirname + '/views');
		app.set('view engine', 'jade');
		app.set('view options',{
			layout:false, 
			pretty: true});
		app.use(app.router);
		app.use(express.static(__dirname + '/public'));
	});
	//Configuracion del entorno.
	app.configure('development', function(){
	  app.use(express.errorHandler({ dumpExceptions: true, showStack: true }));
	});

	app.configure('production', function(){
	  app.use(express.errorHandler());
	});
	this.app = function(){
		return app;
	}	
}