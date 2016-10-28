<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<meta name="description" content="Simon D.">
<meta name="author" content="">

<!-- JQuery -->
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>


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



<title>Graph Visualisation</title>

<!-- Custom styles for dashboard template -->
<link href="inc/dashboard.css" rel="stylesheet" type="text/css" />

<script type="text/javascript" src="inc/vis.js"></script>
<link href="inc/vis.css" rel="stylesheet" type="text/css" />

<!-- Font Awesome icons -->
<link rel="stylesheet"
	href="http://maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">

<style type="text/css">
* {
	margin: 0;
	padding: 0;
}

#networkDiv {
	position: relative;
	float: left;
	width: 100%;
	height: 500px;
	top: 0px;
	left: 0px;
	border: 1px solid lightgray;
}

#informationDiv {
	position: relative;
	width: 30%;
	height: 500px;
	top: 0px;
	left: 0px;
	border: 1px solid lightgray;
}

#closeInfosBtn {
	float: right;
}
</style>

<script type="text/javascript">
		function draw() {
			var  nodesMap = {
	                <c:forEach items="${nodes}" var="node" varStatus="loopNodes">
	                    "${node.id}": { 
	                    	"id" : ${node.id}, 
	                    	"label" : "${node.text}", 
	                    	"type" : "${node.type}",
	                    	"group" : ${node.group}
	                    }${!loopNodes.last ? ',' : ''}
	                </c:forEach>
	            };
			
			var nodesDataSet = new vis.DataSet([]);
			
			for (var id in nodesMap) {
				//alert(JSON.stringify(nodesMap[id]));
				node = nodesMap[id];
				
				var iconCode = '\uf128';
				var nodeColor = '#66ff33';
			    if (node.type === "City" || node.type === "Country" || node.type === "StateOrCounty" || node.type === "Continent"){
			    	iconCode = '\uf041';
			    	nodeColor = '#0091ff';
			    }
			    else if (node.type == "Person") {
			    	iconCode = '\uf007';
			    	nodeColor = '#ff545f';
			    }
			    else if (node.type == "Organization" || node.type == "Company" || node.type == "Company" || node.type == "Company" ) {
			    	iconCode = '\uf1ad';
			    	nodeColor = '#794044';
			    }
			    else if (node.type == "Movie") {
			    	iconCode = '\uf008';
			    	nodeColor = '#000000';
			    }
			    nodesDataSet.add({
			      id: node.id, 
			      label: node.label, 
			      group: node.group,
			      color: nodeColor,
			      shape: 'icon',
			      icon: {
			        face: 'FontAwesome',
			        color: nodeColor,
			        code: iconCode
			      }
			    }); 
			}
				

			var edgesDataSet = new vis.DataSet([
				<c:forEach items="${edges}" var="edge" varStatus="loopEdges">
				{"from": ${edge.node1}, "to": ${edge.node2}}${!loopEdges.last ? ',' : ''}
				</c:forEach>                                
			]);

			// create a network
			var container = document.getElementById('networkDiv');
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
				    forceAtlas2Based: {
				        gravitationalConstant: -500,
				        centralGravity: 0.07,
				        springConstant: 0.08,
				        springLength: 100,
				        damping: 0.6,
				        avoidOverlap: 0
				      },
					solver : 'forceAtlas2Based',
					timestep: 0.15,
					stabilization : {
						enabled : true,
						iterations : 130,
						updateInterval : 50
					}
				}
			};

		var network = new vis.Network(container, data, options);

		var doubleClickTime = 0;
		var threshold = 200;

		network.on('selectNode', function onSelectNode(params) {
		    // handles double click (maybe we want to add a feature with double click later)
			var t0 = new Date();
		    if (t0 - doubleClickTime > threshold) {
		        setTimeout(function () {
		            if (t0 - doubleClickTime > threshold) {
		    		    neighbourhoodHighlight(params);
		            	doOnSelectNode(params);
		            }
		        },threshold);
		    }
		});
		
		network.on('doubleClick', function onDoubleClick(params) {
		    doubleClickTime = new Date();
		    console.log("execute onDoubleClick function");
		    resetColors();
		});
		
		function doOnSelectNode(params){
		    var nodesIds = params.nodes; 
			if (nodesIds.length < 1) return;
			if (network.isCluster(params.nodes[0])) {
			} else {
				var nodeId = nodesIds[0];
				document.getElementById("infosNodeEdgeDiv").innerHTML = "Information about "+nodesMap[nodeId]["label"]+" ("+nodesMap[nodeId]["type"]+")";
			}
			displayInfos();
		}
		
		network.on("selectEdge", function(params) {
			var edgesIds = params.edges;
		    //alert("select edge fired");
		    if (edgesIds.length>0){
			    var clickedEdges = edgesDataSet.get(edgesIds);
			    if (clickedEdges.length>0 && clickedEdges[0] != null){
				    var edge = clickedEdges[0];
				    var node1Id = edge["from"];
			    	var node2Id = edge["to"];
			    	document.getElementById("infosNodeEdgeDiv").innerHTML = "Information about relationship between "+nodesMap[node1Id]["label"]+" ("+nodesMap[node1Id]["type"]+") and "+nodesMap[node2Id]["label"]+" ("+nodesMap[node2Id]["type"]+")";
					displayInfos();
			    }
		    }
		});
		
		document.getElementById("closeInfosBtn").onclick = closeInfos;
		
		function displayInfos(){
			var informationDivEl = document.getElementById('informationDiv');
			informationDivEl.style.display = "";
			var networkDivEl  = document.getElementById('networkDiv');
			networkDivEl.style.width = '70%';
		}
		
		function closeInfos(){
			var informationDivEl = document.getElementById('informationDiv');
			informationDivEl.style.display = "none";
			var networkDivEl  = document.getElementById('networkDiv');
			networkDivEl.style.width = '100%';
			document.getElementById("infosNodeEdgeDiv").innerHTML = "";
		}
		
	    function searchNode(){
	        nodeLabel = document.getElementById('searchInput').value;
	        //alert(nodeLabel);
	        var items = nodesDataSet.getIds({
	          filter: function (item) {
	            return (item.label === nodeLabel);
	          }
	        });
	        //alert(items.length+'filtered items' + JSON.stringify(items));
	        network.selectNodes(items);
	      }

	      $('#searchNodeForm').submit(function () {
	        searchNode();
	        return false;
	      });
	      
	   		// get a JSON object
	      allNodes = nodesDataSet.get({returnType:"Object"});

	      //network.on("click",neighbourhoodHighlight);
	      
		  document.getElementById("rstColorsBtn").onclick = resetColors;

		  hiddenColors = [];
		  
	      function resetColors() {
	    	  //alert("allNodes = "+JSON.stringify(allNodes));
	    	  for (var nodeId in allNodes) {
	    		if (hiddenColors[nodeId] !== undefined) {
	    		  allNodes[nodeId].icon.color = hiddenColors[nodeId];
	    		  hiddenColors[nodeId] = undefined;
	    		} else {
	    			alert("allNodes[nodeId].icon.hiddenColor === undefined");
	    		}
	   	      }
	   	      highlightActive = false
	      }
	      
	      function neighbourhoodHighlight(params) {
	    	    // if something is selected:
	    	    if (params.nodes.length > 0) {
	    	      highlightActive = true;
	    	      var i,j;
	    	      var selectedNode = params.nodes[0];
	    	      var degrees = 2;

	    	      // mark all nodes as hard to read.
	    	      for (var nodeId in allNodes) {
	    	    	if (hiddenColors[nodeId] === undefined) {
	    	    		hiddenColors[nodeId] = allNodes[nodeId].icon.color;
	    	    	}
	    	    	if (allNodes[nodeId].icon.color !== undefined) {
	    	    		allNodes[nodeId].icon.color = 'rgba(200,200,200,0.5)';
	    	    	}
	    	        if (allNodes[nodeId].hiddenLabel === undefined) {
	    	          allNodes[nodeId].hiddenLabel = allNodes[nodeId].label;
	    	          allNodes[nodeId].label = undefined;
	    	        }
	    	      }
	    	      var connectedNodes = network.getConnectedNodes(selectedNode);
	    	      var allConnectedNodes = [];

	    	      // get the second degree nodes
	    	      for (i = 1; i < degrees; i++) {
	    	        for (j = 0; j < connectedNodes.length; j++) {
	    	          allConnectedNodes = allConnectedNodes.concat(network.getConnectedNodes(connectedNodes[j]));
	    	        }
	    	      }

	    	      // all second degree nodes get a different color and their label back
	    	      for (i = 0; i < allConnectedNodes.length; i++) {
	    	    	if (allNodes[allConnectedNodes[i]].icon.color !== undefined) {
	    	    	  allNodes[allConnectedNodes[i]].icon.color = 'rgba(150,150,150,0.75)';
	    	    	}
	    	        if (allNodes[allConnectedNodes[i]].hiddenLabel !== undefined) {
	    	          allNodes[allConnectedNodes[i]].label = allNodes[allConnectedNodes[i]].hiddenLabel;
	    	          allNodes[allConnectedNodes[i]].hiddenLabel = undefined;
	    	        }
	    	      }

	    	      // all first degree nodes get their own color and their label back
	    	      for (i = 0; i < connectedNodes.length; i++) {
	    	    	if (hiddenColors[connectedNodes[i]] !== undefined) {
	    	    	  allNodes[connectedNodes[i]].icon.color = hiddenColors[connectedNodes[i]];
	    	    	}
	    	        //allNodes[connectedNodes[i]].icon.hiddenColor = undefined;
	    	        if (allNodes[connectedNodes[i]].hiddenLabel !== undefined) {
	    	          allNodes[connectedNodes[i]].label = allNodes[connectedNodes[i]].hiddenLabel;
	    	          allNodes[connectedNodes[i]].hiddenLabel = undefined;
	    	        }
	    	      }

	    	      // the main node gets its own color and its label back.
	    	      if (hiddenColors[selectedNode] !== undefined) {
	    	     	allNodes[selectedNode].icon.color = hiddenColors[selectedNode];
	    	      }
	    	      //allNodes[selectedNode].icon.hiddenColor = undefined;
	    	      if (allNodes[selectedNode].hiddenLabel !== undefined) {
	    	        allNodes[selectedNode].label = allNodes[selectedNode].hiddenLabel;
	    	        allNodes[selectedNode].hiddenLabel = undefined;
	    	      }
	    	    }
	    	    else if (highlightActive === true) {
	    	      // reset all nodes
	    	      for (var nodeId in allNodes) {
	    	    	if (hiddenColors[nodeId] !== undefined) {
	    	    	  allNodes[nodeId].icon.color = hiddenColors[nodeId];
	    	    	}
	    	        //allNodes[nodeId].icon.hiddenColor = undefined
	    	        if (allNodes[nodeId].hiddenLabel !== undefined) {
	    	          allNodes[nodeId].label = allNodes[nodeId].hiddenLabel;
	    	          allNodes[nodeId].hiddenLabel = undefined;
	    	        }
	    	      }
	    	      highlightActive = false
	    	    }

	    	    // transform the object into an array
	    	    var updateArray = [];
	    	    for (nodeId in allNodes) {
	    	      if (allNodes.hasOwnProperty(nodeId)) {
	    	        updateArray.push(allNodes[nodeId]);
	    	      }
	    	    }
	    	    nodesDataSet.update(updateArray);
	    	  }
	      
	     
	      
	      
	}
</script>

</head>
<body onload="draw()">

	<%@ include file="header.jsp"%>


	<div class="container">
		<div class="page-header" style="margin-top: 0px;">
			<h1 style="text-align: center; margin-top: 0px;">Alchemy
				Entities on all articles (after filtering)</h1>
		</div>
		<form id="searchNodeForm">
			<div class="input-group">
				<span class="input-group-addon"><i class="fa fa-search fa-fw"
					aria-hidden="true"></i></span> <input class="form-control" type="search"
					placeholder="Search a node" id="searchInput">
			</div>
			<input type="submit" style="display: none">
		</form>
		<button id="rstColorsBtn">Reset colors</button>
		<div id="networkDiv"></div>
		<div id="informationDiv" style="display: none; margin-left: 70%;">
			<button id="closeInfosBtn">x</button>
			<div id="infosNodeEdgeDiv"></div>
		</div>
	</div>

</body>
</html>