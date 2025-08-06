/**
 * Docente Controller

{
	
	// Funzioni di utilità (copiate dal tuo codice per consistenza)
	function makeAJAXCall( method, url, formElement, callBack, reset = true) {
	    var request = new XMLHttpRequest();
	    request.onreadystatechange = function() {
	      callBack(request)
	    };
	    request.open(method, url);
	    if (formElement == null) {
	    	request.send();
	    } 
		else {
	    	request.send(new FormData(formElement));
	    }
		if (formElement !== null && reset === true) {
			// formElement.reset(); // Commentato perché non vogliamo resettare il form di inserimento multiplo
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
	                makeAJAXCall("POST", "LogoutServlet", null, (request) => {
	                    if (request.readyState === XMLHttpRequest.DONE && request.status === 200) {
	                        window.location.href = "login.html";
	                    }
	                });
	            });

	            const nascondiElencoBtn = document.getElementById("nascondi-elenco-btn");
	            nascondiElencoBtn.addEventListener("click", () => {
	                this.studentiIscritti.hide();
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
	            
	            const pubblicaVotiBtn = document.getElementById("pubblica-voti-btn");
	            pubblicaVotiBtn.addEventListener("click", () => {
	                const appello = this.studentiIscritti.currentAppello;
	                if (appello) {
	                    this.studentiIscritti.pubblicaVoti(appello);
	                }
	            });
	            
	            // Listener per il pulsante "Verbalizza"
	            const verbalizzaBtn = document.getElementById("verbalizza-btn");
	            verbalizzaBtn.addEventListener("click", () => {
	                const appello = this.studentiIscritti.currentAppello;
	                if (appello) {
	                    this.studentiIscritti.verbalizza(appello);
	                }
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
				this.appelliTable.style.display = "none";
	            this.appelliTitle.style.display = "none";
	            this.backToCorsiBtn.style.display = "none";
				
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
	                            pageOrchestator.studentiIscritti.show(corso.id, dataConverter(appello.data));
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
		            this.verbalizzaBtn = document.getElementById("verbalizza-btn"); // Nuovo pulsante
		            this.error = errorHandler;
		            this.currentAppello = null;
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
		            let hasVotiDaPubblicare = false;
		            let hasStudentiSenzaVoto = false;
		            let tuttiVotiPubblicati = true;

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
		                
		                if (studente.stato && studente.stato.toLowerCase() === 'modificato') {
		                    hasVotiDaPubblicare = true;
		                }
		                
		                if (!studente.voto) { 
		                    hasStudentiSenzaVoto = true;
		                    tuttiVotiPubblicati = false;
		                }

		                if (studente.voto && studente.stato.toLowerCase() !== 'pubblicato' && studente.stato.toLowerCase() !== 'verbalizzato') {
		                    tuttiVotiPubblicati = false;
		                }

		                const actionsCell = document.createElement("td");
		                const modificaBtn = document.createElement("button");
		                modificaBtn.type = "button";
		                modificaBtn.className = "btn-primary";
		                modificaBtn.textContent = "Modifica";
		                modificaBtn.addEventListener("click", () => {
		                    pageOrchestator.modificaVoto.show(studente.matricola, corsoID, dataAppello);
		                });
		                actionsCell.appendChild(modificaBtn);
		                row.appendChild(actionsCell);
		                
		                this.tableBody.appendChild(row);
		            });
		            
		            this.apriInserimentoMultiploBtn.style.display = hasStudentiSenzaVoto ? "inline-block" : "none";
		            this.pubblicaVotiBtn.style.display = hasVotiDaPubblicare ? "inline-block" : "none";
		            this.verbalizzaBtn.style.display = tuttiVotiPubblicati && !hasStudentiSenzaVoto ? "inline-block" : "none";
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
		                        // Reindirizzamento o refresh della pagina degli iscritti
		                        self.show(appello.corsoID, appello.dataAppello);
		                        // E, come richiesto, mostra i dettagli del verbale creato
		                        pageOrchestator.verbali.showDetails(appello.corsoID, appello.dataAppello);
		                        
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
									pageOrchestator.studentiIscritti.show(
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

				show(studenteID, corsoID, dataAppello) {
		            this.error.resetError();
		            this.modale.style.display = "block";
		            
		            let self = this;
		            const params = new URLSearchParams({ studenteID, corsoID, dataAppello });
		            
		            makeAJAXCall("GET", "ModificaVoto?" + params.toString(), null, (request) => {
		                if (request.readyState === XMLHttpRequest.DONE) {
		                    if (request.status === 200) {
		                        try {
		                            const data = JSON.parse(request.responseText);
		                            self.populateModal(data);
		                        } catch (e) {
		                            self.error.showError("Errore nel parsing dei dati per la modifica.");
		                            self.hide();
		                        }
		                    } else {
		                        self.error.showError("Errore nel caricamento dei dati per la modifica: " + request.status);
		                        self.hide();
		                    }
		                }
		            });
				}

		        populateModal(data) {
		            const studente = data.studente;
		            const votoCorrente = data.valutazione;
		            const votiPossibili = data.votiPossibili;

		            this.studenteIDInput.value = studente.id;
		            this.corsoIDInput.value = data.corsoID;
		            this.dataAppelloInput.value = data.dataAppello;
		            this.studenteInfoSpan.textContent = `${studente.nome} ${studente.cognome}`;
		            
		            this.votoSelect.innerHTML = "";
		            votiPossibili.forEach(voto => {
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
					this.modale = document.getElementById("inserimento_multiplo_modale");
					this.tableBody = document.getElementById("inserimento_multiplo_corpo");
					this.form = document.getElementById("inserimento-multiplo-form");
					this.corsoIDInput = document.getElementById("corsoID-multiplo-input");
					this.dataAppelloInput = document.getElementById("dataAppello-multiplo-input");
					this.salvaBtn = document.getElementById("salva-multiplo-btn");
					this.error = errorHandler;
		            this.setupFormListener();
				}
				
				setupFormListener() {
					this.salvaBtn.addEventListener("click", async () => {
		                this.error.resetError();
		                const votiInputs = this.tableBody.querySelectorAll('tr[data-matricola] .voto-input');
		                let allVotiSubmitted = true;
		                
		                for (const input of votiInputs) {
		                    const matricola = input.parentElement.parentElement.dataset.matricola;
		                    const voto = input.value;
		                    
		                    if (voto && voto !== "") {
		                        const formData = new FormData();
		                        formData.append("studenteID", matricola);
		                        formData.append("corsoID", this.corsoIDInput.value);
		                        formData.append("dataAppello", this.dataAppelloInput.value);
		                        formData.append("voto", voto);
		                        
		                        try {
		                            const request = await new Promise((resolve, reject) => {
		                                makeAJAXCall("POST", "ModificaVoto", formData, (req) => {
		                                    if (req.readyState === XMLHttpRequest.DONE) {
		                                        resolve(req);
		                                    }
		                                }, false);
		                            });

		                            if (request.status !== 200) {
		                                allVotiSubmitted = false;
		                                this.error.showError(`Errore nel salvataggio del voto per lo studente ${matricola}.`);
		                            }
		                        } catch (e) {
		                            allVotiSubmitted = false;
		                            this.error.showError(`Errore nella richiesta per lo studente ${matricola}.`);
		                        }
		                    }
		                }
		                
		                if (allVotiSubmitted) {
		                    this.error.showError("Tutti i voti sono stati salvati con successo.");
		                    this.hide();
		                    pageOrchestator.studentiIscritti.show(
		                        this.corsoIDInput.value,
		                        this.dataAppelloInput.value
		                    );
		                }
					});
				}

				show(appello) {
		            this.error.resetError();
		            this.modale.style.display = "block";
		            this.tableBody.innerHTML = "";
		            this.corsoIDInput.value = appello.corsoID;
		            this.dataAppelloInput.value = appello.dataAppello;

		            let self = this;
		            const params = new URLSearchParams({ corsoID: appello.corsoID, dataAppello: appello.dataAppello });
		            makeAJAXCall("GET", "StudentiSenzaVoti?" + params.toString(), null, (request) => {
		                if (request.readyState === XMLHttpRequest.DONE) {
		                    if (request.status === 200) {
		                        try {
		                            const studenti = JSON.parse(request.responseText);
		                            self.renderStudentiVoti(studenti);
		                        } catch (e) {
		                            self.error.showError("Errore nel parsing dei dati degli studenti per l'inserimento voti.");
		                        }
		                    } else {
		                        self.error.showError("Errore nel caricamento degli studenti per l'inserimento voti: " + request.status);
		                    }
		                }
		            });
				}
				//l'elenco di studenti senza un voto
				renderStudentiVoti(studenti) {
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
			        makeAJAXCall("GET", "ListaVerbali", null, (request) => {
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
			                this.showDettagli(verbale.corsoID, verbale.dataAppello);
			            });
			            actionsCell.appendChild(dettagliBtn);
			            row.appendChild(actionsCell);

			            this.tableBody.appendChild(row);
			        });
			    }

			    showDettagli(corsoID, dataAppello) {
			        this.error.resetError();
			        this.dettagli.style.display = "block";
			        this.verbaleInfo.textContent = `Caricamento dettagli...`;

			        let self = this;
			        const params = new URLSearchParams({ corsoID, dataAppello });

			        makeAJAXCall("GET", "MostraVerbaleCreato?" + params.toString(), null, (request) => {
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

*/