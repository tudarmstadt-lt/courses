<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<meta name="description" content="Simon D.">
<meta name="author" content="">
<link rel="icon" href="inc/bootstrap-3.3.6/docs/favicon.ico">

<title>About</title>

<!-- The resource to jQuery has to be here because bootstrap requires jQuery -->
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>

<!-- Latest compiled and minified CSS -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
	integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
	crossorigin="anonymous">

<!-- Optional theme -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css"
	integrity="sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r"
	crossorigin="anonymous">

<!-- Latest compiled and minified JavaScript -->
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
	integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS"
	crossorigin="anonymous"></script>

<!-- Custom styles for dashboard template -->
<link href="inc/dashboard.css" rel="stylesheet" type="text/css" />

</head>
<body>
	<%@ include file="header.jsp" %>

	<div class="container">
	<br>
This web application was developed in the context of the practical course
		<a href="https://www.lt.informatik.tu-darmstadt.de/de/teaching/lectures-and-classes/summer-term-2016/question-answering-technologies-behind-ibm-watson/" target="_blank">"Question Answering Technologies behind and with IBM Watson"</a>
		at the TU Darmstadt during the summer semester 2016.
		<br><br>
		
		The goal is to create an Information Network representing the Marvel Universe, based on a <a href="http://marvelcinematicuniverse.wikia.com/wiki/Marvel_Cinematic_Universe_Wiki" target="_blank">online Marvel Wikia</a> and on technologies developed by IBM: the <a href="https://new-console.ng.bluemix.net/catalog/?category=watson" target="_blank">Bluemix services</a>.
		<br><br>
		Team members: Simon D., Simon S., Christoph S., Kai S.
	</div>
	<!-- /.container -->


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<script>window.jQuery || document.write('<script src="../../assets/js/vendor/jquery.min.js"><\/script>')</script>
	<script src="inc/bootstrap-3.3.6/dist/js/bootstrap.min.js"></script>
	<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
	<script
		src="inc/bootstrap-3.3.6/docs/assets/js/ie10-viewport-bug-workaround.js"></script>


</body>
</html>