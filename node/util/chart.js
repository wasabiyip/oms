
exports.grafica = function(moneda, periodo){
    var fs = require('fs');
    var databaseUrl = 'history';
    var collection = [moneda];
    var db = require('../../../../lib/node_modules/mongojs').connect(databaseUrl, collection);
    
    db.operaciones.find({Status:0}, function(err,operaciones){
        if(err || !operaciones)
            console.log('Da Fuck!');
        else
            operaciones.forEach(function (op){
                client.emit('orders',op);
            });
    });
    
}