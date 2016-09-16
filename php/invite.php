<?php 
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
  
if (isset($_POST['session']) && isset($_POST['email']) && isset($_POST['eventID']) && isset($_POST['invites'])) {
     
	$session = $_POST['session'];
	$email = $_POST['email'];
	$eventID = $_POST['eventID'];
    $emails = $_POST['invites'];

	$response = $db->invite($session, $email, $eventID, $emails);	
	echo json_encode($response);
} else {
    $response = array("error" => TRUE, "error_msg" => "BAD_PARAMS");
    echo json_encode($response);
}
?>