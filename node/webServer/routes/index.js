
/*
 * GET home page.
 */

exports.index = function(req, res){
	console.log('req');
	console.log(req);
	console.log('res');
	console.log(res);
  res.render('index', { title: 'Trade' })
};