<?php 
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
  
if (isset($_POST['email']) && isset($_POST['sessionID']) && isset($_POST['eventID']) && isset($_POST['name']) && isset($_POST['when']) && isset($_POST['long']) && isset($_POST['lat'])) {
     
    $email = $_POST['email'];
	$session = $_POST['sessionID'];
	$event = $_POST['eventID'];
	$name = $_POST['name'];
    $whenEvent = $_POST['when'];
	$longitude = $_POST['long'];
	$latitude = $_POST['lat'];

	$response = $db->updateEvent($email, $session, $event, $name, $whenEvent, $longitude, $latitude);	
	echo json_encode($response);
} else {
    $response = array("error" => TRUE, "error_msg" => "BAD_PARAMS");
    echo json_encode($response);
}
?>