<?php
$fp = fsockopen("localhost", 12345, $errno, $errstr, 3);
if (!$fp) {
    echo "$errstr ($errno)<br />\n";
} else {
    $out = "text request";
    fwrite($fp, $out);
    while (!feof($fp)) {	
        echo fgets($fp, 128);
    }
    fclose($fp);
}
?>