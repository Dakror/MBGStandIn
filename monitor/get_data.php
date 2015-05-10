<?php
header('Content-Type: text/javascript; charset=utf-8');

$page = -1;
include $_SERVER["DOCUMENT_ROOT"]."/assets/scripts/php/base.php";

$is_admin = @$_SESSION["USERNAME"] == $admin;

$DATE = "strftime('%Y-%m-%d %H:%M', TIMESTAMP, 'unixepoch', 'localtime')";

$DATES = "SELECT $DATE DATE FROM LOG GROUP BY DATE";
$CALLS = "SELECT COUNT(*) COUNT FROM LOG GROUP BY $DATE";
$DURATION = "SELECT AVG(DURATION) DURATION FROM LOG GROUP BY $DATE";

$TIMELINE = "SELECT COUNT(*) COUNT, AVG(DURATION) FROM LOG GROUP BY $DATE";

$VERSION = "SELECT VERSION, COUNT(VERSION) COUNT FROM LOG GROUP BY VERSION";
$PASSWD = "SELECT PASSWD, COUNT(PASSWD) COUNT FROM LOG GROUP BY PASSWD";

$db = new SQLite3("../log/log.db");

function getResult($query) {
	global $db, $is_admin;
	
	if(!$is_admin) return array();
	
	$res = $db->query($query);
	
	$result = array();
	while($row = $res->fetchArray(SQLITE3_NUM)) {
		if(count($row) == 1) array_push($result, $row[0]);
		else array_push($result, $row);
	}
	
	return $result;
}

/*
var timeline = ".json_encode(getResult($TIMELINE)).";
*/
echo "var dates = ".json_encode(getResult($DATES))."; // insert dates with 0 calls
var calls = ".json_encode(getResult($CALLS)).";
var duration = ".json_encode(getResult($DURATION)).";
var version = ".json_encode(getResult($VERSION)).";
var passwd = ".json_encode(getResult($PASSWD)).";
";
?>