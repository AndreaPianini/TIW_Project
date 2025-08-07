/**
 * Docente Controller
*/

{
	
	// Funzioni di utilità (copiate dal tuo codice per consistenza)
	function makeAJAXCall(method, url, data = null, callback) {
		let request = new XMLHttpRequest();
		request.onreadystatechange = function() {
			callback(request);
		};
		request.open(method, url, true);
		if (data) {
			// Se il parametro 'data' è un elemento form, crea un FormData da esso.
			if (data instanceof HTMLFormElement) {
				request.send(new FormData(data));
			// Se il parametro 'data' è già un oggetto FormData, invialo direttamente.
			} else if (data instanceof FormData) {
				request.send(data);
			// Se il parametro 'data' è un oggetto URLSearchParams, invialo con il corretto Content-Type.
			} else if (data instanceof URLSearchParams) {
				request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
				request.send(data.toString());
			} else {
				// Gestisci altri casi (come stringhe di query se necessario)
				request.send(data);
			}
		} else {
			request.send();
		}
	}
	
	function dataConverter(dataOriginale) {
	    const mesi = {
	        "gen": "01", "feb": "02", "mar": "03", "apr": "04", "mag": "05", "giu": "06",
	        "lug": "07", "ago": "08", "set": "09", "ott": "10", "nov": "11", "dic": "12"
	    };
	    const regex = /^(\w{3}) (\d{1,2}), (\d{4})$/;
	    const match = dataOriginale.match(regex);
	    if (match) {
	        const mese = mesi[match[1]];
	        const giorno = match[2].padStart(2, '0');
	        const anno = match[3];
	        return `${anno}-${mese}-${giorno}`;
	    }
        // Se la data è già nel formato YYYY-MM-DD
        if (dataOriginale.match(/^\d{4}-\d{2}-\d{2}$/)) {
            return dataOriginale;
        }
        return dataOriginale;
	}
	
	
	
	
	
	
	
	
	
	// --- Classi per la logica della pagina ---
	
	class PageOrchestrator {
	        init() {
	            this.error = new Errore();
	            this.corsiAppelli = new CorsiAppelli(this.error);
	            this.studentiIscritti = new StudentiIscritti(this.error);
	            this.modificaVoto = new ModificaVoto(this.error);
	            this.inserimentoVotiMultipli = new InserimentoVotiMultipli(this.error);
	            this.verbali = new Verbali(this.error);
	            
	            this.setupEventListeners();
	        }

	        refresh() {
	            this.error.resetError();
	            this.corsiAppelli.show();
	            this.verbali.show();
	        }

	        setupEventListeners() {
	            const logoutBtn = document.getElementById("logout-btn");
	            logoutBtn.addEventListener("click", () => {
	                makeAJAXCall("POST", "Logout", null, (request) => {
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

	            
	            
	            const chiudiDettagliBtn = document.getElementById("nascondi-dettagli-btn");
	            chiudiDettagliBtn.addEventListener("click", () => {
	                this.verbali.hideDetails();
	            });

	            const closeModificaVotoBtn = document.getElementById("close-modifica-voto-btn");
	            closeModificaVotoBtn.addEventListener("click", () => this.modificaVoto.hide());
	            const annullaModificaBtn = document.getElementById("annulla-modifica-btn");
	            annullaModificaBtn.addEventListener("click", () => this.modificaVoto.hide());
	            
	            const apriInserimentoMultiploBtn = document.getElementById("apri-inserimento-multiplo-btn");
	            apriInserimentoMultiploBtn.addEventListener("click", () => {
	                const appello = this.studentiIscritti.currentAppello;
	                if (appello) {
	                    this.inserimentoVotiMultipli.show(appello);
	                } else {
	                    this.error.showError("Nessun appello selezionato per l'inserimento multiplo.");
	                }
	            });

	            const closeMultiploBtn = document.getElementById("close-multiplo-btn");
	            closeMultiploBtn.addEventListener("click", () => this.inserimentoVotiMultipli.hide());
	           
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

	class CorsiAppelli {
	        constructor(errorHandler) {
	            this.corsiAppelliTable = document.getElementById("corsi_appelli-table");
	            this.corsiAppelliTableBody = document.getElementById("corsi_appelli-table-body");
	            this.appelliTable = document.getElementById("appelli-table");
	            this.appelliTableBody = document.getElementById("appelli-table-body");
	            this.appelliTitle = document.getElementById("appelli-titolo");
	            this.backToCorsiBtn = document.getElementById("back-to-corsi-btn");
	            this.nomeCorsoSelezionato = document.getElementById("nome-corso-selezionato");
	            this.error = errorHandler;
	            this.corsiList = null;
	            this.appelliList = null;
	        }

	        show() {
	            let self = this;
	            this.error.resetError();
	            // Nasconde altre sezioni
	            document.getElementById("studenti-iscritti-container").style.display = "none";
	            document.getElementById("verbali-visualizzazione-container").style.display = "none";

	            makeAJAXCall("GET", "VaiHomeDocente", null, (request) => {
	                if (request.readyState === XMLHttpRequest.DONE) {
	                    if (request.status === 200) {
	                        try {
	                            const data = JSON.parse(request.responseText);
								console.log("Dati ricevuti dalla servlet:", data);
	                            self.corsiList = Array.isArray(data.corsi) ? data.corsi : [];
	                            self.appelliList = Array.isArray(data.appelli) ? data.appelli : [];
	                            self.renderData();
	                        } catch (e) {
	                            self.error.showError("Errore nel parsing dei corsi.");
	                        }
	                    } else {
	                        self.error.showError("Errore nel caricamento dei corsi: " + request.status);
	                    }
	                }
	            });
	        }
	        
	        renderData() {
				
	            this.error.resetError();
	            this.corsiAppelliTableBody.innerHTML = "";
	            
	            if (!this.corsiList || this.corsiList.length === 0) {
	                this.corsiAppelliTable.style.display = "none";
	                this.error.showError("Nessun corso disponibile.");
	                return;
	            }
	            
	            this.corsiAppelliTable.style.display = "table";

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
	                        btn.className = "btn-primary";
	                        btn.textContent = "Vedi Iscritti";
	                        btn.addEventListener("click", () => {
	                            pageOrchestrator.studentiIscritti.show(corso.ID, dataConverter(appello.data));
	                        });
	                        div.appendChild(btn);
	                        appelliCell.appendChild(div);
	                    });
	                } 
	                else {
	                    appelliCell.textContent = "Nessun appello disponibile.";
	                }
	                row.appendChild(appelliCell);
	                
	                

	                this.corsiAppelliTableBody.appendChild(row);
	            });
	        }
			
	        
	    }
	
		class StudentiIscritti {
				constructor(errorHandler) {
		            this.container = document.getElementById("studenti-iscritti-container");
		            this.titolo = document.getElementById("titolo-elenco-studenti");
		            this.tableBody = document.getElementById("studenti-iscritti-body");
		            this.apriInserimentoMultiploBtn = document.getElementById("apri-inserimento-multiplo-btn");
		            this.pubblicaVotiBtn = document.getElementById("pubblica-voti-btn");
		            this.verbalizzaBtn = document.getElementById("verbalizza-btn");
		            this.error = errorHandler;
		            this.currentAppello = null;
					this.asc = true;
					
					this.setupEventListeners();
				}
				
				setupEventListeners() {
				    // Aggiungi listener per nascondere l'elenco
				    const nascondiElencoBtn = document.getElementById("nascondi-elenco-btn");
				    nascondiElencoBtn.addEventListener("click", () => {
				        this.hide();
				    });

				    // Aggiungi listener per pubblicare i voti
				    this.pubblicaVotiBtn.addEventListener("click", () => {
				        if (this.currentAppello) {
				            this.pubblicaVoti(this.currentAppello);
				        } else {
				            this.error.showError("Nessun appello selezionato per la pubblicazione.");
				        }
				    });

				    // Aggiungi listener per verbalizzare
				    this.verbalizzaBtn.addEventListener("click", () => {
				        if (this.currentAppello) {
				            this.verbalizza(this.currentAppello);
				        } else {
				            this.error.showError("Nessun appello selezionato per la verbalizzazione.");
				        }
				    });

				    // Aggiungi listener per l'ordinamento delle colonne
				    const tableHeaders = document.querySelectorAll('#studenti-iscritti-table th[id^="sort-"]');
				    tableHeaders.forEach(header => {
				        header.addEventListener('click', () => {
				            this.sortTable(header.id);
				        });
				    });
				}

		        show(corsoID, dataAppello) {
		            let self = this;
		            this.error.resetError();
		            this.currentAppello = { corsoID, dataAppello };
		            this.container.style.display = "block";
		            this.titolo.textContent = `Elenco Studenti Iscritti all'appello del ${dataAppello}`;

		            const params = new URLSearchParams({ corsoID, dataAppello });
		            makeAJAXCall("GET", "VediIscritti?" + params.toString(), null, (request) => {
		                if (request.readyState === XMLHttpRequest.DONE) {
		                    if (request.status === 200) {
		                        try {
		                            const data = JSON.parse(request.responseText);
		                            self.renderData(data.iscritti, data.corsoID, data.dataAppello);
		                        } catch (e) {
		                            self.error.showError("Errore nel parsing degli studenti.");
		                        }
		                    } else {
		                        self.error.showError("Errore nel caricamento degli studenti: " + request.status);
		                    }
		                }
		            });
		        }

		        renderData(studenti, corsoID, dataAppello) {
		            this.tableBody.innerHTML = "";
		            let haVotiDaPubblicare = false;
		            let haStudentiSenzaVoto = false;
		            let haVotiDaVerbalizzare = false;

		            if (!studenti || studenti.length === 0) {
		                const row = document.createElement("tr");
		                const cell = document.createElement("td");
		                cell.setAttribute("colspan", "6");
		                cell.textContent = "Nessuno studente iscritto.";
		                row.appendChild(cell);
		                this.tableBody.appendChild(row);
		                return;
		            }

		            studenti.forEach(studente => {
		                const row = document.createElement("tr");
		                
		                const matricolaCell = document.createElement("td");
		                matricolaCell.textContent = studente.matricola;
		                row.appendChild(matricolaCell);
		                
		                const nomeCell = document.createElement("td");
		                nomeCell.textContent = studente.nome;
		                row.appendChild(nomeCell);
		                
		                const cognomeCell = document.createElement("td");
		                cognomeCell.textContent = studente.cognome;
		                row.appendChild(cognomeCell);

		                const votoCell = document.createElement("td");
		                votoCell.textContent = studente.voto || "-";
		                row.appendChild(votoCell);
		                
		                const statoCell = document.createElement("td");
		                statoCell.textContent = studente.stato || "-";
		                row.appendChild(statoCell);
		                
		                if (studente.stato === 'INSERITO') {
		                    haVotiDaPubblicare = true;
		                }
		                
		                if (!studente.voto) { 
		                    haStudentiSenzaVoto = true;
		                }

		                if (studente.stato === 'PUBBLICATO') {
		                    haVotiDaVerbalizzare = true;
		                }
						
						if(studente.stato === 'INSERITO' || studente.stato === 'NON_INSERITO') {
		                const actionsCell = document.createElement("td");
		                const modificaBtn = document.createElement("button");
		                modificaBtn.type = "button";
		                modificaBtn.className = "btn-primary";
		                modificaBtn.textContent = "Modifica";
		                modificaBtn.addEventListener("click", () => {
		                    pageOrchestrator.modificaVoto.show(studente, corsoID, dataAppello);
		                });
		                actionsCell.appendChild(modificaBtn);
		                row.appendChild(actionsCell);
		                
						}
						this.tableBody.appendChild(row);
						// Aggiungi listener per l'ordinamento delle colonne
						const tableHeaders = document.querySelectorAll('#studenti-iscritti-table th[id^="sort-"]');
						tableHeaders.forEach(header => {
							header.addEventListener('click', () => {
								this.sortTable(header.id);
							});
						});
		            });
		            
		            this.apriInserimentoMultiploBtn.style.display = haStudentiSenzaVoto ? "inline-block" : "none";
		            this.pubblicaVotiBtn.style.display = haVotiDaPubblicare ? "inline-block" : "none";
		            this.verbalizzaBtn.style.display = haVotiDaVerbalizzare ? "inline-block" : "none";
		        }
				
				

					// --- Funzioni per l'ordinamento della tabella ---
					getCellValue(tr, idx) {
	                    return tr.children[idx].textContent;
	                }

	                resetArrows(rowHeaders) {
	                    for (let j = 0; j < rowHeaders.length; j++) {
	                        var toReset = rowHeaders[j].querySelectorAll("span");
	                        for (let i = 0; i < toReset.length; i++) {
	                            toReset[i].className = "normalarrow";
	                        }
	                    }
	                }

	                changeArrow(th) {
	                    var toChange = this.asc ? th.querySelector("span:first-child") : th.querySelector("span:last-child");
	                    if (toChange) {
	                        toChange.className = "boldarrow";
	                    }
	                }

	                createComparer(idx, asc) {
	                    const self = this;
	                    return function(rowa, rowb) {
	                        var v1 = self.getCellValue(asc ? rowa : rowb, idx),
	                            v2 = self.getCellValue(asc ? rowb : rowa, idx);

	                        if (v1 === '' || v2 === '' || isNaN(v1) || isNaN(v2)) {
	                            return v1.toString().localeCompare(v2);
	                        }
	                        return v1 - v2;
	                    };
	                }

	                sortTable(clicked_id) {
	                    var th = document.getElementById(clicked_id);
	                    var table = th.closest('table');
	                    var rowHeaders = table.querySelectorAll('th');
	                    var columnIdx = Array.from(rowHeaders).indexOf(th);
	                    var rowsArray = Array.from(table.querySelectorAll('tbody > tr'));
	                    
	                    rowsArray.sort(this.createComparer(columnIdx, this.asc));
	                    this.asc = !this.asc;
	                    
	                    this.resetArrows(rowHeaders);
	                    this.changeArrow(th);
	                    
	                    for (var i = 0; i < rowsArray.length; i++) {
	                        table.querySelector('tbody').appendChild(rowsArray[i]);
	                    }
	                }
					
					
		        
		        pubblicaVoti(appello) {
		            this.error.resetError();
		            
		            const formData = new FormData();
		            formData.append("corsoID", appello.corsoID);
		            formData.append("dataAppello", appello.dataAppello);

		            let self = this;
		            makeAJAXCall("POST", "Pubblica", formData, (request) => {
		                if (request.readyState === XMLHttpRequest.DONE) {
		                    if (request.status === 200) {
		                        self.error.showError("Voti pubblicati con successo.");
		                        self.show(appello.corsoID, appello.dataAppello);
		                    } else {
		                        self.error.showError("Errore durante la pubblicazione dei voti.");
		                    }
		                }
		            });
		        }
		        
		        verbalizza(appello) {
		            this.error.resetError();
		            const formData = new FormData();
		            formData.append("corsoID", appello.corsoID);
		            formData.append("dataAppello", appello.dataAppello);
		            
		            let self = this;
		            makeAJAXCall("POST", "Verbalizza", formData, (request) => {
		                if (request.readyState === XMLHttpRequest.DONE) {
		                    if (request.status === 200) {
		                        self.error.showError("Verbale creato con successo.");
								const data = JSON.parse(request.responseText);
		                        // Reindirizzamento o refresh della pagina degli iscritti
		                        self.show(appello.corsoID, appello.dataAppello);
								
								pageOrchestrator.verbali.show();
		                        // E, come richiesto, mostra i dettagli del verbale creato
		                        pageOrchestrator.verbali.showDettagli(data);
		                        
		                    } else {
		                        self.error.showError("Errore durante la verbalizzazione.");
		                    }
		                }
		            });
		        }
				
				hide() {
					this.container.style.display = "none";
					this.tableBody.innerHTML = "";
				}
			}
			
			const VOTI_POSSIBILI = [
						    "ASSENTE", "RIMANDATO", "RIPROVATO", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "30L"
						];
			
			class ModificaVoto {
				constructor(errorHandler) {
					this.modale = document.getElementById("modifica-voto-modale");
					this.form = document.getElementById("modifica-voto-form");
					this.studenteInfoSpan = document.getElementById("studente-info-modale");
					this.studenteIDInput = document.getElementById("studenteID-input");
					this.corsoIDInput = document.getElementById("corsoID-input");
					this.dataAppelloInput = document.getElementById("dataAppello-input");
					this.votoSelect = document.getElementById("voto-modifica");
					this.error = errorHandler;
					this.setupFormListener();
				}
				
				setupFormListener() {
					this.form.addEventListener("submit", (event) => {
						event.preventDefault();
						
						makeAJAXCall("POST", "ModificaVoto", this.form, (request) => {
							if (request.readyState === XMLHttpRequest.DONE) {
								if (request.status === 200) {
									this.error.showError("Voto modificato con successo.");
									this.hide();
									pageOrchestrator.studentiIscritti.show(
										this.corsoIDInput.value,
										this.dataAppelloInput.value
									);
								} else {
									this.error.showError("Errore durante la modifica del voto.");
								}
							}
						});
					});
				}

				show(studente, corsoID, dataAppello) {
					this.error.resetError();
		            this.modale.style.display = "block";
		            
		            // Popola la modale direttamente con i dati forniti e la lista hardcoded
		            this.populateModal(studente, corsoID, dataAppello);
				}

		        populateModal(studente, corsoID, dataAppello) {
		            const votoCorrente = studente.voto;

		            this.studenteIDInput.value = studente.id;
		            this.corsoIDInput.value = corsoID;
		            this.dataAppelloInput.value = dataAppello;
		            this.studenteInfoSpan.textContent = `${studente.nome} ${studente.cognome}`;
		            
					this.votoSelect.innerHTML = "";
		            VOTI_POSSIBILI.forEach(voto => {
		                const option = document.createElement("option");
		                option.value = voto;
		                option.textContent = voto;
		                this.votoSelect.appendChild(option);
		            });

		            if (votoCorrente && votoCorrente !== "null") {
		                this.votoSelect.value = votoCorrente;
		            } else {
		                this.votoSelect.value = "";
		            }
		        }
				
				hide() {
					this.modale.style.display = "none";
				}
			}
			
			
			class InserimentoVotiMultipli {
			    constructor(errorHandler) {
			        // Riferimenti agli elementi DOM del modale
			        this.modale = document.getElementById("inserimento_multiplo_modale");
			        this.tableBody = document.getElementById("inserimento_multiplo_corpo");
			        this.form = document.getElementById("inserimento-multiplo-form");
			        this.salvaBtn = document.getElementById("salva-multiplo-btn");
			        this.chiudiBtn = document.getElementById("close-multiplo-btn");
			        this.corsoIDInput = document.getElementById("corsoID-multiplo-input");
			        this.dataAppelloInput = document.getElementById("dataAppello-multiplo-input");
			        this.error = errorHandler;

			        // Listener per il salvataggio dei voti
			        this.setupFormListener();
			        
			        // Listener per la chiusura del modale
			        this.chiudiBtn.addEventListener("click", () => this.hide());
			    }
			    
			    /**
			     * Imposta il listener per il salvataggio dei voti multipli.
			     * Questa versione non usa async/await, ma un contatore per gestire le richieste.
			     */
			    setupFormListener() {
			        this.form.addEventListener("submit", (event) => {
			            event.preventDefault(); // Impedisce l'invio del form tradizionale
			            this.error.resetError();
			            
			            const votiInputs = this.tableBody.querySelectorAll('tr[data-matricola] .voto-input');
			            let requestsCompleted = 0;
			            let allVotiSubmitted = true;
			            const votiToSubmit = Array.from(votiInputs).filter(input => input.value && input.value !== "");
			            const totalRequests = votiToSubmit.length;

			            if (totalRequests === 0) {
			                this.error.showError("Nessun voto da salvare.");
			                return;
			            }

			            votiToSubmit.forEach(input => {
			                const matricola = input.parentElement.parentElement.dataset.matricola;
			                const voto = input.value;
			                
			                const formData = new FormData();
			                formData.append("studenteID", matricola);
			                formData.append("corsoID", this.corsoIDInput.value);
			                formData.append("dataAppello", this.dataAppelloInput.value);
			                formData.append("voto", voto);
			                
			                makeAJAXCall("POST", "ModificaVoto", formData, (request) => {
			                    if (request.readyState === XMLHttpRequest.DONE) {
			                        requestsCompleted++;
			                        if (request.status !== 200) {
			                            allVotiSubmitted = false;
			                            this.error.showError(`Errore nel salvataggio del voto per lo studente ${matricola}.`);
			                        }

			                        // Controlla se tutte le richieste sono terminate
			                        if (requestsCompleted === totalRequests) {
			                            if (allVotiSubmitted) {
			                                this.error.showError("Tutti i voti sono stati salvati con successo.");
			                                this.hide();
			                                pageOrchestrator.studentiIscritti.show(
			                                    this.corsoIDInput.value,
			                                    this.dataAppelloInput.value
			                                );
			                            }
			                        }
			                    }
			                });
			            });
			        });
			    }

			    /**
			     * Mostra la modale e avvia la richiesta per ottenere gli studenti iscritti.
			     * Questa funzione è simile al metodo show della classe ModificaVoto.
			     * @param {Object} appello L'oggetto appello con corsoID e dataAppello.
			     */
			    show(appello) {
			        this.error.resetError();
			        this.modale.style.display = "block";
			        
			        let self = this;
			        this.corsoIDInput.value = appello.corsoID;
			        this.dataAppelloInput.value = appello.dataAppello;

			        const params = new URLSearchParams({ 
			            corsoID: appello.corsoID, 
			            dataAppello: appello.dataAppello 
			        });

			        makeAJAXCall("GET", "StudentiSenzaVoti?" + params.toString(), null, (request) => {
			            if (request.readyState === XMLHttpRequest.DONE) {
			                if (request.status === 200) {
			                    try {
			                        const studenti = JSON.parse(request.responseText);
			                        self.populateModal(studenti);
			                    } catch (e) {
			                        self.error.showError("Errore nel parsing dei dati degli studenti per l'inserimento voti.");
			                        self.hide();
			                    }
			                } else {
			                    self.error.showError("Errore nel caricamento degli studenti per l'inserimento voti: " + request.status);
			                    self.hide();
			                }
			            }
			        });
			    }
			    
			    /**
			     * Popola la tabella all'interno del modale con i dati degli studenti.
			     * Questa funzione è l'equivalente di populateModal nella classe ModificaVoto,
			     * ma per un array di studenti.
			     * @param {Array} studenti L'array di studenti iscritti.
			     */
			    populateModal(studenti) {
			        this.tableBody.innerHTML = "";
			        
			        if (!studenti || studenti.length === 0) {
			            const row = document.createElement("tr");
			            const cell = document.createElement("td");
			            cell.setAttribute("colspan", "4");
			            cell.textContent = "Tutti gli studenti iscritti hanno già un voto.";
			            row.appendChild(cell);
			            this.tableBody.appendChild(row);
			            return;
			        }

			        studenti.forEach(studente => {
			            const row = document.createElement("tr");
			            row.dataset.matricola = studente.matricola;
			            
			            const matricolaCell = document.createElement("td");
			            matricolaCell.textContent = studente.matricola;
			            row.appendChild(matricolaCell);
			            
			            const nomeCell = document.createElement("td");
			            nomeCell.textContent = studente.nome;
			            row.appendChild(nomeCell);
			            
			            const cognomeCell = document.createElement("td");
			            cognomeCell.textContent = studente.cognome;
			            row.appendChild(cognomeCell);
			            
			            const votoCell = document.createElement("td");
			            const votoInput = document.createElement("input");
			            votoInput.type = "number";
			            votoInput.className = "voto-input";
			            votoInput.name = `voto-${studente.matricola}`;
			            votoInput.min = "18";
			            votoInput.max = "31";
			            votoCell.appendChild(votoInput);
			            row.appendChild(votoCell);
			            
			            this.tableBody.appendChild(row);
			        });
			    }
			    
			    /**
			     * Nasconde la modale.
			     */
			    hide() {
			        this.modale.style.display = "none";
			    }
			}
			


			class Verbali {
			    constructor(errorHandler) {
			        this.container = document.getElementById("verbali-visualizzazione-container");
			        this.lista = document.getElementById("verbali-lista");
			        this.tableBody = document.getElementById("verbali-table-body");
			        this.dettagli = document.getElementById("verbali-dettagli");
			        this.verbaleInfo = document.getElementById("verbale-info");
			        this.verbaleCorso = document.getElementById("verbale-corso");
			        this.verbaleData = document.getElementById("verbale-data");
			        this.verbaleVotiBody = document.getElementById("verbale-voti-body");
			        this.error = errorHandler;
			    }

			    show() {
			        this.error.resetError();
			        this.container.style.display = "flex";
			        this.dettagli.style.display = "none";
			        this.tableBody.innerHTML = "";

			        let self = this;
			        makeAJAXCall("GET", "MostraVerbali", null, (request) => {
			            if (request.readyState === XMLHttpRequest.DONE) {
			                if (request.status === 200) {
			                    try {
			                        const verbali = JSON.parse(request.responseText);
			                        self.renderVerbali(verbali);
			                    } catch (e) {
			                        self.error.showError("Errore nel parsing dei verbali.");
			                    }
			                } else {
			                    self.error.showError("Errore nel caricamento dei verbali: " + request.status);
			                }
			            }
			        });
			    }

			    renderVerbali(verbali) {
			        this.tableBody.innerHTML = "";
			        if (!verbali || verbali.length === 0) {
			            const row = document.createElement("tr");
			            const cell = document.createElement("td");
			            cell.setAttribute("colspan", "3");
			            cell.textContent = "Nessun verbale disponibile.";
			            row.appendChild(cell);
			            this.tableBody.appendChild(row);
			            return;
			        }

			        verbali.forEach(verbale => {
			            const row = document.createElement("tr");
			            const appelloCell = document.createElement("td");
			            appelloCell.textContent = `Appello del ${verbale.dataAppello}`;
			            row.appendChild(appelloCell);

			            const dataCell = document.createElement("td");
			            dataCell.textContent = verbale.dataVerbale;
			            row.appendChild(dataCell);

			            const actionsCell = document.createElement("td");
			            const dettagliBtn = document.createElement("button");
			            dettagliBtn.type = "button";
			            dettagliBtn.className = "btn-secondary";
			            dettagliBtn.textContent = "Dettagli";
			            dettagliBtn.addEventListener("click", () => {
			                this.showDettagli(verbale.verbaleID);
			            });
			            actionsCell.appendChild(dettagliBtn);
			            row.appendChild(actionsCell);

			            this.tableBody.appendChild(row);
			        });
			    }

			    showDettagli(verbaleID) {
			        this.error.resetError();
			        this.dettagli.style.display = "block";
			        this.verbaleInfo.textContent = `Caricamento dettagli...`;

			        let self = this;
			        const param = new URLSearchParams({ verbaleID });

			        makeAJAXCall("GET", "MostraVerbaleCreato?" + param.toString(), null, (request) => {
			            if (request.readyState === XMLHttpRequest.DONE) {
			                if (request.status === 200) {
			                    try {
			                        const data = JSON.parse(request.responseText);
			                        self.renderDetails(data);
			                    } catch (e) {
			                        self.error.showError("Errore nel parsing dei dettagli del verbale.");
			                    }
			                } else {
			                    self.error.showError("Errore nel caricamento dei dettagli: " + request.status);
			                }
			            }
			        });
			    }

			    renderDetails(data) {
			        this.verbaleInfo.textContent = `Verbale ID: ${data.verbaleId} - ${data.verbaleDataOra}`;
			        this.verbaleCorso.textContent = data.corso.nome;
			        this.verbaleData.textContent = data.appello.data;

			        this.verbaleVotiBody.innerHTML = "";
			        data.studenti.forEach(studente => {
			            const row = document.createElement("tr");

			            const matricolaCell = document.createElement("td");
			            matricolaCell.textContent = studente.matricola;
			            row.appendChild(matricolaCell);

			            const nomeCell = document.createElement("td");
			            nomeCell.textContent = studente.nome;
			            row.appendChild(nomeCell);

			            const cognomeCell = document.createElement("td");
			            cognomeCell.textContent = studente.cognome;
			            row.appendChild(cognomeCell);

			            const votoCell = document.createElement("td");
			            votoCell.textContent = studente.voto || "-";
			            row.appendChild(votoCell);

			            this.verbaleVotiBody.appendChild(row);
			        });
			    }

			    hideDetails() {
			        this.dettagli.style.display = "none";
			    }
			}
	
	
	// --- Page Controller  ---
	let pageOrchestrator = new PageOrchestrator();
	
	window.addEventListener("load",() => {pageOrchestrator.init(); pageOrchestrator.refresh();}, false);
							 
							 
};

