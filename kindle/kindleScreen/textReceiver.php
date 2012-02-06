<?php

$fp = fsockopen($_GET['ip'], 12345, $errno, $errstr, 5);
if (!$fp) {
    echo "$errstr ($errno)\n";
} else {
    $out = "text request\n.\n";
    fwrite($fp, $out);

	$in = "";
    while (!feof($fp)) {	
        $in .= fgets($fp, 128);
    }
    fclose($fp);

	echo htmlspecialchars($in);
}
?>