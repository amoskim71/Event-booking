<?php 
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
  
if (isset($_POST['email']) && isset($_POST['sessionID'])) {
     
    $email = $_POST['email'];
	$session = $_POST['sessionID'];

	
	$response = $db->fullSync($email, $session);	
	echo json_encode($response);
} else {
    $response = array("error" => TRUE, "error_msg" => "BAD_PARAMS");
    echo json_encode($response);
}
?>