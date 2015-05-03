<?php
/**
 * @author Maximilian Stark | Dakror
 */
interface iJson {
	public function toJson();
}

function apply_iJson($array) {
	$out = array();

	foreach($array as $elem) {
		array_push($out, $elem->toJson());
	}

	return $out;
}
?>