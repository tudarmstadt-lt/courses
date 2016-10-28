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

body {
	background: #232323 !important;
	background-image: url(img/marvel_logo_background.png) !important;
}

img.center {
	display: block;
	margin-left: auto;
	margin-right: auto;
}

#networkDiv {
	position: relative;
	float: left;
	width: 100%;
	height: 100%;
	top: 0px;
	left: 0px;
	border: 1px solid lightgray;
}

#informationDiv {
	position: relative;
	width: 30%;
	height: 100%;
	top: 0px;
	left: 0px;
	border: 1px solid lightgray;
}

#editDiv {
	position: relative;
	width: 30%;
	height: 100%;
	top: 0px;
	left: 0px;
	border: 1px solid lightgray;
}

#evalDiv {
	position: relative;
	width: 30%;
	height: 100%;
	top: 0px;
	left: 0px;
	border: 1px solid lightgray;
}

#closeInfosBtn {
	float: right;
}

#closeEditBtn {
	float: right;
}

#closeEvalBtn {
	float: right;
}

#infosNodeEdgeDiv {
	overflow: auto;
	overflow-y:scroll; 
    height: 100%;
}
</style>

<script type="text/javascript">
    function draw() {
    	
    	$("#content").css("height", window.innerHeight - $("#header").height()-220);

	var  nodesMap = {
	        <c:forEach items="${nodes}" var="node" varStatus="loopNodes">
	        "${node.id}": {
	            "id" : ${node.id},
	            "label" : "${node.text}",
	            "type" : "${node.type}",
	            "sentences" : [
	            	<c:forEach items="${node.sentences}" var="sentence" varStatus="loopSentences">
	            	"${sentence}"${!loopSentences.last ? ',' : ''}
	    	    	</c:forEach>
	            	],
	            "titles" : [
	            	<c:forEach items="${node.titles}" var="title" varStatus="loopTitles">
	            	"${title}"${!loopTitles.last ? ',' : ''}
	    	    	</c:forEach>
	            	],
	            "urls" : [
	            	<c:forEach items="${node.urls}" var="url" varStatus="loopUrls">
	            	"${url}"${!loopUrls.last ? ',' : ''}
	    	    	</c:forEach>
	            	],
	            "count": "${node.count}",
	            "group" : ${node.group}
	        }${!loopNodes.last ? ',' : ''}
	    	</c:forEach>
	};

	var nodesDataSet = new vis.DataSet([]);

	var colorsPerGroup = {0:'#377bb5', 1:'#5fb760', 2:'#60c0dc', 4:'#eeac57', 8:'#d75452'};
	
	for (var nodeId in nodesMap){
		var node = nodesMap[nodeId];
		nodesDataSet.add({
			"id": node["id"],
			"label": node["label"]
		});
		nodesDataSet.update([{id: node["id"], color:{
			background:colorsPerGroup[node["group"]],
			border: 'black',
			highlight: {
				border: 'grey',
				background: 'black'
			}
		}}]);
	}
	
	
	//for (var i=0 ; i<nodesMap.length ; i++){
/* 
 	// Add nodes with group 0 on startup
	for (var nodeId in nodesMap){
		var node = nodesMap[nodeId];
		var group = node["group"];
		if (group == 0){
			nodesDataSet.add({
				"id": node["id"],
				"label": node["label"]
			});
		}
	}
*/

	var edgesMap = {
			<c:forEach items="${edges}" var="edge" varStatus="loopEdges">
			"${edge.id}": {
				"id": "${edge.id}",
				"from": ${edge.node1},
				"to": ${edge.node2},
				"label": "${edge.title}",
				"sentences" : [
	            	<c:forEach items="${edge.sentences}" var="sentence" varStatus="loopSentences">
	            	"${sentence}"${!loopSentences.last ? ',' : ''}
	    	    	</c:forEach>
	            	],
	            "pageTitles" : [
	            	<c:forEach items="${edge.pageTitles}" var="pageTitle" varStatus="loopPageTitle">
	            	"${pageTitle}"${!loopPageTitle.last ? ',' : ''}
	    	    	</c:forEach>
	            	],
	            "urls" : [
	            	<c:forEach items="${edge.urls}" var="url" varStatus="loopUrls">
	            	"${url}"${!loopUrls.last ? ',' : ''}
	    	    	</c:forEach>
	            	],
	            "relationsMode" : "${edge.relationsMode}"
			}${!loopEdges.last ? ',' : ''}
		    </c:forEach>
	};

	var edgesDataSet = new vis.DataSet([]);

	for (var edgeId in edgesMap){
		var edge = edgesMap[edgeId];
		edgesDataSet.add({
			"id": edge["id"],
			"label": edge["label"],
			"from": edge["from"],
			"to": edge["to"]
		});
	}

	// create a network
	var container = document.getElementById('networkDiv');
	var data = {
	    nodes : nodesDataSet,
	    edges : edgesDataSet
	};
	var options = {
	    nodes : {
    	color: {
			border: 'black',
			highlight: {
				border: 'grey',
				background: 'black'
			}
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
    	color : {
		    color : 'darkgray',
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

	var editMode = false;

	var network = new vis.Network(container, data, options);

	var doubleClickTime = 0;
	var threshold = 200;

	network.on('selectNode', function onSelectNode(params) {
	    // handles double click (maybe we want to add a feature with double click later)
	    var t0 = new Date();
	    if (t0 - doubleClickTime > threshold) {
		setTimeout(function () {
		    if (t0 - doubleClickTime > threshold) {
		        doOnSelectNode(params);
		    }
		},threshold);
	    }
	});

        var closeBtn = "<button id=\"closeEditBtn\" type=\"button\" class=\"btn btn-danger\">x</button>";
        var delNodBtn = "<button id=\"deleteNodeBtn\" type=\"button\" class=\"btn btn-danger\">Delete a node</button>";
        var delEdgBtn = "<button id=\"deleteEdgeBtn\" type=\"button\" class=\"btn btn-danger\">Delete an edge</button>";
        var edNodBtn = "<button id=\"editNodeBtn\" type=\"button\" class=\"btn btn-primary\">Modify a node</button>";
        var edEdgBtn = "<button id=\"editEdgeBtn\" type=\"button\" class=\"btn btn-primary\">Modify an edge</button>";
        var adNodBtn = "<button id=\"addNodeBtn\" type=\"button\" class=\"btn btn-success\">Add a node</button>";
        var adEdgBtn = "<button id=\"addEdgeBtn\" type=\"button\" class=\"btn btn-success\">Add an edge</button>";
        var backBtn = "<button id=\"backEditBtn\" type=\"button\" class=\"btn btn-danger\">Back</button>";
        var editDivInnerHtml = closeBtn +
            "<div class=\"btn-group-vertical\">" +
            delNodBtn +
            delEdgBtn +
            edNodBtn +
            edEdgBtn +
            adNodBtn +
            adEdgBtn +
            "</div><div id=\"editNodeEdgeDiv\"></div>";

    var nodesInfoStyle = "<button type=\"button\" id=\"nodesInfoDisabledBtn\" class=\"btn btn-info disabled\">Node</button>";
	var nodesUrlsInfoStyleBtn = "<button type=\"button\" id=\"nodesURLsBtn\" class=\"btn btn-info\">URLs</button>";
	var nodesSentencesInfoStyleBtn = "<button type=\"button\" id=\"nodesSentencesBtn\" class=\"btn btn-info\">Sentences</button>";
	var nodeToDisplayInfos = null;
	function doOnSelectNode(params){
	    var nodesIds = params.nodes;
	    if (nodesIds.length < 1) return;
	    if (network.isCluster(params.nodes[0])) {
	    } else {
		var nodeId = nodesIds[0];
		if (!editMode){
			nodeToDisplayInfos = nodesMap[nodeId];
			var nodeText = nodesInfoStyle + "<br><pre>"+nodesMap[nodeId]["label"];
	    	if (nodesMap[nodeId]["type"] != "" && nodesMap[nodeId]["type"].length > 2) nodeText = nodeText + " ("+nodesMap[nodeId]["type"]+")";
	    	nodeText = nodeText + "</pre>";
	    	document.getElementById("headerInfosDiv").innerHTML = nodeText; 
	    	
	    	document.getElementById("infosNodeEdgeDiv").innerHTML = "<br>"+nodesUrlsInfoStyleBtn+"<br><div id=\"nodesURLsDiv\"></div>";
	    	document.getElementById("infosNodeEdgeDiv").innerHTML = document.getElementById("infosNodeEdgeDiv").innerHTML +"<br>"+nodesSentencesInfoStyleBtn+"<br><div id=\"nodesSentencesDiv\"></div>";
	    	document.getElementById("nodesURLsBtn").addEventListener("click", displayNodesURLs);
	    	document.getElementById("nodesSentencesBtn").addEventListener("click", displayNodesSentences);
	    	displayInfos();

		} else if (deleteNodeMode){
		    //document.getElementById("editNodeEdgeDiv").innerHTML = "Node: "+nodesMap[nodeId]["label"]+" ("+nodesMap[nodeId]["type"]+")";
		    //displayEdit();
		    var choice = confirm("Delete node "+JSON.stringify(nodesMap[nodeId])+" ?");
		    if (choice == true) {
			deleteNodeAndConnectedEdges(nodeId);
			deleteNodeMode = false;
			backToEdit();
		    }
		    else {
            document.getElementById("editDiv").innerHTML = editDivInnerHtml;
			document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
			document.getElementById("deleteNodeBtn").addEventListener("click", deleteNodeOption);
			document.getElementById("deleteEdgeBtn").addEventListener("click", deleteEdgeOption);
			document.getElementById("editNodeBtn").addEventListener("click", editNodeOption);
			document.getElementById("editEdgeBtn").addEventListener("click", editEdgeOption);
			document.getElementById("addNodeBtn").addEventListener("click", addNodeOption);
			document.getElementById("addEdgeBtn").addEventListener("click", addEdgeOption);
		    }
		} else if (addEdgeMode){
		    if (addEdgeNodeOneMode) {
			var c = confirm("node 1 : "+JSON.stringify(nodesMap[nodeId])+" ?");
			if (c) {
			    edgeToAddIdNodeOne = nodeId;
			    addEdgeNodeOneMode = false;
			    addEdgeNodeTwoMode = true;
			    document.getElementById("editDiv").innerHTML = document.getElementById("editDiv").innerHTML+"<br>Node 1: "+JSON.stringify(nodesMap[nodeId]);
			    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
			    document.getElementById("backEditBtn").addEventListener("click", backToEdit);
			}

		    } else if (addEdgeNodeTwoMode){
			var c = confirm("node 2 : "+JSON.stringify(nodesMap[nodeId])+" ?");
			if (c){
			    edgeToAddIdNodeTwo = nodeId;
			    addEdgeNodeTwoMode = false;
			    document.getElementById("editDiv").innerHTML = document.getElementById("editDiv").innerHTML+"<br>Node 2: "+
                                JSON.stringify(nodesMap[nodeId]);
			    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
			    document.getElementById("backEditBtn").addEventListener("click", backToEdit);
			    addEdge(edgeToAddIdNodeOne, edgeToAddIdNodeTwo);

			}
		    }
		} else if (modifyNodeMode){
		    //alert("modify node "+nodeId);
		    var node = nodesMap[nodeId];
            document.getElementById("editDiv").innerHTML = closeBtn + backBtn + "<br><br>";
		    document.getElementById("editDiv").innerHTML = document.getElementById("editDiv").innerHTML+
"<form method=\"post\" onsubmit=\"setTimeout(function () { window.location.reload(); }, 10)\">\
<legend>Modify Node</legend>\
<input type=\"hidden\" name=\"action\" value=\"editNode\">\
<input type=\"hidden\" name=\"id\" value=\""+node["id"]+"\">\
<label for=\"modNodLabel\">Label</label>\
<br>\
<input type=\"text\" name=\"name\" value=\""+node["label"]+"\" id=\"modNodLabel\" class=\"form-control\">\
<br><br>\
<label for=\"modNodType\">Type</label>\
<br>\
<input type=\"text\" name=\"type\" value=\""+node["type"]+"\" id=\"modNodType\" class=\"form-control\">\
<br><br>\
<input type=\"submit\" value=\"Submit\" class=\"btn btn-info\">\
</form>";
		    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
		    document.getElementById("backEditBtn").addEventListener("click", backToEdit);
		}

	    }
	}
	
	function displayNodesSentences(){
		//alert("displayNodesSentences");
		for (var key in nodeToDisplayInfos["sentences"]){
	    	document.getElementById("nodesSentencesDiv").innerHTML = document.getElementById("nodesSentencesDiv").innerHTML +"<pre>"+nodeToDisplayInfos["sentences"][key]+"</pre>";
    	}
		document.getElementById("nodesSentencesBtn").removeEventListener('click', displayNodesSentences, false);
    	document.getElementById("nodesSentencesBtn").addEventListener("click", hideNodesSentences);
	}
	function displayNodesURLs(){
		for (var key in nodeToDisplayInfos["urls"]){
	    	document.getElementById("nodesURLsDiv").innerHTML = document.getElementById("nodesURLsDiv").innerHTML +"<pre><a href=\""+nodeToDisplayInfos["urls"][key]+"\" target=\"_blank\">"+nodeToDisplayInfos["urls"][key]+"</a></pre>";
    	}
		document.getElementById("nodesURLsBtn").removeEventListener('click', displayNodesURLs, false);
		document.getElementById("nodesURLsBtn").addEventListener("click", hideNodesURLs);
	}
	function hideNodesSentences(){
		//alert("hideNodesSentences");
		document.getElementById("nodesSentencesDiv").innerHTML = "";
		document.getElementById("nodesSentencesBtn").removeEventListener('click', hideNodesSentences, false);
    	document.getElementById("nodesSentencesBtn").addEventListener("click", displayNodesSentences);
    }
	function hideNodesURLs(){
		//alert("hideNodesURLs");
		document.getElementById("nodesURLsDiv").innerHTML = "";
		document.getElementById("nodesURLsBtn").removeEventListener('click', hideNodesURLs, false);
		document.getElementById("nodesURLsBtn").addEventListener("click", displayNodesURLs);
	}
	

	var edgesInfoStyle = "<button type=\"button\" id=\"edgesInfoDisabledBtn\" class=\"btn btn-info disabled\">Edge</button>";
	var edgesUrlsInfoStyleBtn = "<button type=\"button\" id=\"edgesURLsBtn\" class=\"btn btn-info\">URLs</button>";
	var edgesSentencesInfoStyleBtn = "<button type=\"button\" id=\"edgesSentencesBtn\" class=\"btn btn-info\">Sentences</button>";
	
	var edge = null;
	network.on("selectEdge", function(params) {
	    var edgesIds = params.edges;
	    if (edgesIds.length>0){
		var clickedEdges = edgesDataSet.get(edgesIds);
		if (clickedEdges.length>0 && clickedEdges[0] != null){
		    var edgeVis = clickedEdges[0];
		    var node1Id = edgeVis["from"];
		    var node2Id = edgeVis["to"];
		    var edgeId = edgeVis["id"];
		    if (!editMode){
		    	edge = edgesMap[edgeId];

		    	var node1Text = "<br><pre>"+nodesMap[node1Id]["label"];
		    	if (nodesMap[node1Id]["type"] != "" && nodesMap[node1Id]["type"].length > 2) node1Text = node1Text + " ("+nodesMap[node1Id]["type"]+")";
		    	node1Text = node1Text + "</pre>";
		    	
		    	var node2Text = "<pre>"+nodesMap[node2Id]["label"];
		    	if (nodesMap[node2Id]["type"] != "" && nodesMap[node2Id]["type"].length > 2) node2Text = node2Text + " ("+nodesMap[node2Id]["type"]+")";
		    	node2Text = node2Text + "</pre>";
		    	
				document.getElementById("headerInfosDiv").innerHTML = edgesInfoStyle + node1Text + node2Text;
		    	
		    	var edgesRelTypeInfoStyle = "<button type=\"button\" id=\"edgesRelTypeInfoDisabledBtn\" class=\"btn btn-default disabled\">Type of relation: \"" + edge["label"]+"\"</button>";
				if (edge.hasOwnProperty("label") && edge.label != "NO_EDGE_LABEL_DEFINED") document.getElementById("headerInfosDiv").innerHTML = document.getElementById("headerInfosDiv").innerHTML + edgesRelTypeInfoStyle +"<br>";
				document.getElementById("infosNodeEdgeDiv").innerHTML = "<br>" + edgesUrlsInfoStyleBtn+ "<br><div id=\"edgesURLsDiv\"></div>";
		    	document.getElementById("infosNodeEdgeDiv").innerHTML = document.getElementById("infosNodeEdgeDiv").innerHTML +"<br>" + edgesSentencesInfoStyleBtn + "<br><div id=\"edgesSentencesDiv\"></div>";
		    	document.getElementById("edgesURLsBtn").addEventListener("click", displayEdgesURLs);
		    	document.getElementById("edgesSentencesBtn").addEventListener("click", displayEdgesSentences);
		    	displayInfos();
		    } else if (deleteEdgeMode){
			var choice = confirm("Delete edge between "+JSON.stringify(nodesMap[edge["from"]])+" and "+JSON.stringify(nodesMap[edge["to"]])+" ?");
			if (choice == true) {
			    deleteEdge(edge["id"]);
			    deleteEdgeMode = false;
			    backToEdit();

			}
			else {
                document.getElementById("editDiv").innerHTML = editDivInnerHtml;
			    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
			    document.getElementById("deleteNodeBtn").addEventListener("click", deleteNodeOption);
			    document.getElementById("deleteEdgeBtn").addEventListener("click", deleteEdgeOption);
			    document.getElementById("editNodeBtn").addEventListener("click", editNodeOption);
			    document.getElementById("editEdgeBtn").addEventListener("click", editEdgeOption);
			    document.getElementById("addNodeBtn").addEventListener("click", addNodeOption);
			    document.getElementById("addEdgeBtn").addEventListener("click", addEdgeOption);
			}
		    } else if (modifyEdgeMode){
			alert("Modify edge between "+JSON.stringify(nodesMap[edge["from"]])+" and "+JSON.stringify(nodesMap[edge["to"]])+" with label "+edge["relName"] + " and EdgeID: " + edge["id"]);

                        document.getElementById("editDiv").innerHTML = closeBtn + backBtn + "<br><br>";
			document.getElementById("editDiv").innerHTML = document.getElementById("editDiv").innerHTML+
"<form method=\"post\" onsubmit=\"setTimeout(function () { window.location.reload(); }, 10)\">\
<legend>Modify Edge</legend>\
<input type=\"hidden\" name=\"action\" value=\"editEdge\">\
<input type=\"hidden\" name=\"id\" value=\""+edge["id"]+"\">"+nodesMap[edge["to"]]["label"]+" ---- "+nodesMap[edge["from"]]["label"]+
"<label for=\"modEdgText\">Label</label>\
<br>\
<input type=\"text\" name=\"label\" value=\""+edge["relName"]+"\" id=\"modEdgText\" class=\"form-control\">\
<br><br>\
<input type=\"submit\" value=\"Submit\" class=\"btn btn-info\">\
</form>";
			document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
			document.getElementById("backEditBtn").addEventListener("click", backToEdit);
		    }

		}
	    }
	});
	
	function displayEdgesSentences(){
		//alert("displayEdgesSentences");
		for (var key in edge["sentences"]){
	    	document.getElementById("edgesSentencesDiv").innerHTML = document.getElementById("edgesSentencesDiv").innerHTML +"<pre>"+edge["sentences"][key]+"</pre>";
    	}
		document.getElementById("edgesSentencesBtn").removeEventListener('click', displayEdgesSentences, false);
    	document.getElementById("edgesSentencesBtn").addEventListener("click", hideEdgesSentences);
	}
	function displayEdgesURLs(){
		//alert("displayEdgesURLs");
		for (var key in edge["urls"]){
	    	document.getElementById("edgesURLsDiv").innerHTML = document.getElementById("edgesURLsDiv").innerHTML +"<pre><a href=\""+edge["urls"][key]+"\" target=\"_blank\">"+edge["urls"][key]+"</a></pre>";
    	}
		document.getElementById("edgesURLsBtn").removeEventListener('click', displayEdgesURLs, false);
		document.getElementById("edgesURLsBtn").addEventListener("click", hideEdgesURLs);
	}
	function hideEdgesSentences(){
		//alert("hideEdgesSentences");
		document.getElementById("edgesSentencesDiv").innerHTML = "";
		document.getElementById("edgesSentencesBtn").removeEventListener('click', hideEdgesSentences, false);
    	document.getElementById("edgesSentencesBtn").addEventListener("click", displayEdgesSentences);
    }
	function hideEdgesURLs(){
		//alert("hideEdgesURLs");
		document.getElementById("edgesURLsDiv").innerHTML = "";
		document.getElementById("edgesURLsBtn").removeEventListener('click', hideEdgesURLs, false);
		document.getElementById("edgesURLsBtn").addEventListener("click", displayEdgesURLs);
	}

	document.getElementById("closeInfosBtn").onclick = closeInfos;
	document.getElementById("closeEditBtn").onclick = closeEdit;
	document.getElementById("editBtn").onclick = displayEdit;
	var infosMode = false;

	var deleteNodeMode = false;
	var deleteEdgeMode = false;
	var addNodeMode = false;
	var addEdgeMode = false;
	var modifyNodeMode = false;
	var modifyEdgeMode = false;
	var addEdgeNodeOneMode = false;
	var addEdgeNodeTwoMode = false;
	var edgeToAddIdNodeOne = null;
	var edgeToAddIdNodeTwo = null;

	function displayInfos(){
	    if (editMode) {
		closeInfos();
		editMode = false;
	    }
	    if (evalMode){
		closeEval();
		evalMode = false;
	    }
	    infosMode = true;
	    var informationDivEl = document.getElementById('informationDiv');
	    informationDivEl.style.display = "";
	    var networkDivEl  = document.getElementById('networkDiv');
	    networkDivEl.style.width = '70%';
	    document.getElementById('infosNodeEdgeDiv').style.overflow = "auto";
	}

	function closeInfos(){
	    infosMode = false;
	    var informationDivEl = document.getElementById('informationDiv');
	    informationDivEl.style.display = "none";
	    var networkDivEl  = document.getElementById('networkDiv');
	    networkDivEl.style.width = '100%';
	    document.getElementById("infosNodeEdgeDiv").innerHTML = "";
	}

	function displayEdit(){
	    if (infosMode) {
		closeInfos();
		infosMode = false;
	    }
	    if (evalMode){
		closeEval();
		evalMode = false;
	    }
	    editMode = true;
	    var editDivEl = document.getElementById('editDiv');
	    editDivEl.style.display = "";
	    var networkDivEl  = document.getElementById('networkDiv');
	    networkDivEl.style.width = '70%';
	    
	    deleteNodeMode = false;
		deleteEdgeMode = false;
		addNodeMode = false;
		addEdgeMode = false;
		modifyNodeMode = false;
		modifyEdgeMode = false;
		addEdgeNodeOneMode = false;
		addEdgeNodeTwoMode = false;
	}

	function closeEdit(){
	    editMode = false;
	    var editDivEl = document.getElementById('editDiv');
	    editDivEl.style.display = "none";
	    var networkDivEl  = document.getElementById('networkDiv');
	    networkDivEl.style.width = '100%';
	    
	    deleteNodeMode = false;
		deleteEdgeMode = false;
		addNodeMode = false;
		addEdgeMode = false;
		modifyNodeMode = false;
		modifyEdgeMode = false;
		addEdgeNodeOneMode = false;
		addEdgeNodeTwoMode = false;
	}

	document.getElementById("backEditBtn").onclick = backToEdit;

	function backToEdit(){
        document.getElementById("editDiv").innerHTML = editDivInnerHtml;
	    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
	    document.getElementById("deleteNodeBtn").addEventListener("click", deleteNodeOption);
	    document.getElementById("deleteEdgeBtn").addEventListener("click", deleteEdgeOption);
	    document.getElementById("editNodeBtn").addEventListener("click", editNodeOption);
	    document.getElementById("editEdgeBtn").addEventListener("click", editEdgeOption);
	    document.getElementById("addNodeBtn").addEventListener("click", addNodeOption);
	    document.getElementById("addEdgeBtn").addEventListener("click", addEdgeOption);

	    deleteNodeMode = false;
		deleteEdgeMode = false;
		addNodeMode = false;
		addEdgeMode = false;
		modifyNodeMode = false;
		modifyEdgeMode = false;
		addEdgeNodeOneMode = false;
		addEdgeNodeTwoMode = false;
	}

	function addNodeOption(){
            document.getElementById("editDiv").innerHTML = closeBtn + backBtn + "<br>";
	    document.getElementById("editDiv").innerHTML = document.getElementById("editDiv").innerHTML+ "<br>";
            var editForm =
"<form method=\"post\" onsubmit=\"setTimeout(function () { window.location.reload(); }, 10)\">\
<legend>Node to add:</legend>\
<input type=\"hidden\" name=\"action\" value=\"addNode\">\
<label for=\"nodeNameInput\">Name</label>\
<input type=\"text\" name=\"name\" id=\"nodeNameInput\" class=\"form-control\" placeholder=\"Spider-Man\" class=\"form-control\">\
<br><br>\
<fieldset class=\"form-group\">\
<label for=\"addNodeRadioGroup\">Type</label><br>\
<div class=\"btn-group-vertical\" data-toggle=\"buttons\" id=\"addNodeRadioGroup\">\
<label class=\"btn btn-primary\">\
<input type=\"radio\" name=\"type\" value=\"person\" checked> Person\
</label>\
<label class=\"btn btn-primary\">\
<input type=\"radio\" name=\"type\" value=\"location\"> Location\
</label>\
<label class=\"btn btn-primary\">\
<input type=\"radio\" name=\"type\" value=\"organization\"> Organization\
</label>\
</div>\
</fieldset>\
<br><br>\
<input type=\"submit\" value=\"Submit\" class=\"btn btn-info\">\
</form>"
	    document.getElementById("editDiv").innerHTML = document.getElementById("editDiv").innerHTML+editForm;
	    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
	    document.getElementById("backEditBtn").addEventListener("click", backToEdit);

	}

	function addEdgeOption(){
            document.getElementById("editDiv").innerHTML = closeBtn + backBtn + "<br>";
	    document.getElementById("editDiv").innerHTML = document.getElementById("editDiv").innerHTML+
                "<br><legend>Select nodes to connect</legend>";
	    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
	    document.getElementById("backEditBtn").addEventListener("click", backToEdit);
	    deleteEdgeMode = false;
	    deleteNodeMode = false;
	    addNodeMode = false;
	    addEdgeMode = true;
	    addEdgeNodeOneMode = true;
	}

	document.getElementById("addNodeBtn").onclick = addNodeOption;
	document.getElementById("deleteNodeBtn").onclick = deleteNodeOption;

	document.getElementById("editNodeBtn").onclick = editNodeOption;
	function editNodeOption(){
            document.getElementById("editDiv").innerHTML = closeBtn + backBtn +
                "<br><br><legend>Select node to modify</legend>";
	    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
	    document.getElementById("backEditBtn").addEventListener("click", backToEdit);
	    deleteNodeMode = false;
	    deleteEdgeMode = false;
	    modifyNodeMode = true;
	    modifyEdgeMode = false;
	    addNodeMode = false;
	    addEdgeMode = false;
	}

	document.getElementById("editEdgeBtn").onclick = editEdgeOption;
	function editEdgeOption(){
            document.getElementById("editDiv").innerHTML = closeBtn + backBtn +
                "<br><br><legend>Select the edge to modify</legend>";
	    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
	    document.getElementById("backEditBtn").addEventListener("click", backToEdit);
	    deleteNodeMode = false;
	    deleteEdgeMode = false;
	    modifyNodeMode = false;
	    modifyEdgeMode = true;
	    addNodeMode = false;
	    addEdgeMode = false;
	}

	function deleteNodeOption(){
            document.getElementById("editDiv").innerHTML = closeBtn + backBtn +
                "<br><br><legend>Select node to delete</legend>";
	    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
	    document.getElementById("backEditBtn").addEventListener("click", backToEdit);
	    deleteNodeMode = true;
	    deleteEdgeMode = false;
	    addNodeMode = false;
	    addEdgeMode = false;
	}

	document.getElementById("addEdgeBtn").onclick = addEdgeOption;
	document.getElementById("deleteEdgeBtn").onclick = deleteEdgeOption;

	function deleteEdgeOption(){
            document.getElementById("editDiv").innerHTML = closeBtn + backBtn +
                "<br><br><legend>Select edge to delete</legend>";
	    document.getElementById("closeEditBtn").addEventListener("click", closeEdit);
	    document.getElementById("backEditBtn").addEventListener("click", backToEdit);
	    deleteEdgeMode = true;
	    deleteNodeMode = false;
	    addNodeMode = false;
	    addEdgeMode = false;
	}

	function deleteNodeAndConnectedEdges(nodeId){

	    // get and delete edges connected to this node
	    var edgeIds = network.getConnectedEdges(nodeId);
	    for (var i=0; i<edgeIds.length ; i++) {
		deleteEdge(edgeIds[i]);
	    }

	    alert("delete node "+nodeId);
	    $.post("smallTest",{action:"deleteNode",nodeId:nodeId}, function(data, status){
		//alert("Data: " + data + "\nStatus: " + status);
	    });

	    deleteNodeMode = false;
	    window.location.reload(true);
	}

	function deleteEdge(edgeId){

	    $.post("smallTest",{action:"deleteEdge",edgeId:edgeId}, function(data, status){
		//alert("Data: " + data + "\nStatus: " + status);
	    });
	    deleteEdgeMode = false;
	    window.location.reload(true);
	}

	function addEdge(nodeIdOne, nodeIdTwo){

	    $.post("smallTest",{action:"addEdge",nodeId1:nodeIdOne, nodeId2:nodeIdTwo}, function(data, status){
		//alert("Data: " + data + "\nStatus: " + status);
		addEdgeMode = false;
		backToEdit();
		window.location.reload(true);
	    });


	}


	var evalMode = false;
	var edgeToEvaluate = null;
	var correctEdge = null;
	var node1ToEvaluate = null;
	var correctNode1 = null;
	var node2ToEvaluate = null;
	var correctNode2 = null;

	document.getElementById("evalBtn").onclick = evaluate;

	/* Select a random edge and ask the user to confirm it is correct
	   In the future: more intelligent choice of edges, record choices
	*/
	function evaluate(){

			$.post("smallTest",{action:"evaluate"}, function(response){
				edgeToEvaluate = response["edge"];
				node1ToEvaluate = response["node1"];
				node2ToEvaluate = response["node2"];
				showNewEvalQuestionNode(node1ToEvaluate, node2ToEvaluate);
				displayEval();
			});
		}

		function showNewEvalQuestionNode(node1, node2){

	        //alert("edge between "+JSON.stringify(node1)+" and "+JSON.stringify(node2));
			document.getElementById("evalDiv").innerHTML = "<button id=\"finishEvalBtn\" type=\"button\" class=\"btn btn-primary\">Finish</button><button id=\"closeEvalBtn\" type=\"button\" class=\"btn btn-danger\">x</button>";
			document.getElementById("evalDiv").innerHTML = document.getElementById("evalDiv").innerHTML + "<br>Does this node seem correct?<br>";
			document.getElementById("evalDiv").innerHTML = document.getElementById("evalDiv").innerHTML + "Label = \""+node1["text"]+"\"<br>Type = \""+node1["type"]+"\"";
			var yesNo = "<div class=\"btn-group\"><button id=\"yesBtn\" type=\"button\" class=\"btn btn-success\">Yes</button><button id=\"noBtn\" type=\"button\" class=\"btn btn-danger\">No</button>";
            document.getElementById("evalDiv").innerHTML = document.getElementById("evalDiv").innerHTML + "<br><br>" + yesNo;
			document.getElementById("yesBtn").addEventListener("click", processAnswerYes);
			document.getElementById("noBtn").addEventListener("click", processAnswerNo);
			document.getElementById("closeEvalBtn").addEventListener("click", closeEval);
			document.getElementById("finishEvalBtn").addEventListener("click", closeEval);
			// select node to evaluate in graph
			/*
			var array = [];
			array.push(idNode1ToEvaluate);
			network.selectNodes(array);
			*/
		}

		function showNewEvalQuestionEdge(){

	        //alert("edge between "+JSON.stringify(node1)+" and "+JSON.stringify(node2));
		    document.getElementById("evalDiv").innerHTML =
"<button id=\"finishEvalBtn\" type=\"button\" class=\"btn btn-primary\">Finish</button>\
<button id=\"closeEvalBtn\" type=\"button\" class=\"btn btn-danger\">x</button>";
			document.getElementById("evalDiv").innerHTML = document.getElementById("evalDiv").innerHTML + "<br>Is this a correct edge?<br>";
			document.getElementById("evalDiv").innerHTML = document.getElementById("evalDiv").innerHTML + "\""+node1ToEvaluate["text"]+"\" ---- \""+node2ToEvaluate["text"]+"\"";
		    var yesNo =
"<div class=\"btn-group\">\
<button id=\"yesBtn\" type=\"button\" class=\"btn btn-success\">Yes</button>\
<button id=\"noBtn\" type=\"button\" class=\"btn btn-danger\">No</button>\
</div>";
            document.getElementById("evalDiv").innerHTML = document.getElementById("evalDiv").innerHTML + "<br><br>" + yesNo;
			document.getElementById("yesBtn").addEventListener("click", processAnswerYes);
			document.getElementById("noBtn").addEventListener("click", processAnswerNo);
			document.getElementById("closeEvalBtn").addEventListener("click", closeEval);
			document.getElementById("finishEvalBtn").addEventListener("click", closeEval);
			// select edge to evaluate in graph
			/*
			edgeIds = network.getConnectedEdges(idNode1ToEvaluate);
			for (var i=0 ; i<edgeIds.length ; i++){
				var edgeId = edgeIds[i];
				var edge = edgesDataSet.get(edgeId);
				if (edge["from"]==idNode2ToEvaluate || edge["to"]==idNode2ToEvaluate){
					var array = [];
					array.push(edgeId);
					network.selectEdges(array);
				}
			}
			*/
		}

		function processAnswerYes(){

			if (correctNode1 == null){
				correctNode1 = true;
				showNewEvalQuestionNode(node2ToEvaluate, node1ToEvaluate);
			} else if (correctNode2 == null){
				correctNode2 = true;
				showNewEvalQuestionEdge();
			} else if (correctEdge == null){
				correctEdge = true;
			    $.post("smallTest",{action:"evalResults",
                                                idNode1:node1ToEvaluate["id"],
                                                node1Res:correctNode1,
                                                idNode2:node2ToEvaluate["id"],
                                                node2Res:correctNode2,
                                                idEdge:edgeToEvaluate["id"],
                                                edgeRes:correctEdge},
                                   function(response, status){
					correctNode1 = null;
					correctNode2 = null;
					correctEdge = null;
					edgeToEvaluate = response["edge"];
					node1ToEvaluate = response["node1"];
					node2ToEvaluate = response["node2"];
					showNewEvalQuestionNode(node1ToEvaluate, node2ToEvaluate);
				});
			}

		}

		function processAnswerNo(){

			if (correctNode1 == null){
				correctNode1 = false;
			    $.post("smallTest",{action:"evalResults",
                                                idNode1:node1ToEvaluate["id"],
                                                node1Res:correctNode1},
                                   function(response, status){
					correctNode1 = null;
					correctNode2 = null;
					correctEdge = null;
					edgeToEvaluate = response["edge"];
					node1ToEvaluate = response["node1"];
					node2ToEvaluate = response["node2"];
					showNewEvalQuestionNode(node1ToEvaluate, node2ToEvaluate);
				});
			} else if (correctNode2 == null){
				correctNode2 = false;
			    $.post("smallTest",{action:"evalResults",
                                                idNode1:node1ToEvaluate["id"],
                                                node1Res:correctNode1,
                                                idNode2:node2ToEvaluate["id"],
                                                node2Res:correctNode2},
                                   function(response, status){
					correctNode1 = null;
					correctNode2 = null;
					correctEdge = null;
					edgeToEvaluate = response["edge"];
					node1ToEvaluate = response["node1"];
					node2ToEvaluate = response["node2"];
					showNewEvalQuestionNode(node1ToEvaluate, node2ToEvaluate);
				});
			} else if (correctEdge == null){
				correctEdge = false;
			    $.post("smallTest",{action:"evalResults",
                                                idNode1:node1ToEvaluate["id"],
                                                node1Res:correctNode1,
                                                idNode2:node2ToEvaluate["id"],
                                                node2Res:correctNode2,
                                                idEdge:edgeToEvaluate["id"],
                                                edgeRes:correctEdge},
                                   function(response, status){
					correctNode1 = null;
					correctNode2 = null;
					correctEdge = null;
					edgeToEvaluate = response["edge"];
					node1ToEvaluate = response["node1"];
					node2ToEvaluate = response["node2"];
					showNewEvalQuestionNode(node1ToEvaluate, node2ToEvaluate);
				});
			}
		}

	function displayEval(){
	    if (infosMode) {
		closeInfos();
		infosMode = false;
	    }
	    if (editMode) {
		closeEdit();
		editMode = false;
	    }
	    evalMode = true;
	    var evalDivEl = document.getElementById('evalDiv');
	    evalDivEl.style.display = "";
	    var networkDivEl  = document.getElementById('networkDiv');
	    networkDivEl.style.width = '70%';
	}

	document.getElementById("closeEvalBtn").onclick = closeEval;

	function closeEval(){
	    evalMode = false;
	    var evalDivEl = document.getElementById('evalDiv');
	    evalDivEl.style.display = "none";
	    var networkDivEl  = document.getElementById('networkDiv');
	    networkDivEl.style.width = '100%';
	}

	$(function() {
		$(".auto_submit_item").change(function() {
			
			var group0 = document.getElementById("group0").checked;
			var group1 = document.getElementById("group1").checked;
			var group2 = document.getElementById("group2").checked;
			var group4 = document.getElementById("group4").checked;
			var group8 = document.getElementById("group8").checked;
			
			nodesDataSet.clear();
			for (var nodeId in nodesMap){
				var node = nodesMap[nodeId];
				var group = node["group"];
			    if ((group == 0 && group0) ||
                                (group == 1 && group1) ||
                                (group == 2 && group2) ||
                                (group == 4 && group4) ||
                                (group == 8 && group8)){
		    		nodesDataSet.add({
						"id": node["id"],
						"label": node["label"]
					});
		    		nodesDataSet.update([{id: node["id"], color:{
						background:colorsPerGroup[node["group"]],
						border: 'black',
						highlight: {
							border: 'grey',
							background: 'black'
						}
					}}]);
				}
			}
			
			if (!physicsEnabled) switchPhysics();
		});
	});
	
	var physicsEnabled = true;
  	document.getElementById("freezeBtn").onclick = switchPhysics;
  	
	function switchPhysics(){
		if (physicsEnabled){
			network.stopSimulation();
			/*network.setOptions({
				physics: {barnesHut: {gravitationalConstant: 0,
					centralGravity: 0, springConstant: 0}}
			});*/
			network.setOptions({physics: {enabled: false}});
			document.getElementById("freezeBtn").innerText = 'Unfreeze';
			physicsEnabled = false;
		} else {
			network.setOptions({
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
					solver : 'forceAtlas2Based',
					//timestep: 0.35,
					stabilization : {
					    enabled : true,
					    iterations : 1000,
					    updateInterval : 50
					}
				    }
			});
			document.getElementById("freezeBtn").innerText = 'Freeze';
			physicsEnabled = true;
		}
	}

    }
</script>

</head>
<body onload="draw()">

	<%@ include file="header.jsp"%>
	<div class="container-fluid" style="overflow: hidden;">
	<br>
		<div id="editBtnDiv">
			<fieldset class="form-group">
    <button id="editBtn" class="btn btn-default">Edit</button>
    <button id="evalBtn" class="btn btn-danger" style="display:none;">Evaluate</button>
    <button id="freezeBtn" class="btn btn-default">Freeze</button>
				<div class="btn-group" data-toggle="buttons">
				  <label for="group0" class="btn btn-primary active">
                  <input type="checkbox" name="var_id[]" autocomplete="off" value="" id="group0" class="auto_submit_item" checked>Single Nodes
                </label>
                <label for="group1" class="btn btn-success active">
                  <input type="checkbox" name="var_id[]" autocomplete="off" value="" id="group1" class="auto_submit_item" checked>Alchemy Relations
                </label>
                <label for="group2" class="btn btn-info active">
                  <input type="checkbox" name="var_id[]" autocomplete="off" value="" id="group2" class="auto_submit_item" checked>Infoboxes
                </label>
                <label for="group4" class="btn btn-warning active">
                  <input type="checkbox" name="var_id[]" autocomplete="off" value="" id="group4" class="auto_submit_item" checked>Typed Relations
                </label>
                <label for="group8" class="btn btn-danger active">
                  <input type="checkbox" name="var_id[]" autocomplete="off" value="" id="group8" class="auto_submit_item" checked>Entity Graph
                </label>
				</div>
			</fieldset>
	</div>
	<div id="content" style="background-image: url(img/marvel_folks_grey.png);">
		<div id="networkDiv"  style="background-image: url(img/marvel_folks_grey.png);"></div>
		<div id="informationDiv" style="display: none; margin-left: 70%;">
			<button id="closeInfosBtn" class="btn btn-danger">x</button>
			<div id="headerInfosDiv"></div>
			<div id="infosNodeEdgeDiv"></div>
		</div>
		<div id="editDiv" style="display: none; margin-left: 70%;">
			<button id="closeEditBtn" class="btn btn-danger">x</button>
			<div class="btn-group-vertical">
				<button id="deleteNodeBtn" type="button" class="btn btn-danger">Delete
					a node</button>
				<button id="deleteEdgeBtn" type="button" class="btn btn-danger">Delete
					an edge</button>
				<button id="editNodeBtn" type="button" class="btn btn-primary">Modify
					a node</button>
				<button id="editEdgeBtn" type="button" class="btn btn-primary">Modify
					an edge</button>
				<button id="addNodeBtn" type="button" class="btn btn-success">Add
					a node</button>
				<button id="addEdgeBtn" type="button" class="btn btn-success">Add
					an edge</button>
			</div>
			<button id="backEditBtn" type="button" class="btn btn-danger"
				style="display: none">Back</button>
			<div id="editNodeEdgeDiv"></div>
		</div>
		<div id="evalDiv" style="display: none; margin-left: 70%;">
			<button id="closeEvalBtn" class="btn btn-danger">x</button>
		</div>
	</div>
	</div>
	<br>
	<img src="img/Marvel_Logo.png" id="marvelLogo" class="center">

	<!-- Placed at the end of the document so the pages load faster -->

	<script>
    window.jQuery
    || document
    .write('<script src="../../assets/js/vendor/jquery.min.js"><\/script>')
    </script>

</body>
</html>
