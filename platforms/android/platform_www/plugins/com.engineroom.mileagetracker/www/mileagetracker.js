cordova.define("com.engineroom.mileagetracker.mileagetracker", function(require, exports, module) {
/*global cordova, module*/

module.exports = {
    greet: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MileageTracker", "greet", [name]);
    }
};

});
