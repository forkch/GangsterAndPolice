/*global google: false, $: false, gap: false, maputil: false */
var gap = gap || {};
gap.arrestCircles = {};
(function (global) {

	var DEFAULT_CIRCLE_OPTIONS = {
			strokeColor: "#00FF00",
			strokeOpacity: 0.8,
			strokeWeight: 2,
			fillColor: "#00FF00",
			fillOpacity: 0.35,
			radius: 100
		},
		createCircle = function (spec) {
			var populationOptions = $.extend({}, DEFAULT_CIRCLE_OPTIONS, spec);
			return new google.maps.Circle(populationOptions);
		};

	gap.messageDispatcher = {
		position: function (data) {
			var latLng = new google.maps.LatLng(data.lat, data.lng),
				clientItem = $("#client-" + data.clientId);

			gap.mapController.updatePosition(data.clientId, data.lat, data.lng, data.role);
			if (gap.arrestCircles[data.clientId]) {
				gap.arrestCircles[data.clientId].setCenter(latLng);
			}
			clientItem.find(".lat").text(data.lat);
			clientItem.find(".lng").text(data.lng);
		},
		arrest: function (data) {
			gap.arrestCircles[data.clientId] = createCircle({
				map: gap.mapController.map,
				center: new google.maps.LatLng(data.lat, data.lng)
			});
			$.each(data.arrestables, function () {
				console.log("add arrest circle", this.clientId);
				gap.arrestCircles[this.clientId] = createCircle({
					map: gap.mapController.map,
					fillColor: "#ff5555",
					strokeColor: "#ff5555",
					center: new google.maps.LatLng(this.lat, this.lng)
				});
			});
		},
		"arrest-timeout": function (data) {
			if (gap.arrestCircles[data.clientId]) {
				gap.arrestCircles[data.clientId].setMap(null);
				delete gap.arrestCircles[data.clientId];

				$.each(data.arrestables, function () {
					gap.arrestCircles[this.clientId].setMap(null);
					delete gap.arrestCircles[this.clientId];
				});
			}
		},
		hit: function (data) {
			console.log("hit", data);
		}
	};
}(this));