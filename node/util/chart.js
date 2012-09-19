
exports.grafica = function(moneda, periodo){
    var fs = require('fs');
    var databaseUrl = 'history';
    var collection = [moneda];
    var db = require('../../../../lib/node_modules/mongojs').connect(databaseUrl, collection);
    
    db.operaciones.find({Status:0}, function(err,data){
        if(err || !data)
            console.log('Da Fuck with the chart!');
        else
            data.forEach(function (historico){
                client.emit('orders',historico);
            });
    });
}