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

?>