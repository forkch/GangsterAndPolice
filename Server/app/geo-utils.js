/*jslint node: true */
var toRad = function (degree) {
    return degree * Math.PI / 180;
};

exports.calculateDistance = function (lat1, lon1, lat2, lon2) {
	// http://www.movable-type.co.uk/scripts/latlong.html
	var R = 6371,
		dLat = toRad(lat2 - lat1),
		dLon = toRad(lon2 - lon1),
		a,
		c;
	lat1 = toRad(lat1);
	lat2 = toRad(lat2);
	a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
	    Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
	c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	return R * c;
};