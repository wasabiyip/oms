
var mongoose = require('mongoose');
mongoose.connect('mongodb://localhost/history');

var schema = new mongoose.Schema({});
module.exports = mongoose.model('operaciones',schema);