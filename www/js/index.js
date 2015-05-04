var app = {
	initialize: function() {
		this.bindEvents();
	},
	// Bind Event Listeners
	//
	// Bind any events that are required on startup. Common events are:
	// 'load', 'deviceready', 'offline', and 'online'.
	bindEvents: function() {
		document.addEventListener('deviceready', this.onDeviceReady, false);
	},
	// deviceready Event Handler
	//
	// The scope of 'this' is the event. In order to call the 'receivedEvent'
	// function, we must explicitly call 'app.receivedEvent(...);'
	onDeviceReady: function() {
		//app.receivedEvent('deviceready');
		
		$.support.cors = true;

		/*$.post("http://dakror.de/MBGStandIns/", {
			"courses": "9D,10A,1E5",
			"pwd": password
		}, function(data) {
			console.log("wfgweegf");
		});*/
		
		$.ajax({
			type: "POST",
			url: "http://dakror.de/MBGStandIns/index.php",
			timeout: 60 * 1000,
			data: {
				courses: "9D",
				pwd: app.getMD5("einstein")
			},
			fail: function (a, b, c) {
				console.log(b + '|' + c);
			},
			success: function(data) {
				console.log("ata");
			},
			done: function(data) {
				console.log("g");
			}
		});
		
		/*var data = [{
				"courses": ["1D4"],
				"lessons": ["1", "2"],
				"free": true
			},
			{
				"courses": ["1D4"],
				"lessons": ["5", "6"],
				"subject": "D",
				"replacer": "EVA",
				"room": "205"
			}
		];*/
	},
	getMD5: function(text) {
		return CryptoJS.MD5(text).toString(CryptoJS.enc.Hex);
	},
	// Update DOM on a Received Event
	/*receivedEvent: function(id) {
		var parentElement = document.getElementById(id);
		var listeningElement = parentElement.querySelector('.listening');
		var receivedElement = parentElement.querySelector('.received');

		listeningElement.setAttribute('style', 'display:none;');
		receivedElement.setAttribute('style', 'display:block;');

		console.log('Received Event: ' + id);
	}*/
};
