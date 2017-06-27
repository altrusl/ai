function showComments(link) { 
	var cont = document.getElementById("comments-body");  
	// var loading = document.getElementById("comments-loading");  
	var comm_link = document.getElementById("comments-link");  
	comm_link.innerHTML = "Загрузка комментариев, подождите...";  

	var http = createRequestObject();  
	if( http ){  
		http.open("get", link);  
		http.onreadystatechange = function (){  
			if(http.readyState == 4) {  
				cont.innerHTML = http.responseText;  
				comm_link.innerHTML = "Обновить комментарии";
			}  
		};
		http.send(null);      
	} else {  
		document.location = link;  
	}	
}  
function createRequestObject() {  
	try { 
		return new XMLHttpRequest() 
	} catch(e) {  
		try { 
			return new ActiveXObject("Msxml2.XMLHTTP") 
		} catch(e) {  
			try { 
				return new ActiveXObject("Microsoft.XMLHTTP") 
			} catch(e) { 
				return null; 
			}
		}  
	}  
}  