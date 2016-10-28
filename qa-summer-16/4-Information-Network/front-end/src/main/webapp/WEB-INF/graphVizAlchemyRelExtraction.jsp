<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<meta name="description" content="Simon D.">
<meta name="author" content="">

<!-- Latest compiled and minified CSS -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">

<!-- Optional theme -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css" integrity="sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r" crossorigin="anonymous">

<!-- Latest compiled and minified JavaScript -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>

<title>Graph Visualisation</title>

<!-- Custom styles for dashboard template -->
<link href="inc/dashboard.css" rel="stylesheet" type="text/css" />

<script type="text/javascript" src="inc/vis.js"></script>
<link href="inc/vis.css" rel="stylesheet" type="text/css" />

<style type="text/css">
* {
	margin: 0;
	padding: 0;
}

#mynetwork {
	position: relative;
	width: 100%;
	height: 500px;
	top: 0px;
	left: 0px;
	border: 1px solid lightgray;
}
</style>

<script type="text/javascript">
	function draw() {
		var nodesDataSet = new vis.DataSet([]);
		var nodesJson = ${nodesJSON};
		for (var i = 0; i < nodesJson.length; i++) {
			var node = nodesJson[i];
			//alert("add node "+JSON.stringify(node));
			var nodeId = node["id"];
			var nodeLabel = node["properties"]["name"][0]["value"] + "("+node["label"]+")";
			//var nodeImage = node["image"];
			nodesDataSet.add({
				'id' : nodeId,
				'label' : nodeLabel,
				//'image' : nodeImage,
				'shape' : "dot"
			});
		}

		var edgesDataSet = new vis.DataSet([]);
		var edgesJson = ${edgesJSON};
		
		for (var i = 0; i < edgesJson.length; i++) {
			var edge = edgesJson[i];
			var edgeFrom = edge["inV"];
			var edgeTo = edge["outV"];
			var edgeTitle = edge["label"]
			edgesDataSet.add({
				from : edgeFrom,
				to : edgeTo,
				title : edgeTitle,
				label : edgeTitle
			});
		}	
		

		// create a network
		var container = document.getElementById('mynetwork');
		var data = {
			nodes : nodesDataSet,
			edges : edgesDataSet
		};
		var options = {
			nodes : {
				color : {
					border : '#222222',
					background : '#666666'
				},
				font : {
					size : 24,
					color : '#000000'
				},
				shape : 'dot',
				scaling : {
					label : {
						min : 8,
						max : 20
					}
				}
			},
			edges : {
				/*arrows : {
					to : 'true'
				},*/
				color : {
					color : 'lightgray',
					highlight : 'black',
					hover : 'black'
				},
				smooth : false,
				width : 5
			},
			layout : {
				randomSeed : 8
			},
			interaction : {
				hover : false,
				hoverConnectedEdges : false
			},
			physics : {
				//maxVelocity: 146,
				forceAtlas2Based: {
			      gravitationalConstant: -500,
			      centralGravity: 0.05,
			      springConstant: 0.08,
			      springLength: 100,
			      damping: 0.8,
			      avoidOverlap: 0.3
			    },
			    barnesHut: {
					gravitationalConstant: -10000,
					centralGravity: 0.01,
					springLength: 150,
					springConstant: 0.08,
					damping: 0.9,
					avoidOverlap: 0
			    },
				solver : 'forceAtlas2Based',
				//timestep: 0.35,
				stabilization : {
					enabled : true,
					iterations : 1000,
					updateInterval : 50
				}
			}
		};

	var network = new vis.Network(container, data, options);
	
	}
</script>

</head>
<body onload="draw()">

	<%@ include file="header.jsp"%>

	<div class="container">
		<div class="page-header" style="margin-top: 0px;">
			<h1 style="text-align: center; margin-top: 0px;">Relationship Extraction with 1 article</h1>
		</div>
		<div id="mynetwork"></div>
	</div>


	<!-- Placed at the end of the document so the pages load faster -->
	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<script>
		window.jQuery
				|| document
						.write('<script src="../../assets/js/vendor/jquery.min.js"><\/script>')
	</script>

</body>
</html>