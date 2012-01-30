<?php

$fp = fsockopen("localhost", 12345, $errno, $errstr, 10);
if (!$fp) {
    echo "$errstr ($errno)\n";
} else {
    $out = "text request";
    fwrite($fp, $out);
    while (!feof($fp)) {	
        echo fgets($fp, 128);
    }
    fclose($fp);
}
?>