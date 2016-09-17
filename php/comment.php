<?php 
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
  
if (isset($_POST['email']) && isset($_POST['sessionID']) && isset($_POST['eventID']) && isset($_POST['post'])) {
     
    $email = $_POST['email'];
	$session = $_POST['sessionID'];
	$eventID = $_POST['eventID'];
    $post = $_POST['post'];

	$response = $db->storeComment($email, $session, $eventID, $post);	
	echo json_encode($response);
} else {
    $response = array("error" => TRUE, "error_msg" => "BAD_PARAMS");
    echo json_encode($response);
}
?>
