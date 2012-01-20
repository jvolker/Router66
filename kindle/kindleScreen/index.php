<!DOCTYPE html>
<html lang="en" manifest="cache.manifest">
  <head>
    <meta charset="utf-8">
    <title>KindleScreen</title>
    <meta name="description" content="">
    <meta name="author" content="">
	
    <link rel="stylesheet" type="text/css" href="css/global.css" />
	
	  
	<script src="js/jquery-1.7.1.min.js" type="text/javascript" charset="utf-8"></script>	
	
	<script type="text/javascript">
	$(document).ready(function() {
		 refreshText = function() {
			var xmlhttp=new XMLHttpRequest();
	
			xmlhttp.onreadystatechange=function() {
		  		if (xmlhttp.readyState==4 && xmlhttp.status==200) {
		    		$('#text').html(xmlhttp.responseText);
		    	}
		  	}
	
			xmlhttp.open("GET","helloClient.txt",true);
			xmlhttp.send();
			
			setTimeout("refreshText()",500); 
		}

		setTimeout("refreshText()",500); 
	});
	</script>
	
  </head>

  <body>
 	<div id="screen"><div id="text">...</div></div>
 </body>
</html>
