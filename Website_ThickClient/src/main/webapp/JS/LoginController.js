/**
 * Login Controller
 */

	function makeAJAXCall( method, url, formElement, callBack ) {
	    var request = new XMLHttpRequest(); // visible by closure
	    request.onreadystatechange = function() {
	      callBack(request)
	    }; // closure
	    request.open(method, url);
	    if (formElement == null) {
	      request.send();
	    } 
		else {
	      request.send(new FormData(formElement));
	    }
	}
	
	
	// Immediately Invoked Function Expression (IIFE)
	(function(){ 
	  document.getElementById("login-button").addEventListener('click', 
		(e) => {
			e.preventDefault(); // Impedisce il submit del form e il caricamento della pagina
		    var form = e.target.closest("form");
		    if (form.checkValidity()) {
		    	makeAJAXCall("POST", "CheckLogin", form, reqCallBack);
		    } 
			else {
		    	 form.reportValidity();
		    }
	    }
	  );
	})();
	
	
	
	function reqCallBack(x) {
	  	if (x.readyState == XMLHttpRequest.DONE) {
	    	var errorDiv = document.getElementById("error-message");
	    	var errorText = document.getElementById("error-text");
	    	switch (x.status) {
	    		case 200: // ok
	        		try {
	          			var response = JSON.parse(x.responseText);
	          			var user = response.user;
	          			if (user.role === "Docente") {
	            			window.location.href = "DocenteHome.html";
	          			} 
						else if (user.role === "Studente") {
	            			window.location.href = "StudenteHome.html";
	          			} 
						else {
	            			errorText.textContent = "Ruolo non riconosciuto.";
	            			errorDiv.style.display = "block";
	            			return;
	          		    }
	          			form.reset();
	          			errorDiv.style.display = "none";
	        		} 
					catch (err) {
	          			errorText.textContent = "Risposta non valida dal server.";
	          			errorDiv.style.display = "block";
	        		}
	        		break;
	      		case 400:
	      		case 401:
	      		case 500:
	        		// Mostra il messaggio di errore restituito dal backend
	        		errorText.textContent = x.responseText;
	        		errorDiv.style.display = "block";
	        		break;
	    	}
	  	}
	}