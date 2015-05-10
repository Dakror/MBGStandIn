function getRandomColor() {
	var str = Please.make_color({
			format : "rgb-string"
		})[0];
	return str.substring(0, str.length - 1).replace("rgb", "rgba");
}

$(document).ready(function () {
	console.log(getRandomColor());

	Chart.defaults.global.responsive = true;
	Chart.defaults.global.scaleLineColor = "rgba(255,255,255,.3)";
	Chart.defaults.global.scaleBeginAtZero = true;
	Chart.defaults.global.maintainAspectRatio = false;

	// -- calls chart -- //
	var c1 = getRandomColor();
	new Chart(document.getElementById("calls").getContext("2d")).Bar({
		labels : dates,
		datasets : [{
				label : "Query calls per minute",
				fillColor : c1 + ",0.2)",
				strokeColor : c1 + ",1)",
				pointColor : c1 + ",1)",
				pointStrokeColor : "#fff",
				pointHighlightFill : "#fff",
				pointHighlightStroke : c1 + ",1)",
				data : calls
			}
		]
	}, {
		scaleGridLineColor : "rgba(255,255,255,.1)"
	});

	// -- duration chart -- //
	var c2 = getRandomColor();
	new Chart(document.getElementById("duration").getContext("2d")).Bar({
		labels : dates,
		datasets : [{
				label : "Execution Time",
				fillColor : c2 + ", 0.2)",
				strokeColor : c2 + ", 151)",
				pointColor : c2 + ", 151)",
				pointStrokeColor : "#fff",
				pointHighlightFill : "#fff",
				pointHighlightStroke : c2 + ", 1)",
				data : duration
			}
		]
	}, {

		scaleGridLineColor : "rgba(255,255,255,.1)"
	});

	// -- version doughnut -- //
	var versionData = [];
	for (var v in version) {
		var color = Please.make_color({
				format : "hsv"
			})[0];
		var colors = Please.make_scheme(color, "mono");

		var vers = version[v][0];
		if (vers == "-1")
			vers = "Not Android";
		else
			vers = "Android " + vers;

		versionData.push({
			'value' : version[v][1],
			'color' : colors[0],
			'highlight' : colors[1],
			'label' : vers
		});
	}

	new Chart(document.getElementById("version").getContext("2d")).Doughnut(versionData);
	
	// -- password doughnut -- //
		var passwdData = [];
	for (var v in passwd) {
		var color = Please.make_color({
				format : "hsv"
			})[0];
		var colors = Please.make_scheme(color, "mono");

		var pwd = passwd[v][0];
		if (pwd == "0") pwd = "Wrong Password";
		else pwd = "Correct Password";

		passwdData.push({
			'value' : passwd[v][1],
			'color' : colors[0],
			'highlight' : colors[1],
			'label' : pwd
		});
	}

	new Chart(document.getElementById("passwd").getContext("2d")).Doughnut(passwdData);
	
});
