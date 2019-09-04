/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
initialize: function() {
    document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
},
    
    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
onDeviceReady: function() {
    this.receivedEvent('deviceready');
    
    var success = function(message) {
        alert(message);
    }
    
    var failure = function() {
        alert("Error calling MileageTracker Plugin");
    }
    
	cordova.exec(success, failure, "MileageTracker", "greet", [name]);
		
	window.sqlitePlugin.selfTest(function() {
		alert("Sqlite SELF test OK");
	});
	
    document.getElementById("startTripButton").onclick = function () {
        cordova.exec(success, failure, "hello", "startTrip", ["10", "20"]);
        
    };
    document.getElementById("pauseTripButton").onclick = function () {
        cordova.exec(success, failure, "hello", "pauseTrip");
    };
    document.getElementById("stopTripButton").onclick = function () {
        cordova.exec(success, failure, "hello", "stopTrip");
    };
    
    var myDB = window.sqlitePlugin.openDatabase({name: "TripDB.db", location: 'default'});

    document.getElementById("viewTableDB").onclick = function () {
        console.log('viewTableDB click')
        myDB.transaction(function(transaction) {
                         transaction.executeSql('SELECT * FROM gps_table', [],
                                                function (tx, results) {
                                                    console.log('viewTableDB success');
                                                    var len = results.rows.length, i;
                                                    console.log('viewTableDB success length = ' + len);
                                                alert('viewTableDB success length = ' + len);
                                                    for (i = 0; i < len; i++){
                                                        console.log("[" + results.rows.item(i).id + ", " +
                                                                    results.rows.item(i).distance + ", " +
                                                                    results.rows.item(i).lat + ", " +
                                                                    results.rows.item(i).long + ", " +
                                                                    results.rows.item(i).timestamp + "]");
                                                    }
                                                },
                                                function(error) {
                                                    console.log('viewTableDB error');
                                                }
                                                );
                         });
    };
    var intervalTimeSeconds = 2000;
    setInterval(function() {
                console.log('setInterval')
                myDB.transaction(function(transaction) {
                                 transaction.executeSql('SELECT * FROM gps_table WHERE id=(SELECT MAX(id) FROM gps_table)', [],
                                                        function (tx, results) {
                                                            console.log('viewTableDB success');
                                                            var len = results.rows.length, i;
                                                            if(len > 0) {
                                                                document.getElementById('distanceTextArea').value = 'Distance = ' + results.rows.item(0).distance.toFixed(2) + ' meters';
                                                            }
                                                        },
                                                        function(error) {
                                                            console.log('viewTableDB error');
                                                        }
                                                        );
                                 });
                
                }, intervalTimeSeconds);
},
    
    // Update DOM on a Received Event
receivedEvent: function(id) {
    var parentElement = document.getElementById(id);
    var listeningElement = parentElement.querySelector('.listening');
    var receivedElement = parentElement.querySelector('.received');
    
    listeningElement.setAttribute('style', 'display:none;');
    receivedElement.setAttribute('style', 'display:block;');
    
    console.log('Received Event: ' + id);
}
};

app.initialize();

