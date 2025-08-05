/**
 * Studente Controller
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

	class PageOrchestrator {
        init() {
            this.corsiTable = document.getElementById("corsi-table");
            this.corsiTableBody = document.getElementById("corsi-table-body");
            this.errorMessage = document.getElementById("error-message");
            this.errorText = document.getElementById("error-text");
        }

        refresh() {
            makeAJAXCall("GET", "VaiHomeStudente", null, (request) => {
                if (request.readyState === XMLHttpRequest.DONE) {
                    if (request.status === 200) {
                        try {
                            const data = JSON.parse(request.responseText);
                            this.renderCorsi(data);
                        } 
						catch (e) {
                            this.showError("Errore nel parsing della risposta JSON.");
                        }
                    } 
					else {
                        this.showError("Errore nella richiesta: " + request.status);
                    }
                }
            });
        }

        renderCorsi(data) {
            console.log("Dati ricevuti dalla servlet:", data);
            this.corsiTableBody.innerHTML = "";
            if (!data || !Array.isArray(data.corsi) || data.corsi.length === 0) {
                this.showError("Nessun corso disponibile.");
                return;
            }
            this.corsiTable.style.display = "table";
            this.errorMessage.style.display = "none";
            data.corsi.forEach((corso, idx) => {
                const row = document.createElement("tr");
                const corsoCell = document.createElement("td");
                corsoCell.textContent = corso.nome;
                row.appendChild(corsoCell);
                const appelliCell = document.createElement("td");
                const appelli = Array.isArray(data.appelli) ? data.appelli[idx] : [];
                if (appelli && appelli.length > 0) {
                    appelliCell.innerHTML = appelli.map((appello, appelloIdx) => {
                        // Mostra la data così come arriva
                        let formattedDate = appello.data || "";
                        return `<div>${formattedDate} <button type='button' class='vedi-voto-btn' data-corso='${corso.ID}' data-appello='${appelloIdx}'>Vedi Voto</button></div>`;
                    }).join("");
                } else {
                    appelliCell.textContent = "Nessun appello";
                }
                row.appendChild(appelliCell);
                this.corsiTableBody.appendChild(row);
            });
        }

        showError(message) {
            this.errorText.textContent = message;
            this.errorMessage.style.display = "block";
            this.corsiTable.style.display = "none";
        }
	}

	let pageOrchestator = new PageOrchestrator();
	
	window.addEventListener("load", 
							() => {
									pageOrchestator.init();
									pageOrchestator.refresh();
    		                      },
							 false);