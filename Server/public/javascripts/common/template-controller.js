/*global $: false, Mustache: false */
/*jslint browser: true */
(function (global) {
	var DEFAULT_SPEC = {},
		/**
		 * controller to manage rendering and event dispatching within the client list 
		 */
		TemplateController = function (spec) {
			var that = this;
			spec = spec || DEFAULT_SPEC;
			if (spec.include) {
				$.extend(this, spec.include);
			}
			this.container = $("#" + spec.containerId);
			this.actions = spec.actions || {};
			this.container.bind("click", function(ev) {
				var target = $(ev.target),
					action = target.closest("[data-action]").data("action");
				if (that.actions[action]) {
					that.actions[action].call(that, ev);
				}
			})
			this.model = spec.model || {};
			this.template = $("#" +  spec.templateId).text();
			this.render(this.model);
			return this;
		};
	
	TemplateController.prototype.setModel = function (model) {
		this.model = model;
		this.render();
	};
	TemplateController.prototype.render = function (model) {
		console.log("controller renders view", this.model);
		this.container.html(Mustache.render(this.template, model || this.model));
	};
	
	global.TemplateController = TemplateController;
	
}(this));