module.exports = function(app, models){
	//Index
	app.get('/', function(req, res){
		var grafs = models.graf_modl.getCharts();
		
		res.render('trade', 
			{ 
				title: 'Trade', 
				graficas: models.graf_modl.getCharts(),
				monedas : models.graf_modl.monedas_arr
			});
	});
	
	app.get('/monitor', function(req, res){
		res.render('monitor', {
			title:'Monitoreo',
			monedas : models.graf_modl.monedas_arr
		});
	});
}