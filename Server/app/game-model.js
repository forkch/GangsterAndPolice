/*jslint node: true */
exports.ROLES = {
	police: "police",
	robber: "robber",
	admin: "admin"
};
var GeoUtils = require("./geo-utils.js"),
	prop = function(that, name, value, writable, enumerable, configurable) {
		Object.defineProperty(that, name, {
			value: value,
			writable: writable || true,
			enumerable: enumerable || true,
			configurable: configurable || true
		});
	},
	GameModel = function () {
		prop(this, "idMap", 			{}, false, true, false);
		prop(this, "policemen", 		{}, false, true, false);
		prop(this, "robbers", 			{}, false, true, false);
		prop(this, "admins", 			{}, false, true, false);
		prop(this, "chasedArrestables", [], true, true, false);
		prop(this, "arrestRadius", 		0.1, true, true, false);
		prop(this, "nextUserId", 		1, true, true, false);
		return this;
	};
	
GameModel.prototype.getNextUserId = function (socket) {
	var id = this.nextUserId++;
	this.idMap[socket.id] = id;
	return id;
};
GameModel.prototype.register = function (name, role, socket) {
	var id = this.idMap[socket.id] || this.getNextUserId(socket),
		user = {
			socket: socket,
			name: name,
			role: role
		};

	delete this.robbers[id];
	delete this.policemen[id];
	delete this.admins[id];

	if (role === exports.ROLES.admin) {
		this.admins[id] = user;
	} else if (role === exports.ROLES.robber) {
		this.robbers[id] = user;
	} else {
		// users are policeman by default
		this.policemen[id] = user;
	}
	return id;
};
GameModel.prototype.getRoleOfSocket = function (socket) {
	var id = this.idMap[socket.id],
		role;
	if (this.policemen[id]) {
		role = exports.ROLES.police;
	} else if (this.robbers[id]) {
		role = exports.ROLES.robber;
	} else if (this.admins[id]) {
		role = exports.ROLES.admin;
	} else {
		role = "unregistered";
	}
	return role;
};
GameModel.prototype.unregister = function (socket) {
	var id = this.idMap[socket.id];
	if (id) {
		delete this.idMap[socket.id];
		delete this.policemen[id];
		delete this.robbers[id];
		delete this.admins[id];
	}
	return id;
};
GameModel.prototype.updatePosition = function (socket, lat, lng) {
	var id = this.idMap[socket.id],
		position = {
			clientId: id,
			lat: lat,
			lng: lng,
			role: this.getRoleOfSocket(socket)
		},
		user = this.robbers[id] || this.policemen[id];
	
	if (user) {
		user.lat = lat;
		user.lng = lng;
		position.name = user.name;
	}
	return position;
};
GameModel.prototype.toJSON = function (excludeGeoLocation) {
	excludeGeoLocation = excludeGeoLocation || false;
	return {
		sendRobberPosition: this.sendRobberPosition,
		idMap: this.idMap,
		policemen: this.userListToJSON(this.policemen, excludeGeoLocation),
		robbers: this.userListToJSON(this.robbers, excludeGeoLocation),
		admins: this.userListToJSON(this.admins, true),
		chasedArrestables: this.chasedArrestables
	};
};
GameModel.prototype.userListToJSON = function (userList, excludeGeoLocation) {
	var items = [];
	excludeGeoLocation = excludeGeoLocation || false;
	Object.getOwnPropertyNames(userList).forEach(function (userId) {
		var client = userList[userId],
			item = {
				clientId: userId,
				name: client.name
			};
		if (excludeGeoLocation === false) {
			item.lat = client.lat;
			item.lng = client.lng;
		}
		items.push(item);
	});
	return items;
};
GameModel.prototype.hasRobbers = function () {
	var count = 0;
	Object.getOwnPropertyNames(this.robbers).forEach(function () {
		count++;
	});
	return count > 0;
};
GameModel.prototype.removeRobber = function (clientId) {
	var client = this.robbers[clientId];
	delete this.robbers[clientId];
	delete this.idMap[client.socket.id];
};
GameModel.prototype.getRobbersInRadius = function (lat, lng) {
	var arrestables = [], that = this;
	Object.getOwnPropertyNames(this.robbers).forEach(function (clientId) {
		var robber = that.robbers[clientId],
			distance;

		if (robber.lat && robber.lng) {
			distance = GeoUtils.calculateDistance(lat, lng, robber.lat, robber.lng);
			if (distance <= that.arrestRadius) {
				arrestables.push({
					name: robber.name,
					clientId: clientId,
					lat: robber.lat,
					lng: robber.lng,
					distance: distance
				});
			}
		}
	});
	return arrestables;
};
/* expose to exports */
exports.create = function () {
	return new GameModel();
};