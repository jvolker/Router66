<?php

$fp = fsockopen("localhost", 12345, $errno, $errstr, 2);
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

	echo htmlentities($in);
}
?>