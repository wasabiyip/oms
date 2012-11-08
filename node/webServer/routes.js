module.exports = function(app,models){
	//Index
	app.get('/', function(req, res){
		res.render('index', { title: 'Trade' })
	});
}