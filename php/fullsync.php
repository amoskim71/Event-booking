<?php 
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
  
if (isset($_POST['email']) && isset($_POST['sessionID']) && isset($_POST['token'])) {
     
    $email = $_POST['email'];
	$session = $_POST['sessionID'];
	$fbtoken = $_POST['token'];

	
	$response = $db->fullSync($email, $session, $fbtoken);	
	echo json_encode($response);
} else {
    $response = array("error" => TRUE, "error_msg" => "BAD_PARAMS");
    echo json_encode($response);
}
?>