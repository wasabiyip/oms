module.exports = function(app, models){
	//Index
	app.get('/', function(req, res){
		console.log(models.graf_modl.graf_arr);

		res.render('trade', { title: 'Trade', graficas: models.graf_modl.graf_arr});
	});
	app.get('/chart', function(req, res){
		res.render('chart', {title:'charts!'});
	});
}