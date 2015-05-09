<?php
require_once "StandIn.php";
/**
 * @author Maximilian Stark | Dakror
 */
class StandInTable {
	# easily stored as month, day
	public $date = array();
	
	public $today;
	
	public $info;
	
	protected $standins = array();
	
	public function addStandIn($standin) {
		array_push($this->standins, $standin);
	}
	
	public function getStandIns() { return $this->standins; }
	
	public function getRelevantStandIns($courses) {
		$out = array();
		foreach($this->standins as $standin) {
			if(count(array_intersect($courses, $standin->courses)) > 0) {
				array_push($out, $standin);
			}
		}
		
		return $out;
	}
	
	public function __toString() {
		return implode(".", $this->date)."<br>$this->info<br>[\t".implode("<br>", $this->standins)."<br>]";
	}
}
?>