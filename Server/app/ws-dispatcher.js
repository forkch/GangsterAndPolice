/*jslint node: true */

var GameModelFactory = require("./game-model.js"),
	gameModel = GameModelFactory.create(),
	ROBBER_POSITION_BROADCAST_DELAY = 5 * 60 * 1000,
	sendTo = function (name, data, clients) {
		Object.getOwnPropertyNames(clients).forEach(function (clientId) {
			clients[clientId].socket.emit(name, data);
		});
	},
	sendToAdmins = function (name, data) {
		sendTo(name, data, gameModel.admins);
	},
	sendToPolice = function (name, data) {
		sendTo(name, data, gameModel.policemen);
		sendToAdmins(name, data);
	},
	sendToRobbers = function (name, data) {
		sendTo(name, data, gameModel.robbers);
		sendToAdmins(name, data);
	},
	sendToAll = function (name, data) {
		sendTo(name, data, gameModel.policemen);
		sendTo(name, data, gameModel.robbers);
		sendTo(name, data, gameModel.admins);
	},
	releaseRobberPositionLocks = function () {
		console.log("release robber position locks after delay");
		gameModel.sendRobberPositionLocks = {};
		Object.getOwnPropertyNames(gameModel.robbers).forEach(function (clientId) {
			gameModel.sendRobberPositionLocks[clientId] = true;
		});
	},
	allowSendRobberPosition = function (robberClientId) {
		var allow = typeof gameModel.sendRobberPositionLocks[robberClientId] !== "undefined";
		console.log("allow sending position for client ", robberClientId, allow);
		return allow;
	},
	arrestTimeout = function (clientId) {
		return function () {
			console.log("cleared arrestables after 30 second without hit");
			sendToAll("arrest-timeout", {
				type: "arrest-timeout",
				clientId: clientId,
				arrestables: gameModel.chasedArrestables
			});
			gameModel.chasedArrestables = [];
		};
	};

releaseRobberPositionLocks();

/**
 * object with dispatcher functions for supported messages
 */
exports.dispatchers = {
	position: function (data, socketOfOrigin) {
		var clientId = gameModel.idMap[socketOfOrigin.id],
			position = gameModel.updatePosition(socketOfOrigin, data.lat, data.lng);

		console.log("retrieved message 'position' of client", clientId, position.role);

		if (position.role === "police") {
			sendToPolice("position", position);
			sendToAdmins("position", position);
		} else if (position.role === "robber" && allowSendRobberPosition(clientId)) {
			sendToAll("position", position);
			delete gameModel.sendRobberPositionLocks[clientId];
			setTimeout(releaseRobberPositionLocks, ROBBER_POSITION_BROADCAST_DELAY);
		} else {
			sendToAdmins("position", position);
		}
	},
	message: function (data, socketOfOrigin) {
		console.log("message dispatcher", data, socketOfOrigin.id);
		data.clientId = gameModel.idMap[socketOfOrigin.id];
		sendToAll("message", data);
	},
	arrest: function (data, socketOfOrigin) {
		var clientId = gameModel.idMap[socketOfOrigin.id];
		console.log("arrest dispatcher", data, clientId, socketOfOrigin.id);
		data.clientId = clientId;
		console.log("arrest dispatcher clears idmap", gameModel.idMap);
		if (gameModel.policemen[clientId]) {
			data.arrestables = gameModel.getRobbersInRadius(data.lat, data.lng);
			if (gameModel.chasedArrestables.length) {
				// TODO move model logic into GameModel and keep decisions in here
				if (gameModel.chasedArrestables.policeman !== clientId) {
					gameModel.chasedArrestables.forEach(function (chased) {
						data.arrestables.forEach(function (item) {
							if (item.clientId === chased.clientId) {
								sendToAll("hit", item);
								clearTimeout(gameModel.arrestTimeout);
								gameModel.arrestTimeout = undefined;
								gameModel.removeRobber(item.clientId);
								gameModel.chasedArrestables = [];
								delete gameModel.sendRobberPositionLocks[item.clientId];
								if (!gameModel.hasRobbers()) {
									sendToAll("all-robbers-arrested", { msg: "well done, guys!"});
								}
							}
						});
					});
				}
			} else if (data.arrestables.length > 0) {
				sendToAll("arrest", data);
				gameModel.chasedArrestables = data.arrestables;
				gameModel.chasedArrestables.policeman = clientId;
				gameModel.arrestTimeout = setTimeout(arrestTimeout(clientId), 30000);
			} else {
				console.log("arrest failed with no robbers in reachable distance");
			}
		}
	},
	state: function (data, socketOfOrigin) {
		var clientId = gameModel.idMap[socketOfOrigin.id],
			state = gameModel.toJSON(typeof gameModel.robbers[clientId] !== "undefined");

		console.log("state requested by client with id", clientId, "and socket id", socketOfOrigin.id);
		state.clientId = clientId;
		socketOfOrigin.emit("state", state);
	},
	reset: function (data, socketOfOrigin) {
		var id = gameModel.idMap[socketOfOrigin.id];
		if (gameModel.policemen[id]) {
			gameModel = GameModelFactory.create();
			releaseRobberPositionLocks();
		}
	},
	register: function (data, socketOfOrigin) {
		var role = data.role || GameModelFactory.ROLES.robber,
			clientId = gameModel.register(data.name, role, socketOfOrigin),
			response = gameModel.toJSON();

		gameModel.sendRobberPositionLocks[clientId] = true;

		response.role = role;
		response.clientId = clientId;
		response.state = "register";

		socketOfOrigin.emit("register", response);
		sendToAdmins("register", response);
	},
	disconnect: function (socket) {
		var id = gameModel.unregister(socket);
		console.log("client disconnected", id, socket.id);
		sendToAdmins("unregister", {clientId: id });
		sendToAll("off", {
			clientId: id
		});
	}
};