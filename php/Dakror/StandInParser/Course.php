<?php
/**
 * @author Maximilian Stark | Dakror
 */
class Course {
	const Q_LEVEL_PATTERN = "/([12])([a-zA-Z]+)([1-9]+)/";
	const DEFAULT_PATTERN = "/([0-9]+)([a-zA-Z])/";
	
	public $grade;
	public $course;
	
	// -- qLevel -- //
	public $qLevel;
	public $qLevelCourse;
	public $qLevelSubject;
	
	// -- custom -- //
	public $name;
	
	public $desc;
	
	public function __construct($desc) {
		$this->desc = $desc;
		
		$matches = array();
		
		if(preg_match(self::Q_LEVEL_PATTERN, $desc, $matches)) {
			$this->qLevel = true;
			
			$this->grade = 10 + $matches[1];
			$this->qLevelSubject = $matches[2];
			$this->qLevelCourse = $matches[3];
		} else if (preg_match(self::DEFAULT_PATTERN, $desc, $matches)) {
			$this->grade = $matches[1];
			$this->course = $matches[2];
		} else {
			$this->name = $desc;
		}
	}
	
	public function __toString() {
		return $this->desc;
	}
	
	public static function isCourse($string) {
		return preg_match(Course::Q_LEVEL_PATTERN, $string) || preg_match(Course::DEFAULT_PATTERN, $string);
	}
}
?>