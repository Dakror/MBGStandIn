<?php
$start = microtime(true);

error_reporting(-1);
/**
 * @author Maximilian Stark | Dakror
 */
require_once "vendor/autoload.php";

require_once "standinparser/StandInParser.php";

header('Content-Type: text/html; charset=utf-8');

function _die($code) {
	$codes = array('100' => 'Continue','101' => 'Switching Protocols','200' => 'OK','201' => 'Created','202' => 'Accepted','203' => 'Non-Authoritative Information','204' => 'No Content','205' => 'Reset Content','206' => 'Partial Content','300' => 'Multiple Choices','301' => 'Moved Permanently','302' => 'Moved Temporarily','303' => 'See Other','304' => 'Not Modified','305' => 'Use Proxy','400' => 'Bad Request','401' => 'Unauthorized','402' => 'Payment Required','403' => 'Forbidden','404' => 'Not Found','405' => 'Method Not Allowed','406' => 'Not Acceptable','407' => 'Proxy Authentication Required','408' => 'Request Time-out','409' => 'Conflict','410' => 'Gone','411' => 'Length Required','412' => 'Precondition Failed','413' => 'Request Entity Too Large','414' => 'Request-URI Too Large','415' => 'Unsupported Media Type','500' => 'Internal Server Error','501' => 'Not Implemented','502' => 'Bad Gateway','503' => 'Service Unavailable','504' => 'Gateway Time-out','505' => 'HTTP Version not supported');

	http_response_code($code);
	die($codes[$code]);
}

function isAprilFools() {
	$now = getdate();
	return $now["mday"] == 1 && $now["mon"] == 4;
}

function getAndroidVersion() {
	if ($c = preg_match_all("/.*?(Android).*?([+-]?\\d*\\.\\d+)(?![-+0-9\\.])/is", $_SERVER["HTTP_USER_AGENT"], $matches)) {
      $word1=$matches[1][0];
      $float1=$matches[2][0];
      if($word1 == "Android") return $float1;
  }
	
	return -1;
}

function _log($string) {
	file_put_contents("log/log.ini", $string, FILE_APPEND);
}

function logTime() {
	global $start;
	_log("duration[] = ".((microtime(true) - $start) * 1000)."\r\n");
}

/**
 *
 * Seperated by ',':
 * 1D4,10D,5A
 *
 */
$courses = @$_POST["courses"];
$pwd = @$_POST["pwd"];
$debug = array_key_exists("debug", $_POST);

if(!$pwd || !$courses) _die(400);

define("__DEBUG__", $debug);

$DEBUG_TABLE = array();

function d_echo($msg) {
	global $DEBUG_TABLE;
	array_push($DEBUG_TABLE, $msg);
}

$parser = new StandInParser();

$pwdRight = $parser->checkPassword($pwd); 

_log("version[] = ".getAndroidVersion()."\r\ntimestamp[] = ".(microtime(true) * 1000)."\r\ncourses[] = $courses\r\npassword[] = ".($pwdRight?"true":"false")."\r\n");

if(!$pwdRight) {
	logTime();
	_die(401);
}
		
$parser->update($pwd);

@include "pages/today.php";
@include "pages/tomorrow.php";

$table = $parser->load();

$courses = Course::toCourseArray(strtoupper($courses));

$standins = $table->getRelevantStandIns($courses);

$arr = array("date" => implode(".", $table->date), "standins" => apply_iJson($standins));
if($table->info) $arr["info"] = $table->info;

if($debug) $arr["debug"] = $DEBUG_TABLE;

echo json_encode($arr);
logTime();
?>
