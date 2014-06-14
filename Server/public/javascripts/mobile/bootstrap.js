/*global io: false, google: false, $: false */
/*jslint browser: true */
(function () {
	
	var socket = io.connect('/'),
		map,
		markers = {},
		createMarker = function (latLng, color) {
			var pinColor = color || "00ff00",
				pinUrl = "http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|",
				pinImage = new google.maps.MarkerImage(pinUrl + pinColor,
			        new google.maps.Size(21, 34),
			        new google.maps.Point(0, 0),
			        new google.maps.Point(10, 34)),
				pinShadow = new google.maps.MarkerImage("http://chart.apis.google.com/chart?chst=d_map_pin_shadow",
			        new google.maps.Size(40, 37),
			        new google.maps.Point(0, 0),
			        new google.maps.Point(12, 35));

			return new google.maps.Marker({
				position: latLng,
				map: map,
				icon: pinImage,
				shadow: pinShadow,
				title: "Name des Spielers"
			});
		},
		getMarkerBounds = function () {
			var name, maxLat, maxLng, minLat, minLng,
				latLng, sw, ne, b;
			for (name in markers) {
				if (markers.hasOwnProperty(name)) {
					latLng = markers[name].getPosition();
					if (!maxLat || maxLat < latLng.lat()) {
						maxLat = latLng.lat();
					}
					if (!minLat || minLat > latLng.lat()) {
						minLat = latLng.lat();
					}
					if (!maxLng || maxLng < latLng.lng()) {
						maxLng = latLng.lng();
					}
					if (!minLng || minLng > latLng.lng()) {
						minLng = latLng.lng();
					}
				}
			}
			sw = new google.maps.LatLng(minLat, minLng);
			ne = new google.maps.LatLng(maxLat, maxLng);
			b = new google.maps.LatLngBounds(sw, ne);
			console.log("bounds", sw, ne);
			return b;
		};

	document.addEventListener("DOMContentLoaded", function () {
		var clientId = -1,
			currentPosition = new google.maps.LatLng(46.92841880655054, 7.119019179473867),
			mapOptions = {
				zoom: 14,
				center: new google.maps.LatLng(currentPosition.lat(), currentPosition.lng()),
				mapTypeId: google.maps.MapTypeId.ROADMAP
			},
			msgInput = $("#message"),
			messageList = $("#messages"),
			prependMessage = function (data) {
				console.log("prepend ", data);
				messageList.prepend("<div><pre>" + JSON.stringify(data, undefined, 3) + "</pre></div>");
			},
			getCurrentPosition = function (latLng) {
				currentPosition = latLng || currentPosition;
				return {
					clientId: clientId,
					lat: currentPosition.lat(),
					lng: currentPosition.lng()
				};
			},
			sendInputMessage = function () {
				if (msgInput.val()) {
					socket.emit('message', {
						clientId: clientId,
						message: msgInput.val(),
						time: new Date()
					});
					msgInput.val("");
				}
			},
			sendPosition = function (latLng) {
				socket.emit('position', getCurrentPosition(latLng));
			},
			arrest = function () {
				socket.emit('arrest', getCurrentPosition());
			},
			reset = function () {
				socket.emit('reset', {});
			},
			requestState = function () {
				socket.emit("state", {});
			},
			registerAs = function (role) {
				socket.emit('register', {
					role: role,
					name: "marcbaechinger"
				});
			};

		map = new google.maps.Map(document.getElementById('map'), mapOptions);
		google.maps.event.addListener(map, 'click', function (ev) {
			sendPosition(ev.latLng);
		});


		$("#send").bind("click", function () {
			sendInputMessage();
		});

		$("#police").bind("click", function () {
			registerAs("police");
		});

		$("#robber").bind("click", function () {
			registerAs("robber");
		});

		$("#send-position").bind("click", function () {
			sendPosition();
		});

		$("#state").bind("click", function () {
			requestState();
		});

		$("#reset").bind("click", function () {
			reset();
		});
		
		$("#arrest").bind("click", function () {
			arrest();
		});

		socket.on("connect", function () {
			socket.on("message", function (data) {
				prependMessage(data);
			});
			socket.on("arrest-timeout", function (data) {
				prependMessage(data);
			});
			socket.on("all-robbers-arrested", function (data) {
				prependMessage(data);
			});
			socket.on("hit", function (data) {
				prependMessage(data);
			});
			socket.on("register", function (data) {
				prependMessage(data);
				clientId = data.clientId;
				$("head > title").text(data.clientId + " - " + data.role);
			});
			socket.on("position", function (data) {
				var marker, color = "FE7569",
					latLng = new google.maps.LatLng(data.lat, data.lng);
				console.log("position", data, map, latLng);
				if (markers[data.clientId]) {
					marker = markers[data.clientId];
					marker.setPosition(latLng);
				} else {
					if (data.role === "police") {
						color = "00ff00";
					}
					marker = createMarker(latLng, color);
					markers[data.clientId] = marker;
				}
				map.fitBounds(getMarkerBounds());
				prependMessage(data);
			});
			socket.on("state", function (data) {
				prependMessage(data);
			});
			socket.on("arrest", function (data) {
				prependMessage(data);
			});
		});
	}, false);
}());
