module.exports = function(app, models){
	//Index
	app.get('/', function(req, res){
		
		res.render('trade', { title: 'Trade', graficas: models.graf_modl.graf_arr });
	});
}