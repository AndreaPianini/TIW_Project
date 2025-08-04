/**
 * Login Controller
 */

	function makeCall( method, url, formElement, cback, reset = true ) {
	    var req = new XMLHttpRequest(); // visible by closure
	    req.onreadystatechange = function() {
	      cback(req)
	    }; // closure
	    req.open(method, url);
	    if (formElement == null) {
	      req.send();
	    } 
		else {
	      req.send(new FormData(formElement));
	    }
	    if (formElement !== null && reset === true) {
	      formElement.reset();
	    }
	}
	
	
	(function(){ // avoid variables ending up in the global scope

	  document.getElementById("loginbutton").addEventListener('click', (e) => {
	    var form = e.target.closest("form");
	    if (form.checkValidity()) {
	    	makeCall("POST", 'CheckLogin', e.target.closest("form"),
	        function(x) {
		          if (x.readyState == XMLHttpRequest.DONE) {
		            var message = x.responseText;
		            switch (x.status) {
		              case 200:
		            	sessionStorage.setItem('username', message);
		                window.location.href = "HomeCS.html";
		                break;
		              case 400: // bad request
		                document.getElementById("errormessage").textContent = message;
		                break;
		              case 401: // unauthorized
		                  document.getElementById("errormessage").textContent = message;
		                  break;
		              case 500: // server error
		            	document.getElementById("errormessage").textContent = message;
		                break;
		            }
		          }
		        }
	       );
	    } 
		else {
	    	 form.reportValidity();
	    }
	  });

	})();