var express = require('express');
var app = express.createServer()
  , io = require('./node_modules/socket.io').listen(app);

app.listen(3000);
app.use(express.static(__dirname + '/public'));
app.get('/', function (req, res) {
 
});

io.sockets.on('connection', function (socket) {
  console.log('conectado');
});