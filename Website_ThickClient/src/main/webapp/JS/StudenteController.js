/**
 * Studente Controller
 */
{
	
	function makeAJAXCall( method, url, formElement, callBack, reset = true) {
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
		if (formElement !== null && reset === true) {
			formElement.reset();
		}
	}
	

	class PageOrchestrator {
        init() {
            this.corsi_appelli = new Corsi_Appelli();
			this.error = new Errore();
        }

        refresh() {
			this.error.resetError();
            this.corsi_appelli.show();
        }
	}
	
	class Errore{
		constructor() {
            this.errorMessage = document.getElementById("error-message");
            this.errorText = document.getElementById("error-text");
        }

        showError(message) {
            this.errorText.textContent = message;
            this.errorMessage.style.display = "block";
        }
		
		resetError() {
			this.errorText.textContent = "";
            this.errorMessage.style.display = "none";
        }
	}

	class Corsi_Appelli {
		constructor() {
            this.corsi_appelliTable = document.getElementById("corsi_appelli-table");
            this.corsi_appelliTableBody = document.getElementById("corsi_appelli-table-body");
			this.corsiList = null;
            this.appelliList = null;
        }
		
		// Ottieni i dati e renderizza la tabella
        show() {
			let self = this;
            makeAJAXCall("GET", "VaiHomeStudente", null, (request) => {
                if (request.readyState === XMLHttpRequest.DONE) {
                    if (request.status === 200) {
                        try {
                            const data = JSON.parse(request.responseText);
                            // Parsing e salvataggio
                           	self.corsiList = Array.isArray(data.corsi) ? data.corsi : [];
                            self.appelliList = Array.isArray(data.appelli) ? data.appelli : [];
							self.renderData();
                        } 
                        catch (e) {
							pageOrchestator.error
							.showError("Errore nel parsing della risposta JSON.");
                        }
                    } 
                    else {
						pageOrchestator.error
						.showError("Errore nella richiesta: " + request.status);
                    }
                }
            });
        }

        renderData() {
			console.log("Dati ricevuti dalla servlet:", data);
            // Pulisci la tabella
            this.corsi_appelliTableBody.innerHTML = "";
            // Mostra la tabella solo se ci sono corsi
            if (!this.corsiList || this.corsiList.length === 0) {
                this.corsi_appelliTable.style.display = "none";
				pageOrchestator.error
				.showError("Nessun corso disponibile.");
            }
            this.corsi_appelliTable.style.display = "table";
            // Per ogni corso
            this.corsiList.forEach((corso, idx) => {
                const row = document.createElement("tr");
                // Colonna corso
                const corsoCell = document.createElement("td");
                corsoCell.textContent = corso.nome;
                row.appendChild(corsoCell);
                // Colonna appelli
                const appelliCell = document.createElement("td");
                const appelli = Array.isArray(this.appelliList) ? this.appelliList[idx] : [];
                if (appelli && appelli.length > 0) {
                    appelliCell.innerHTML = appelli.map((appello, appelloIdx) => {
                        let formattedDate = appello.data || "";
                        return `<div>${formattedDate} <button type='button' class='vedi-voto-btn' data-corso='${corso.ID}' data-appello='${appelloIdx}'>Vedi Voto</button></div>`;
                    }).join("");
                } 
				else {
                    appelliCell.textContent = "Nessun appello";
                }
                row.appendChild(appelliCell);
                this.corsi_appelliTableBody.appendChild(row);
            });
        }

        update() {
			this.getData();
        }
    }
	
	
	/***** Page Controller  *****/
	let pageOrchestator = new PageOrchestrator();
	
	window.addEventListener("load",() => {pageOrchestator.refresh();}, false);
							 
							 
};