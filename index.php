<?php
/**
 * @author Maximilian Stark | Dakror
 */
require_once "vendor/autoload.php";

require_once "php\Dakror\StandInParser\StandInParser.php";

@include "pages/today.php";
@include "pages/tomorrow.php";

header('Content-Type: text/html; charset=utf-8');

const __DEBUG__ = false;

error_reporting(-1);

set_time_limit(0);

$parser = new StandInParser();

// 37a08ed30093a133b1bb4ae0b8f3601f

$parser->update("37a08ed30093a133b1bb4ae0b8f3601f");

#echo nl2br($parser->getText( /*$parser->fetchFile(true, md5("einstein"))*/file_get_contents("morgen.pdf")));

#$table = $parser->parseAllPages(file_get_contents("morgen.pdf"));

#$parser->store($table);

#if(__DEBUG__) {
#	echo "<br><br>>>>>>>>>>>>>>>>>>>>>>><br><br><br><pre>";
#	echo $table;
#print_r($table->getStandIns()[2]);
#
#echo "<br><br>>>>>>>>>>>>>>>>>>>>>>><br><br><br>";
#
#print_r($table->getStandIns()[1]);
#
#echo "<br><br>>>>>>>>>>>>>>>>>>>>>>><br><br><br>";
#
#print_r($table->getStandIns()[19]);
#}
?>