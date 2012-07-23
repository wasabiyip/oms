var fs = require('fs');
var stream = fs.createWriteStream("log.csv");
var ask=0;
var bid=0;

exports.writePrecio = function(tipo, precio){
    if(tipo === 'bid'){
        bid = precio;
    }else ask =precio;
    
    str = bid + ','+ ask+"\n";
    fs.appendFile('log.csv', str, function (err) {
        if (err) throw err;
        console.log(str);
    });    
}
