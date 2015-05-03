<?php
require_once "StandInTable.php";
/**
 * @author Maximilian Stark | Dakror
 */
class StandInParser {
	const DATE_PATTERN = "/([0-9]+)\\.([0-9]+)\\./";
	
	const INTERVAL = 3000000; // 5 minutes // 300
	
	public function fetchFile($today, $pwd) {
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, "http://mbg-germering.de/mbg/".($today? "heute":"morgen").".pdf");
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_POST, 1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, "pwd=$pwd");
		$data = curl_exec($ch);
		curl_close($ch);
		
		return $data;
	}

	public function getDocument($content) {
		$parser = new Smalot\PdfParser\Parser();
		$doc = $parser->parseContent($content);
		
		return $doc;
	}
	
	public function getText($content, $page = -1) {
		$doc = $this->getDocument($content);
		$pages = $doc->getPages();

		return $doc->getText($page > -1 ? $pages[$page] : null);
	}
	
	public function parseAllPages($content) {
		$doc = $this->getDocument($content);
		$pages = $doc->getPages();
		
		$table = new StandInTable();
		
		foreach ($pages as $num => $page) {
			$this->parse($page->getText(), $table, $num != 0);
		}
		
		return $table;
	}
	
	public function getFilename($today) {
		return "pages/".($today ? "today" : "tomorrow").".php";
	}
	
	public function store($table, $lazy = false) {
		@mkdir("pages");

		$filename = $this->getFilename($table->today);
		
		if($lazy || time() - filemtime($filename) > self::INTERVAL) {
			file_put_contents($filename, "<?php\nconst".($table->today ? "__TODAY__" : "__TOMORROW__")."= '".serialize($table)."';\n?>", LOCK_EX);
		} elseif(__DEBUG__) echo "$filename up to date<br>";
	}
	
	public function load() {
		$now = getdate();
		
		$today = unserialize(__TODAY__);
		$tomorrow = unserialize(__TOMORROW__);
		
		if($this->isSameDay($now, $today)) {
			if($now["hours"] > 18 && $this->isFutureDay($date, $tomorrow) /* better safe than sorry. */) return $tomorrow; // after school show the standins for the next day
			return $today;
		} else {
			return $tomorrow;
		}
	} 
	
	public function isSameDay($date, $table) {
		return $table->date[0] == $date["mon"] && $table->date[1] == $date["mday"];
	}
	
	public function isFutureDay($date, $table) {
		return $table->date[0] > $date["mon"] || ($table->date[0] == $date["mon"] && $table->date[1] > $date["mday"]);
	}
	
	public function _update($today, $password) {
		$filename = $this->getFilename($today);
		
		if(time() - @filemtime($filename) > self::INTERVAL) {
			$table = $this->parseAllPages($this->fetchFile($today, $password), $password);
			$table->today = $today;
			$this->store($table, true);
		} elseif(__DEBUG__) echo "$filename up to date<br>";
	}
	
	public function update($password) {
		$this->_update(true, $password);
		$this->_update(false, $password);
	}
	
	public function parse($text, $existing_table = null, $skipHeader = false) {
		$table = $existing_table ? $existing_table : new StandInTable();
		$d = "\n";
		
		$tok = strtok($text, $d);

		$tok = strtok($d); // Max-Born Gymnasium
		$tok = strtok($d); // D-82110 Germering, Joh.-Seb.-Bach-Str. 8 Stundenplan 2014/15

		if(!$skipHeader) {
			$arr = array();
			preg_match(self::DATE_PATTERN, $tok, $arr);
			array_shift($arr); // remove group 0
			$table->date = $arr;
		}
		$tok = strtok($d);
		
		$list_started = false;
		
		$standin;
		
		while ($tok !== false) {
			$p = explode(" ", $tok);
			
			if($list_started) {
				if(count($p) < 5) {
					if($standin) {
						if($standin->isExpectingMoreCourses()) {
							$standin->addCourse($p[0]);
							if(__DEBUG__) echo "<font color=green>$tok</font><br>";
						} else {
							$standin->text.=" $tok";
							if(__DEBUG__) echo "<font color=blue>$tok</font><br>";
						}
						// TODO: Handle it if there is a course overflow as well as a text overflow!
						// TODO: Also count($p) < 5 is tricky with the text field
					}
				} else {				
					if(__DEBUG__) echo ">> $tok<br>";
					$standin = new StandIn();

					$i = 0;

					while(count($p) > 0) {
						$e = array_shift($p);
						
						if(strlen($e) == 0) continue; // empty string got stuck apparently.
						
						switch($i) {
							case 0: {
								$course = $e;
								$standin->addCourse($e);

								while($standin->isExpectingMoreCourses() && Course::isCourse($p[0])) {
									$e = array_shift($p);
									$standin->addCourse($e);
								}
	
								break;
							} 
							case 1: {
								array_push($standin->lessons, $e);
								if($p[0] == "-") {
									array_shift($p); // remove '-'
									$e = array_shift($p);
									array_push($standin->lessons, $e);
								}
							
								break;
							}
							case 2: {
								if ($e == "x") {
									$standin->free = true;
								} else {
									$standin->replacer = $e;
								}
								break;
							}
							case 3: {
								if (!$standin->free) {
									$standin->subject = $e;
								}
								break;
							}
							case 4: {
								if (!$standin->free) {
									$standin->room = $e;
									if($e == "Halle") { // fix for Halle 4
										$standin->room.=" ".array_shift($p);
									}
								}
								break;
							}
							default: {
								if($e == "---") break;
								
								$standin->text=$e;
								while(count($p) > 0) {
									$standin->text.=" ".array_shift($p);
								}
								break;
							}
						}
						$i++;
					}					
					
					$table->addStandIn($standin);
				}
			} elseif (strpos($tok, "Klasse") === 0) {
				$list_started = true;
			} elseif (!$skipHeader) {
				$table->info.=" $tok";
			}
			
			$tok = strtok($d);
		}
		
		return $table;
	}
}
?>
