var net = require('net');
var express = require('express')
  , http = require('http')
  , path = require('path');

  var app = express();
var server_web = http.createServer(app);
var io = require('socket.io').listen(server_web);

var server_net = net.createServer( function(socket){
    socket.write('Ola ke ase \r\n');
    socket.pipe(socket);
});
server_net.listen(1300);
server_web.listen(1300);