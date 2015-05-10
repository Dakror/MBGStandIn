<?php
$page = -1;
include $_SERVER["DOCUMENT_ROOT"]."/assets/scripts/php/base.php";

$is_admin = @$_SESSION["USERNAME"] == $admin;
?>
<!Doctype html>
<html>
	<head>
		<title>MBGStandIns Monitor</title>
		<link rel="shortcut icon" href="../../assets/img/dakror.png">
		<link href="../../assets/style/style.css" rel="stylesheet">
    <script src="../../assets/scripts/js/jquery-1.11.1.min.js"></script>
    <script src="../../assets/scripts/js/main.js"></script>
		
		<script src="js/Chart.min.js"></script>
		<script src="js/Please.js"></script>
		<script src="js/main.js"></script>
		<script src="get_data.php"></script>
		<style>
			.line-chart, .pie-chart {
				width:50%;
				height:300px;
				display:inline-block;
			}

		</style>
    <meta charset="utf-8">
	</head>
	<body><?php echo $header; ?>
		<section>
			<br>
				<?php
				if($is_admin) {
					echo '<div class="line-chart"><center><h4>Query calls</h4></center><canvas id="calls"></canvas></div><div class="line-chart" style="float:right"><center><h4>Avg. Execution time (ms)</h4></center><canvas id="duration"></canvas></div><br>
					<div class="pie-chart"><center><h4>Client Android Versions</h4></center><canvas id="version"></canvas></div><div class="pie-chart" style="float:right"><center><h4>Password Correctness</h4></center><canvas id="passwd"></canvas></div>';
				} else {
					echo '<div style="text-align:center;width:100%">You\'re not authorized to view this page.</div>';
				}
			?>
		</section>
		<?php echo $footer; ?>
	</body>
</html>