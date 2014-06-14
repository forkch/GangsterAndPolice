/*global google: true */
(function (global) {
	var MARKER_URL = "http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|",
		MARKER_SHADOW_API = "http://chart.apis.google.com/chart?chst=d_map_pin_shadow",
		createMarker = function (latLng, color, map, name) {
			var pinColor = color || "00ff00",
				pinImage = new google.maps.MarkerImage(MARKER_URL + pinColor,
			        new google.maps.Size(21, 34),
			        new google.maps.Point(0, 0),
			        new google.maps.Point(10, 34)),
				pinShadow = new google.maps.MarkerImage(MARKER_SHADOW_API,
			        new google.maps.Size(40, 37),
			        new google.maps.Point(0, 0),
			        new google.maps.Point(12, 35));

			return new google.maps.Marker({
				position: latLng,
				animation: google.maps.Animation.DROP,
				map: map,
				icon: pinImage,
				shadow: pinShadow,
				title: name
			});
		};

	global.maputil = {};
	global.maputil.createMarker = createMarker;
}(this));