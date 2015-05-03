<?php
require_once "Course.php";
/**
 * @author Maximilian Stark | Dakror
 */
class StandIn {
	protected $courses = array();
	public $lessons = array();
	
	public $free;
	public $replacer;
	public $subject;
	public $room;
	
	public $text;
	
	public $expects_more_courses = false;
	
	public function addCourse($string) {
		$this->expects_more_courses = strpos($string, ",") !== false;
		
		$string = str_replace(",", "", $string);
		array_push($this->courses, new Course($string));
	}
	
	public function getCourses() { return $this->courses; }
	
	public function __toString() {
		return "(".implode(",\t|\t\t|\t\t|\t\t|\t\t|\t\t|\n\t( ", $this->courses)."\t|\t".implode(" - ", $this->lessons)."\t|\t".strval($this->free)."\t|\t$this->replacer\t|\t$this->subject\t|\t$this->room\t|\t$this->text";
	}
}
?>