/*global google: false, io: false, $: false, MapController: false,
TemplateController: false, maputil: false, gap: false */
/*jslint browser: true */
(function (global) {
	var mapController = new MapController({
			containerId: "admin-map",
			zoom: 17,
			center: new google.maps.LatLng(46.9284565, 7.1159752),
			mapType: google.maps.MapTypeId.SATELLITE
		}),
		stateViewController = new TemplateController({
			containerId: "client-list",
			templateId: "client-list-tmpl",
			actions: {
				"select-client": function(ev) {
					var target =  $(ev.target).closest("[data-id]"),
						clientId = target.data("id");
					mapController.centerBy(clientId);
					
					$("#client-list > div").removeClass("selected");
					target.addClass("selected");
				}
			}
		}),
		messageDisplayController = new TemplateController({
			containerId: "admin-messages",
			templateId: "message-tmpl",
			model: {
				messages: []
			},
			include: {
				add: function (msg) {
					if (this.model.messages.length === 12) {
						this.model.messages.splice(this.model.messages.length - 1, 1);
					}
					this.model.messages.unshift(msg);
					this.render();
				}
			}
		}),
		socket = io.connect('/');

	global.gap.mapController = mapController;

	socket.on("connect", function () {
		// register client as admin immediatly
		socket.emit('register', {
			role: "admin",
			name: "marcbaechinger"
		});
		// register message dispatchers
		$.each(gap.messageDispatcher, function (name, value) {
			socket.on(name, value);
		});
		socket.on("register", function (data) {
			socket.emit("state", {});
		});
		socket.on("unregister", function (data) {
			mapController.removeClient(data.clientId);
			socket.emit("state", {});
		});
		socket.on("message", function (data) {
			data.name = $("#client-" + data.clientId + " .name").text();
			messageDisplayController.add(data);
		});
		socket.on("state", function (data) {
			stateViewController.setModel(data);
			$.each(data.policemen, function () {
				if (this.lat && this.lng) {
					mapController.updatePosition(this.clientId, this.lat, this.lng, "police", this.name);
				}
			});
			$.each(data.robbers, function () {
				if (this.lat && this.lng) {
					mapController.updatePosition(this.clientId, this.lat, this.lng, "robber", this.name);
				}
			});
		});
	});
}(this));
