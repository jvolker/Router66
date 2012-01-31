<?php

$fp = fsockopen("localhost", 12345, $errno, $errstr, 2);
if (!$fp) {
    echo "$errstr ($errno)\n";
} else {
    $out = "text request\n.\n";
    fwrite($fp, $out);

    while (!feof($fp)) {	
        echo fgets($fp, 128);
    }
    fclose($fp);
}
?>