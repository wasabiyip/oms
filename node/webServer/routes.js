module.exports = function(app, models){
	//Index
	app.get('/', function(req, res){
		console.log(models.graf_modl.graf_arr);

		res.render('trade', 
			{ 
				title: 'Trade', 
				graficas: models.graf_modl.graf_arr,
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