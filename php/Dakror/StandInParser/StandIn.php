<?php
require_once "Course.php";
/**
 * @author Maximilian Stark | Dakror
 */
class StandIn implements iJson {
	public $courses = array();
	public $lessons = array();
	
	public $free;
	public $replacer;
	public $subject;
	public $room;
	
	public $text;
	
	protected $expects_more_courses = false;
	
	public function addCourse($string) {
		$this->expects_more_courses = strpos($string, ",") !== false;
		
		$string = str_replace(",", "", $string);
		array_push($this->courses, new Course($string));
	}

	public function __toString() {
		return "(".implode(",\t|\t\t|\t\t|\t\t|\t\t|\t\t|\n( ", $this->courses)."\t|\t".implode(" - ", $this->lessons)."\t|\t".strval($this->free)."\t|\t$this->replacer\t|\t$this->subject\t|\t$this->room\t|\t$this->text";
	}
	
	public function isExpectingMoreCourses() { return $this->expects_more_courses; }
	
	public function toJson() {
		$out = array(
			"courses" => apply_iJson($this->courses),
			"lessons" => $this->lessons,
			"free" => $this->free,
			"replacer" => $this->replacer,
			"subject" => $this->subject,
			"room" => $this->room,
			"text" => $this->text,
		);
		
		return array_filter($out);
	}	
}
?>
