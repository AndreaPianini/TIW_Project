/* ---------------------------------------------------------
 *  DocenteHome.js – versione aggiornata con:
 *   • endpoint italiani (VaiHomeDocente, Modifica, …)
 *   • componente SingleStudentDetails
 *   • funzione modifyStudent() agganciata alla tabella
 * -------------------------------------------------------*/
(function () {  

  /* ----------------- Personal message ----------------- */
  function PersonalMessage(text, container) {
    this.show = () => { container.textContent = text; };
  }

  /* --------------- LISTA CORSI + APPELLI ---------------*/
  function CoursesList(alertBox, ulContainer) {
    this.appelliMap = new Map();

    this.reset = () => {
      ulContainer.innerHTML = "";
      ulContainer.style.visibility = "hidden";
      this.appelliMap.clear();
    };

    this.show = () => {
      makeCall("GET", "VaiHomeDocente", null, (req) => {
        if (req.readyState !== 4) return;
        if (req.status === 200) {
          const payload = JSON.parse(req.responseText);
          const corsi   = payload.corsi;
          const appelli = payload.appelli;
          corsi.forEach((c, i) => this.appelliMap.set(c.id, appelli[i]));
          this.update(corsi);
        } else {
          alertBox.textContent = req.responseText;
        }
      });
    };

    this.update = (courses) => {
      this.reset();
      courses.forEach((c) => {
        const li = document.createElement("li");
        const a  = document.createElement("a");
        a.textContent = c.nome;
        a.href = "#";
        a.addEventListener("click", () => {
          courseAppeals.update(this.appelliMap.get(c.id), c.id);
        });
        li.appendChild(a);
        ulContainer.appendChild(li);
      });
      ulContainer.style.visibility = "visible";
    };
  }

  /* ---------------- LISTA APPELLI --------------------- */
  function CourseAppeals(alertBox) {
    const appealListEl = document.getElementById("id_appeals");
    let currentCourseId = null;

    this.update = (appeals, courseId) => {
      currentCourseId = courseId;
      appealListEl.innerHTML = "";
      if (!appeals || appeals.length === 0) {
        alertBox.textContent = "Nessun appello per questo corso.";
        return;
      }
      appeals.forEach((a) => {
        const li  = document.createElement("li");
        const btn = document.createElement("button");
        btn.textContent = a.data;
        btn.addEventListener("click", () => registeredView.show(currentCourseId, a.data));
        li.appendChild(btn);
        appealListEl.appendChild(li);
      });
    };
  }

  /* -------- COMPONENTE Dettaglio Studente ------------- */
  function SingleStudentDetails() {
    const container  = document.getElementById("id_studentdetailscontainer");
    const idSpan     = document.getElementById("id_studentid");
    const nameSpan   = document.getElementById("id_studentname");
    const surSpan    = document.getElementById("id_studentsurname");
    const form       = document.getElementById("id_modifyevaluationform");
    const selectEval = document.getElementById("id_evalselect");

    this.update = (stud, courseId, appealDate) => {
      idSpan.textContent   = stud.matricola;
      nameSpan.textContent = stud.nome;
      surSpan.textContent  = stud.cognome;
      selectEval.value = "";
      container.hidden = false;

      document.getElementById("id_modifyevaluationbutton").onclick = () => {
        const val = selectEval.value;
        if (!val) { alert("Seleziona un voto"); return; }

        makeCall(
          "POST",
          `ModificaVoto?studenteID=${stud.matricola}&corsoID=${courseId}&dataAppello=${appealDate}&voto=${val}`,
          form,
          (req) => {
            if (req.readyState !== 4) return;
            if (req.status === 200) {
              container.hidden = true;
              registeredView.show(courseId, appealDate); // refresh tabella
            } else {
              document.getElementById("id_alert").textContent = req.responseText;
            }
          }
        );
      };
    };
  }
  
  /* ----------- COMPONENTE Dettaglio Verbale ----------- */
  function ReportDetails() {
    const container = document.getElementById("id_reportdetailscontainer");
    const tbody     = document.getElementById("id_reportstudentdetailscontainerbody");

    /* aggiorna UI con il JSON ricevuto */
    this.update = (rep, courseId, appealDate) => {
      document.getElementById("id_reportnumber").textContent   = rep.id;
      document.getElementById("id_reportdate").textContent     = rep.data;
      document.getElementById("id_reporttime").textContent     = rep.ora;
      document.getElementById("id_reportcourseid").textContent = courseId;
      document.getElementById("id_reportappealdate").textContent = appealDate;

      /* popola tabella studenti */
      tbody.innerHTML = "";
      rep.studentsData.forEach(sd => {
        const tr = document.createElement("tr");
        ["studentId","studentName","studentSurname","studentMark"]
          .forEach(k => {
            const td = document.createElement("td");
            td.textContent = sd[k];
            tr.appendChild(td);
          });
        tbody.appendChild(tr);
      });
	  
	  a.addEventListener("click",
	    () => this.viewReport(v.id, v.corsoId, v.dataAppello));


      container.hidden = false;
    };
	
	this.viewReport = (vid, cid, appDate) => {
	  makeCall("GET",
	           `MostraVerbaleCreato?verbaleID=${vid}&corsoID=${cid}&dataAppello=${appDate}`,
	           null,
	           (r) => {
	             if (r.readyState !== 4) return;
	             if (r.status === 200) {
	               reportDetails.update(JSON.parse(r.responseText), cid, appDate);
	             } else {
	               alertBox.textContent = r.responseText;
	             }
	           });
	};

  }
  
  /* ------------ COMPONENTE Modale Voti Multipli ------------ */
  function MultipleInsertionModal(alertBox) {
    const modal   = document.getElementById("id_multipleinsertionmodal");
    const tbody   = document.getElementById("id_multipleinsertioncontainerbody");
    const formBtn = document.getElementById("id_multipleinsertionsubmitbutton");
    const closeEl = modal.querySelector(".close");

    let currentCourse, currentDate;
    let studenti = [];

    /* — apri e carica studenti senza voto — */
    this.open = (courseId, appealDate) => {
      currentCourse = courseId;
      currentDate   = appealDate;

      makeCall(
        "GET",
        `GetRegisteredStudentsWithoutEvaluationByAppeal?idCorso=${courseId}&dataAppello=${appealDate}`,
        null,
        (req) => {
          if (req.readyState !== 4) return;
          if (req.status === 200) {
            studenti = JSON.parse(req.responseText);
            if (studenti.length === 0) {
              alertBox.textContent = "Nessuno studente senza valutazione.";
              return;
            }
            renderTable();
            modal.style.display = "block";
          } else {
            alertBox.textContent = req.responseText;
          }
        }
      );
    };

    /* — riempie la tabella del modale — */
    function renderTable() {
      tbody.innerHTML = "";
      studenti.forEach(st => {
        const tr = document.createElement("tr");

        ["studentId","studentName","studentSurname","studentEmail","studentDegree"]
          .forEach(k => {
            const td = document.createElement("td");
            td.textContent = st[k];
            tr.appendChild(td);
          });

        /* select voto */
        const selTd = document.createElement("td");
        const sel = document.createElement("select");
        sel.innerHTML =
          `<option value=\"\"></option><option>ASSENTE</option><option>RIMANDATO</option><option>RIPROVATO</option>` +
          `${Array.from({length:13},(_,i)=>18+i).map(v=>`<option>${v}</option>`).join("")}` +
          `<option>30L</option>`;
        sel.id = `sel_${st.studentId}`;
        selTd.appendChild(sel);
        tr.appendChild(selTd);

        tbody.appendChild(tr);
      });
    }

    /* — salva tutti i voti scelti — */
    formBtn.querySelector("button").onclick = () => {
      const promises = [];
      studenti.forEach(st => {
        const voto = document.getElementById(`sel_${st.studentId}`).value;
        if (voto !== "") {
          promises.push(new Promise(res => {
            makeCall(
              "POST",
              `ModificaVoto?studenteID=${st.studentId}&corsoID=${currentCourse}&dataAppello=${currentDate}&voto=${voto}`,
              null,
              () => res()
            );
          }));
        }
      });

      Promise.all(promises).then(() => {
        modal.style.display = "none";
        registeredView.show(currentCourse, currentDate);   // refresh tabella iscritti
      });
    };

    /* chiusura modale */
    closeEl.onclick = () => { modal.style.display = "none"; };
    window.onclick  = (e) => { if (e.target === modal) modal.style.display = "none"; };
  }

  
  /* ------------ COMPONENTE Lista Verbali ------------ */
  function VerbaliList(alertBox, ulContainer) {

    /* 1) Recupera e stampa la lista */
    this.show = () => {
      makeCall("GET", "MostraVerbali", null, (req) => {
        if (req.readyState !== 4) return;
        if (req.status === 200) {
          const verbali = JSON.parse(req.responseText);     // array di verbali
          this.update(verbali);
        } else {
          alertBox.textContent = req.responseText;
        }
      });
    };

    /* 2) Popola la <ul> con i verbali */
    this.update = (verbali) => {
      ulContainer.innerHTML = "";
      if (!verbali || verbali.length === 0) {
        ulContainer.textContent = "Nessun verbale trovato.";
        return;
      }

      verbali.forEach(v => {
        const li  = document.createElement("li");
        const a   = document.createElement("a");
        a.href = "#";
        a.textContent = `${v.corsoNome} – ${v.dataAppello}  (N° ${v.id})`;
        a.addEventListener("click", () => viewReport(v.id, v.corsoId, v.dataAppello));
        li.appendChild(a);
        ulContainer.appendChild(li);
      });
    };
  }



  /* --------------- LISTA ISCRITTI -------------------- */
  function RegisteredStudentsDetails(alertBox) {
    const containerWrap = document.getElementById("id_registeredstudentscontainer");
    const tbody = document.getElementById("id_registeredstudentscontainerbody");
    const stringEl = document.getElementById("id_registeredstudentsstring");

    const publishForm  = document.getElementById("id_publishbutton");
    const reportForm   = document.getElementById("id_createreportbutton");
    const multiFormBtn = document.getElementById("id_multipleinsertionbutton");

    /* ---- ORDINAMENTO tabella iscritti ---- */
    const headerLinks = document.querySelectorAll(".column-label a");
    const marksOrder  = ["", "ASSENTE","RIMANDATO","RIPROVATO"]
                        .concat(Array.from({length:13},(_,i)=> (18+i).toString() ))
                        .concat("30L");

    let currentSort   = { column: null, asc: true };

    function compareCells(a, b, column) {
      const tA = a.cells[column].textContent.toLowerCase();
      const tB = b.cells[column].textContent.toLowerCase();
      if (column === 5) {
        return marksOrder.indexOf(tA) - marksOrder.indexOf(tB);
      }
      return tA.localeCompare(tB);
    }

    headerLinks.forEach((link, idx) => {
      link.addEventListener("click", () => {
        if (currentSort.column === idx) {
          currentSort.asc = !currentSort.asc;
        } else {
          currentSort = { column: idx, asc: true };
        }
        const rows = Array.from(tbody.querySelectorAll("tr"));
        rows.sort((r1, r2) => {
          const cmp = compareCells(r1, r2, currentSort.column);
          return currentSort.asc ? cmp : -cmp;
        });
        tbody.innerHTML = "";
        rows.forEach(tr => tbody.appendChild(tr));
      });
    });

    this.show = (courseId, appealDate) => {
      makeCall("GET", `VediIscritti?corsoID=${courseId}&dataAppello=${appealDate}&sortBy=matricola&order=ASC`, null, (req) => {
        if (req.readyState !== 4) return;
        if (req.status === 200) {
          const data = JSON.parse(req.responseText);
          this.update(data.iscritti, courseId, appealDate);
        } else {
          alertBox.textContent = req.responseText;
        }
      });
    };

    this.update = (students, courseId, appealDate) => {
      stringEl.textContent = `Iscritti all'appello del ${appealDate}`;
      tbody.innerHTML = "";

      students.forEach((s) => {
        const tr = document.createElement("tr");
        ["matricola","cognome","nome","email","corsoLaurea","voto","stato"].forEach((k)=>{
          const td=document.createElement("td");
          td.textContent = s[k] ?? "";
          tr.appendChild(td);
        });

        const actionTd = document.createElement("td");
        if (s.stato === "INSERITO" || s.stato === "NON_INSERITO") {
          const btn = document.createElement("button");
          btn.textContent = "Modifica";
          btn.addEventListener("click", () => this.openStudent(courseId, appealDate, s.matricola));
          actionTd.appendChild(btn);
        }
        tr.appendChild(actionTd);
        tbody.appendChild(tr);
      });

      publishForm.querySelector("input[name='appealDate']").value = appealDate;
      publishForm.querySelector("input[name='courseId']").value   = courseId;
      reportForm.querySelector("input[name='dataAppello']").value = appealDate;
      reportForm.querySelector("input[name='idCorso']").value     = courseId;
      multiFormBtn.querySelector("input[name='dataAppello']").value = appealDate;
      multiFormBtn.querySelector("input[name='idCorso']").value     = courseId;

      publishForm.querySelector("button").onclick =
        () => this.publishAppeal(courseId, appealDate);
      reportForm.querySelector("button").onclick =
        () => this.createReport(courseId, appealDate);
      multiFormBtn.querySelector("button").onclick =
        () => multiModal.open(courseId, appealDate);

      containerWrap.style.visibility = "visible";
    };

    this.publishAppeal = (courseId, appealDate) => {
      makeCall("POST", `Pubblica?corsoID=${courseId}&dataAppello=${appealDate}`, null, (req) => {
        if (req.readyState !== 4) return;
        if (req.status === 200) {
          this.show(courseId, appealDate);
        } else {
          alertBox.textContent = req.responseText;
        }
      });
    };

    this.createReport = (courseId, appealDate) => {
      makeCall("POST", `Pubblica?corsoID=${courseId}&dataAppello=${appealDate}`, null, (req) => {
        if (req.readyState !== 4) return;
        if (req.status === 200) {
          const vid = JSON.parse(req.responseText);
          if (vid === -1) {
            alertBox.textContent = "Impossibile creare un nuovo verbale (nessun voto pubblicato)";
            return;
          }
          makeCall("GET", `MostraVerbaleCreato?verbaleID=${vid}&corsoID=${courseId}&dataAppello=${appealDate}`, null, (r2) => {
            if (r2.readyState !== 4) return;
            if (r2.status === 200) {
              const report = JSON.parse(r2.responseText);
              reportDetails.update(report, courseId, appealDate);
              verbaliList.show();
              this.show(courseId, appealDate);
            } else {
              alertBox.textContent = r2.responseText;
            }
          });
        } else {
          alertBox.textContent = req.responseText;
        }
      });
    };

    this.openStudent = (courseId, appealDate, studId) => {
      makeCall("GET", `Modifica?studenteID=${studId}&corsoID=${courseId}&dataAppello=${appealDate}`, null, (req) => {
        if (req.readyState !== 4) return;
        if (req.status === 200) {
          const json = JSON.parse(req.responseText);
          singleDetail.update(json.studente, courseId, appealDate);
        } else {
          alertBox.textContent = req.responseText;
        }
      });
    };
  }




  /* ------------- ORCHESTRATOR COMPLETO ------------- */
  function PageOrchestrator() {
    /* riferimento all’alert globale */
    const alertBox = document.getElementById("id_alert");

    /* 1. Messaggio personale */
    const personalMsg = new PersonalMessage(
      JSON.parse(sessionStorage.getItem("user")).nome,
      document.getElementById("id_username")
    );

    /* 2. Vista corsi (si occuperà anche di mappare gli appelli) */
    const coursesList = new CoursesList(
      alertBox,
      document.getElementById("id_courses")
    );

    /* 3. Vista appelli (non fa AJAX, usa la mappa di coursesList) */
    window.courseAppeals = new CourseAppeals(alertBox); // se serve globale

    /* 4. Vista iscritti all’appello */
    window.registeredView = new RegisteredStudentsDetails(alertBox);

    /* 5. Vista dettaglio singolo studente */
    window.singleDetail = new SingleStudentDetails();
	
	window.reportDetails = new ReportDetails();
	
	/* 6. lista verbali (cliccabile) */
	window.verbaliList = new VerbaliList(
	  alertBox,
	  document.getElementById("id_verbali")
	);
	
	window.multiModal = new MultipleInsertionModal(alertBox);




    /* ------- metodi publici ------- */

	/* Avvio iniziale */
	this.start = () => {
	  alertBox.textContent = "";
	  personalMsg.show();
	  coursesList.show();     // carica corsi + appelli
	  verbaliList.show();          // carica e mostra la lista verbali

	};

	/* Refresh generale: torna allo stato “pagina aperta per la prima volta” */
	this.refresh = () => {
	  alertBox.textContent = "";

	  /* 1. Resetta tutte le viste */
	  coursesList.reset();                  // svuota lista corsi
	  courseAppeals.update([], null);       // svuota lista appelli
	  registeredView.show  = () => {};      // disattiva tabella iscritti
	  document.getElementById("id_registeredstudentscontainer").style.visibility = "hidden";

	  /* Nascondi pannelli laterali se sono visibili */
	  document.getElementById("id_studentdetailscontainer").hidden = true;
	  document.getElementById("id_reportdetailscontainer").hidden  = true;

	  /* 2. Ricarica i corsi (e quindi gli appelli) */
	  coursesList.show();
	};

  }

  /*   --------- bootstrap pagina ---------   */
  window.addEventListener("load", () => {
    if (sessionStorage.getItem("user") == null) {
      window.location.href = "index.html";
    } else {
      new PageOrchestrator().start();
    }
  });
