/**
 * Studente Controller
 */
{
	// AJAX Call to the server
	function makeAJAXCall(method, url, data, callBack, reset = true) {
	    var request = new XMLHttpRequest();
	    request.onreadystatechange = function() {
	      callBack(request)
	    };
	    request.open(method, url);
		if (data) {
			// Se il parametro 'data' è un elemento form, crea un FormData da esso.
			if (data instanceof HTMLFormElement) {
				request.send(new FormData(data));
			// Se il parametro 'data' è già un oggetto FormData, invialo direttamente.
			} 
			else if (data instanceof FormData) {
				request.send(data);
			// Se il parametro 'data' è una stringa, invialo con il corretto Content-Type.
			}
	    	else if (typeof data === "string") {
	        	request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	        	request.send(data);
	    	} 
		}
		else if (data == null) {
	        request.send();
	    } 
		else {
	        request.send(data);
	    }
		if (data !== null && reset === true) {
			data.reset();
		}
	}
	
	// Converte la data in formato YYYY-MM-DD per la servlet
	function dataConverter(dataOriginale) {
	    // Esempio: "lug 20, 2025" -> "2025-07-20"
	    const mesi = {
	        "gen": "01", "feb": "02", "mar": "03", "apr": "04", "mag": "05", "giu": "06",
	        "lug": "07", "ago": "08", "set": "09", "ott": "10", "nov": "11", "dic": "12"
	    };
		// Regular expression per estrarre mese, giorno e anno
	    const regex = /^(\w{3}) (\d{1,2}), (\d{4})$/;
	    const match = dataOriginale.match(regex);
	    if (match) {
	        const mese = mesi[match[1]];
	        const giorno = match[2].padStart(2, '0');
	        const anno = match[3];
	        return `${anno}-${mese}-${giorno}`;
	    }
		else{
			return dataOriginale; // Se non corrisponde al formato, restituisce la data originale
		}
	}
	
	

	class PageOrchestrator {
        init() {
            this.error = new Errore();
            this.corsi_appelli = new Corsi_Appelli(this.error);
            this.valutazione = new Valutazione(this.error);
        }

        refresh() {
			this.error.resetError();
            this.corsi_appelli.show(this.valutazione);
			document.getElementById("logout-btn")
			.addEventListener("click", () => {
				makeAJAXCall("GET", "Logout", null, (request) => {
                	if (request.readyState === XMLHttpRequest.DONE) {
                    	if (request.status === 200) {
                            window.location.href = "Login.html"; // Reindirizza alla pagina di login
                        } 
						else {
                            this.error.showError("Errore durante il logout: " + request.status);
                        }
                    }
                });
            });
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
		constructor(errorHandler) {
            this.corsi_appelliTable = document.getElementById("corsi_appelli-table");
            this.corsi_appelliTableBody = document.getElementById("corsi_appelli-table-body");
			this.corsiList = null;
            this.appelliList = null;
            this.error = errorHandler;
        }
		
		// Ottieni i dati e renderizza la tabella
        show(valutazioneInstance) {
			let self = this;
            makeAJAXCall("GET", "VaiHomeStudente", null, (request) => {
                if (request.readyState === XMLHttpRequest.DONE) {
                    if (request.status === 200) {
                        try {
                            const data = JSON.parse(request.responseText);
							console.log("Dati ricevuti dalla servlet:", data);
                            // Parsing e salvataggio
                           	self.corsiList = Array.isArray(data.corsi) ? data.corsi : [];
                            self.appelliList = Array.isArray(data.appelli) ? data.appelli : [];
							self.renderData(valutazioneInstance);
                        } 
                        catch (e) {
							self.error.showError("Errore nel parsing della risposta JSON.");
                        }
                    } 
                    else {
						self.error.showError("Errore nella richiesta: " + request.status);
                    }
                }
            });
        }
		
	    // Renderizza i dati nella pagina html
        renderData(valutazioneInstance) {
			// Resetta eventuali errori precedenti
			this.error.resetError();
            // Pulisci la tabella
            this.corsi_appelliTableBody.innerHTML = "";
            // Mostra la tabella solo se ci sono corsi
            if (!this.corsiList || this.corsiList.length === 0) {
                this.corsi_appelliTable.style.display = "none";
				this.error.showError("Nessun corso disponibile.");
				return;
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
                    appelli.forEach((appello) => {
                        const div = document.createElement("div");
                        div.textContent = appello.data || "";
                        const btn = document.createElement("button");
                        btn.type = "button";
						btn.ID = "vedi-voto-btn";
                        btn.className = "btn-primary";
                        btn.setAttribute("corsoID", corso.ID);
                        btn.setAttribute("dataAppello", appello.data);
                        btn.textContent = "Vedi Voto";
						// Event delegation per il bottone Vedi Voto
						btn.addEventListener("click", () => {
                            valutazioneInstance.show(corso.ID, dataConverter(appello.data));
                        })
                        div.appendChild(btn);
                        appelliCell.appendChild(div);
                    });
                } 
				else {
                    appelliCell.textContent = "Nessun appello a cui sei iscritto";
                }
                row.appendChild(appelliCell);
                this.corsi_appelliTableBody.appendChild(row);
            });
        }
		
    }
	
	class Valutazione {
        constructor(errorHandler) {
            this.error = errorHandler;
            this.valutazioneBox = document.getElementById("valutazione-box");
            this.valutazione = null;
            this.studInfo = null;
            this.corso = null;
            this.dataAppello = null;
            this.rifiutabile = false;
            this.draggedVoto = null;
        }

        show(corsoID, dataAppello) {
            let self = this;
            const params = new URLSearchParams({ corsoID, dataAppello });
            makeAJAXCall("GET", "VediVoto?" + params.toString(), null, (request) => {
                if (request.readyState === XMLHttpRequest.DONE) {
                    if (request.status === 200) {
                        try {
                            const data = JSON.parse(request.responseText);
							console.log("Dati ricevuti dalla servlet:", data);
							// Parsing e salvataggio
							self.valutazione = data.valutazione || null;
							self.studInfo = data.studInfo || null;
							self.corso = data.corso || null;
							self.dataAppello = data.dataAppello || null;
                            self.rifiutabile = data.rifiutabile ? true : false;
                            self.renderData();
                        } 
						catch (e) {
                            self.error.showError("Errore nel parsing della risposta JSON.");
                        }
                    } 
					else {
                        self.error.showError("Errore nella richiesta: " + request.status);
                    }
                }
            });
        }

        renderData() {
			// Resetta eventuali errori precedenti
			this.error.resetError();
			// Pulisci il box della valutazione
			this.valutazioneBox.innerHTML = "";
            if (!this.valutazione || !this.studInfo || !this.corso || !this.dataAppello) {
                this.error.showError("Dati di valutazione non disponibili.");
                return;
            }
            this.valutazioneBox.style.display = "block";

            // Bottone Chiudi
            const closeBtn = document.createElement("button");
            closeBtn.type = "button";
            closeBtn.className = "btn-secondary";
            closeBtn.textContent = "Chiudi";
            closeBtn.style.float = "left";
            closeBtn.style.marginBottom = "1rem";
            closeBtn.addEventListener("click", () => {
                this.hide();
                document.querySelector(".container").classList.remove("show-valutazione");
            });
            this.valutazioneBox.appendChild(closeBtn);

            // Tabella Studente 
            const studSection = document.createElement("div");
            const studTitle = document.createElement("h3");
            studTitle.textContent = "Dati Studente";
            studSection.appendChild(studTitle);
            const studTable = document.createElement("table");
            studTable.className = "table";
            [
                ["Matricola", this.studInfo.matricola || ""],
                ["Cognome", this.studInfo.cognome || ""],
                ["Nome", this.studInfo.nome || ""],
                ["Email", this.studInfo.email || ""],
                ["Corso di Laurea", this.studInfo.corsoLaurea || ""]
            ].forEach(([label, value]) => {
                const row = document.createElement("tr");
                const th = document.createElement("th");
                th.textContent = label;
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(th);
                row.appendChild(td);
                studTable.appendChild(row);
            });
            studSection.appendChild(studTable);

            // Tabella Corso 
            const corsoSection = document.createElement("div");
            const corsoTitle = document.createElement("h3");
            corsoTitle.textContent = "Corso";
            corsoSection.appendChild(corsoTitle);
            const corsoTable = document.createElement("table");
            corsoTable.className = "table";
            [
                ["Nome", this.corso.nome || ""],
                ["CFU", this.corso.cfu || ""],
                ["Data Appello", this.dataAppello || ""]
            ].forEach(([label, value]) => {
                const row = document.createElement("tr");
                const th = document.createElement("th");
                th.textContent = label;
                const td = document.createElement("td");
                td.textContent = value;
                row.appendChild(th);
                row.appendChild(td);
                corsoTable.appendChild(row);
            });
            corsoSection.appendChild(corsoTable);

            // Tabella Esito
            const esitoSection = document.createElement("div");
            const esitoTitle = document.createElement("h3");
            esitoTitle.textContent = "Esito";
            esitoSection.appendChild(esitoTitle);
            const esitoTable = document.createElement("table");
            esitoTable.className = "table";
            const thead = document.createElement("thead");
            const headRow = document.createElement("tr");
            ["Voto Assegnato", "Stato Valutazione"].forEach(label => {
                const th = document.createElement("th");
                th.textContent = label;
                headRow.appendChild(th);
            });
            thead.appendChild(headRow);
            esitoTable.appendChild(thead);
            const tbody = document.createElement("tbody");
            const bodyRow = document.createElement("tr");
            // Voto cell
            const voto = this.valutazione.voto ? this.valutazione.voto : "-";
            const votoTd = document.createElement("td");
            votoTd.textContent = voto;
            // Drag & Drop solo se rifiutabile
			//Qui solo Voto Draggable
            if (this.rifiutabile) {
                votoTd.setAttribute("draggable", "true");
                votoTd.style.cursor = "grab";
                votoTd.addEventListener("dragstart", (e) => {
                    e.dataTransfer.setData("text/plain", voto);
                    this.draggedVoto = votoTd;
                });
            }
            bodyRow.appendChild(votoTd);
            // Stato Valutazione cell
            const stato = this.valutazione.statoValutazione ? this.valutazione.statoValutazione : "Non disponibile";
            const statoTd = document.createElement("td");
            statoTd.textContent = stato;
            bodyRow.appendChild(statoTd);
            tbody.appendChild(bodyRow);
            esitoTable.appendChild(tbody);
            esitoSection.appendChild(esitoTable);

            // Inserisci tutto nel box
            this.valutazioneBox.appendChild(studSection);
            this.valutazioneBox.appendChild(corsoSection);
            this.valutazioneBox.appendChild(esitoSection);

            // Cestino in fondo al box, solo se rifiutabile
			//Qui Cestino Droppable
            if (this.rifiutabile) {
                const trashDiv = document.createElement("div");
                trashDiv.innerHTML = '<span style="font-size:2.5rem;cursor:pointer;" id="trash-icon" title="Trascina qui il voto per rifiutare">🗑️</span>';
                trashDiv.style.display = "flex";
                trashDiv.style.justifyContent = "center";
                trashDiv.style.alignItems = "center";
                trashDiv.style.margin = "2rem 0 0 0";
                trashDiv.style.height = "60px";
                trashDiv.addEventListener("dragover", (e) => {
                    e.preventDefault();
                    trashDiv.style.background = "#ffeaea";
                });
                trashDiv.addEventListener("dragleave", (e) => {
                    trashDiv.style.background = "";
                });
                trashDiv.addEventListener("drop", (e) => {
                    e.preventDefault();
                    trashDiv.style.background = "";
                    this.showRifiutaPopup();
                });
                trashDiv.addEventListener("click", () => {
                    this.showRifiutaPopup();
                });
                this.valutazioneBox.appendChild(trashDiv);
            }
        }

        showRifiutaPopup() {
	        // Overlay
	        const overlay = document.createElement("div");
	        overlay.style.position = "fixed";
	        overlay.style.top = "0";
	        overlay.style.left = "0";
	        overlay.style.width = "100vw";
	        overlay.style.height = "100vh";
	        overlay.style.background = "rgba(0,0,0,0.3)";
	        overlay.style.zIndex = "9999";
	        overlay.id = "rifiuta-overlay";
	        // Popup
	        const popup = document.createElement("div");
	        popup.style.position = "fixed";
	        popup.style.top = "50%";
	        popup.style.left = "50%";
	        popup.style.transform = "translate(-50%, -50%)";
	        popup.style.background = "#fff";
	        popup.style.padding = "2rem";
	        popup.style.borderRadius = "10px";
	        popup.style.boxShadow = "0 2px 8px rgba(192,57,43,0.15)";
	        popup.style.textAlign = "center";
	        popup.innerHTML = `<h3>Conferma rifiuto voto</h3><p>Sei sicuro di voler rifiutare il voto?</p>`;
	        // Bottone Cancella
	        const btnCancel = document.createElement("button");
	        btnCancel.className = "btn-secondary";
	        btnCancel.textContent = "Cancella";
	        btnCancel.style.marginRight = "1rem";
	        btnCancel.onclick = () => {
	            document.body.removeChild(overlay);
	        };
			// Bottone Conferma
	        const btnConfirm = document.createElement("button");
	        btnConfirm.className = "btn-warning";
	        btnConfirm.textContent = "Conferma";
	        btnConfirm.onclick = () => {
	            document.body.removeChild(overlay);
	            this.rifiutaVoto();
	        };
			
	        popup.appendChild(btnCancel);
	        popup.appendChild(btnConfirm);
	        overlay.appendChild(popup);
	        document.body.appendChild(overlay);
	    }

    	rifiutaVoto() {
		console.log("Rifiuto voto per il corso:", this.corso.ID, "data appello:", this.dataAppello);
		
		const formData = new FormData();
		formData.append("corsoID", this.corso.ID);
		formData.append("dataAppello", this.dataAppello);
        
        makeAJAXCall("POST", "RifiutaVoto", formData, (request) => {
            if (request.readyState === XMLHttpRequest.DONE) {
                if (request.status === 200) {
                    this.error.showError("Voto rifiutato con successo.");
                    this.hide();
                } else {
                    this.error.showError(request.responseText || "Errore nel rifiuto del voto.");
                }
            }
        }, false);
    }

    hide() {
        this.valutazioneBox.style.display = "none";
        this.valutazioneBox.innerHTML = "";
    }
}
	
	
	/***** Page Controller  *****/
	let pageOrchestator = new PageOrchestrator();
	
	window.addEventListener("load",() => {pageOrchestator.init(); pageOrchestator.refresh();}, false);
							 
							 
};