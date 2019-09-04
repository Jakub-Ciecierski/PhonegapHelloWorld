cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
  {
    "id": "cordova-sqlite-storage.SQLitePlugin",
    "file": "plugins/cordova-sqlite-storage/www/SQLitePlugin.js",
    "pluginId": "cordova-sqlite-storage",
    "clobbers": [
      "SQLitePlugin"
    ]
  },
  {
    "id": "com.engineroom.mileagetracker.mileagetracker",
    "file": "plugins/com.engineroom.mileagetracker/www/mileagetracker.js",
    "pluginId": "com.engineroom.mileagetracker",
    "clobbers": [
      "mileagetracker"
    ]
  }
];
module.exports.metadata = 
// TOP OF METADATA
{
  "cordova-sqlite-storage": "3.3.0",
  "com.engineroom.mileagetracker": "0.7.0"
};
// BOTTOM OF METADATA
});