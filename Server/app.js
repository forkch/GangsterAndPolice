/*jslint node: true */
var express = require('express'), 
	routes = require('./routes'),
	messageDispatchers = require("./app/ws-dispatcher.js").dispatchers;


var app = module.exports = express.createServer();
var io = require('socket.io').listen(app);
var connectedSockets = [];

// Configuration

app.configure(function(){
  app.set('views', __dirname + '/views');
  app.set('view engine', 'jade');
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(express.compiler({ src: __dirname + '/public', enable: ['less'] }));
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
});

app.configure('development', function(){
  app.use(express.errorHandler({ dumpExceptions: true, showStack: true })); 
});

app.configure('production', function(){
  app.use(express.errorHandler()); 
});

// Routes
app.get('/', routes.index);
app.get('/admin.html', routes.admin);

// websocket connection
io.sockets.on('connection', function (socket) {
	console.log("client connected " , socket.id);
	Object.getOwnPropertyNames(messageDispatchers).forEach(function(messageName) {
		if (messageName !== "disconnect") {
  			socket.on(messageName, function (data) {
    			messageDispatchers[messageName](data, socket);
  			});
		}
  	});
	
	socket.on('disconnect', function () {
		console.log("client disconnected " , socket.id);
	    messageDispatchers.disconnect(socket);
	});
});

app.listen(3000);
console.log("Express server listening on port %d in %s mode", app.address().port, app.settings.env);
