var ops_controller = require('./controllers/operacionesController.js');
module.exports = function(app, models){
	//Index
	app.get('/', function(req, res){
		var grafs = models.graf_modl.getCharts();			
		res.render('TradeView', 
			{ 
				title: 'Trade', 
				profile : models.master_modl.getProfile(),
				graficas: models.graf_modl.getCharts(),
				monedas : models.graf_modl.monedas_arr
			});
	});
	
	/*app.get('/operaciones', function(req, res){
		res.render('OperacionesView',{
			title : 'Operaciones',
			profile : models.master_modl.getProfile(),
			operaciones : models.operaciones_modl.getOperaciones()
		});
	});*/
	
	app.get('/operaciones',ops_controller.getOperaciones);
}