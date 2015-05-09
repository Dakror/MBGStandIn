<?php
$source = parse_ini_file("../../../log/log.ini", true);

$dest = array();
$keys = array_keys($source);
for($i = 0; $i < count($source[$keys[0]]); $i++) {
	$dest[$i] = array();	
	foreach($keys as $k => $v) {
		$dest[$i][$k] = $source[$v][$i];
	}
}

echo json_encode($dest);
?>