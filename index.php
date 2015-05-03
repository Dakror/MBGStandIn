<?php
/**
 * @author Maximilian Stark | Dakror
 */
require_once "vendor/autoload.php";

require_once "php/Dakror/StandInParser/StandInParser.php";

@include "pages/today.php";
@include "pages/tomorrow.php";

header('Content-Type: text/html; charset=utf-8');

/**
 *
 * Seperated by ',':
 * 1D4,10D,5A
 *
 */
$courses = @$_POST["courses"];
$pwd = @$_POST["pwd"];
$debug = array_key_exists("debug", $_POST);

if(!$pwd || !$courses) die();

define("__DEBUG__", $debug);

error_reporting(-1);

set_time_limit(1337);

$parser = new StandInParser();

$parser->update($pwd);
$table = $parser->load();

#$table = $parser->parseAllPages(file_get_contents("morgen.pdf"));

$courses = Course::toCourseArray(strtoupper($courses));

$standins = $table->getRelevantStandIns($courses);

die(json_encode(apply_iJson($standins)));
?>
