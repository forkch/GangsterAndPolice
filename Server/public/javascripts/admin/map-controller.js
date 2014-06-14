/*global google: false, maputil: false, $: false */
/*jslint browser: true */
(function (global) {
	var DEFAULT_MARKER_COLOR = "11bb11",
		COLOR_MAP = {
			"police": DEFAULT_MARKER_COLOR,
			"robber": "dd3333",
			"hit": "3333dd"
		},
		MapController = function (spec) {
			this.containerId = spec.containerId || "map-container";
			this.markers = {};
			this.clients = {};
			this.clickHandlers = [];
			this.map = new google.maps.Map(document.getElementById(this.containerId), {
				zoom: spec.zoom || 17,
				center: spec.center || new google.maps.LatLng(46.9284565, 7.1159752),
				mapTypeId: spec.mapType || google.maps.MapTypeId.SATELLITE
			});
			return this;
		},
		ClientMarker = function (spec) {
			this.circle = new google.maps.Circle({
				strokeColor: "#" + spec.color,
				strokeOpacity: 0.4,
				strokeWeight: 2,
				fillColor: "#" + spec.color,
				fillOpacity: 0.15,
				radius: 100,
				center: spec.latLng,
				map: spec.map
			});
			this.clientId = spec.clientId;
			this.latLng = spec.latLng;
			this.projection = spec.map.getProjection();
			this.marker = maputil.createMarker(spec.latLng, spec.color, spec.map, spec.name);
			google.maps.event.addListener(this.marker, "click", function () {
				if (spec.onclick) {
					spec.onclick.call(undefined, spec.clientId);
				}
			});
		};
		
	ClientMarker.prototype.getPosition = function () {
		return this.latLng;
	};	
	ClientMarker.prototype.setPosition = function (latLng) {
		this.latLng = latLng;
		this.marker.setPosition(latLng);
		this.circle.setCenter(latLng);
	};
	ClientMarker.prototype.remove = function () {
		this.marker.setMap(null);
		this.circle.setMap(null);
	};
	ClientMarker.prototype.getXY = function () {
		return this.projection.fromLatLngToPoint(this.latLng);
	};


	MapController.prototype.removeClient = function (clientId) {
		if (this.markers[clientId]) {
			this.markers[clientId].remove();
			delete this.markers[clientId];
		}
		delete this.clients[clientId];
	};
	MapController.prototype.centerBy = function(clientId) {
		var marker = this.markers[clientId];
		if (marker) {
			this.map.setCenter(marker.getPosition());
		}
	};
	MapController.prototype.onSelect = function (callback) {
		this.clickHandlers.push(callback);
	};
	MapController.prototype.onMarkerClick = function (clientId) {
		var that = this;
		$.each(this.clickHandlers, function () {
			this.call(that, that.clients[clientId]);
		});
	};
	MapController.prototype.updatePosition = function (clientId, lat, lng, role, name) {
		var latLng = new google.maps.LatLng(lat, lng),
			color = COLOR_MAP[role || "robber"] || DEFAULT_MARKER_COLOR,
			marker = this.markers[clientId],
			that = this;
		
		if (marker) {
			marker.setPosition(latLng);
		} else {
			this.markers[clientId] = new ClientMarker({
				clientId: clientId,
				latLng: latLng,
				color: color,
				map: this.map,
				name: name + " (" + clientId + ")",
				onclick: function (clientId) {
					that.onMarkerClick(clientId);
				}
			});
			this.clients[clientId] = {
				clientId: clientId,
				lat: lat,
				lng: lng,
				role: role,
				name: name
			};
		}
	};
	global.MapController = MapController;
}(this));