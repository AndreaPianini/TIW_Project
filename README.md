# 📘 University Web Application – TIW Project

### *Developed for the “Tecnologie Informatiche per il Web” course*  
**Authors:** Andrea Pianini – Marco Puddu

---

## 🧩 Overview

This project implements a **web application for exam management** in a university context.  
It allows **students** and **professors** to manage and view exam sessions (*appelli*), grades, and official reports (*verbali*), following a clear and secure workflow.

The project includes two separate implementations:
- **Thin Client:** classic server-side rendering (HTML + Servlet + JSP/Thymeleaf)
- **Thick Client:** single-page AJAX-based version with dynamic interactions

---

## 🎯 Project Goals

- Provide a web-based system for managing exams and grades.
- Ensure distinct functionalities and access levels for **students** and **professors**.
- Guarantee **data consistency**, **security**, and **ease of navigation**.
- Demonstrate both **Thin** and **Thick** client architectures in a Java EE environment.

---

## 👥 Users and Functionalities

### 👨‍🏫 **Professor**
- Log in using personal credentials.
- Select one of their assigned courses (alphabetically sorted).
- View all exam sessions (*appelli*) for a selected course (sorted by date).
- Access a list of registered students for an exam session.
- Sort student lists dynamically by column headers (surname, ID, etc.).
- **Modify grades** individually or through **bulk grade insertion**.
- **Publish results**, making grades visible to students.
- **Verbalize grades** (finalize results) and automatically generate a report (*verbale*).
- View all previously created *verbali*, ordered by course and date.

### 👩‍🎓 **Student**
- Log in using matriculation number and password.
- Select a course and a related exam session.
- View their evaluation status:
  - “Grade not yet defined”
  - Published grade
- If the grade is between 18 and 30L, the student can **refuse the grade**.
  - In the Thick Client version, this is done via **drag & drop** of the grade into a trash icon.
- Once refused, the grade becomes “rejected” and is updated in the professor’s view.

---

## 🧱 Architecture Overview

### 🪶 **Thin Client Version**
- Each user interaction reloads a new page rendered server-side.
- The server handles:
  - Authentication
  - Authorization (via filters)
  - Data access
  - HTML generation (via templates)

**Main Components:**
- **Controllers (Servlets):** handle navigation, CRUD actions, and redirects.
- **DAO Layer:** manages all database access.
- **Model (Beans):** encapsulates application entities.
- **Filters:** enforce access control for student and professor roles.
- **Views (HTML Templates):** render user interfaces for each page.

---

### ⚡ **Thick Client Version**
- Implemented as a **single-page web application**.
- Uses **AJAX** for asynchronous communication with the backend.
- Page content updates dynamically without full reloads.
- Client-side sorting and grade refusal (drag & drop).
- Modal dialogs for editing or inserting multiple grades.

**Client-Side Logic Includes:**
- Asynchronous data fetching and DOM updates.
- Dynamic modals for editing/creating entries.
- Confirmation popups for sensitive actions (e.g., grade refusal).

---

## 🗂️ System Structure

### 📁 **Main Packages**

| Layer | Package | Description |
|-------|----------|-------------|
| Controller | `controllers` | Java servlets managing user requests and navigation |
| Model | `beans` | Data model (User, Course, Exam, Evaluation, Report, etc.) |
| DAO | `dao` | SQL queries and database access |
| Filter | `filters` | Access control and session validation |
| View | `/webapp/` | HTML templates (student and professor interfaces) |

---

### 📜 **Key Servlets**

#### Common
- `Login`, `CheckLogin`, `Logout`

#### Professor
- `VaiHomeDocente`, `VediIscritti`, `ModificaVoto`, `Pubblica`, `Verbalizza`, `MostraVerbali`, `MostraVerbaleCreato`

#### Student
- `VaiHomeStudente`, `VediVoto`, `RifiutaVoto`

---

### 🧩 **Main Entities (Beans)**
- `User`
- `Docente` (Professor)
- `Studente` (Student)
- `Corso` (Course)
- `Appello` (Exam Session)
- `Valutazione` (Grade/Evaluation)
- `Verbale` (Official Report)

---

### 💾 **DAO Methods Overview**

| DAO | Methods |
|-----|----------|
| `UtenteDAO` | `checkCredenziali()` |
| `DocenteDAO` | `getCorsiAndAppelliByDocente()`, `getIscrittiByAppello()`, `modificaVoto()`, `pubblicaValutazioni()`, `verbalizzaValutazioni()` |
| `StudenteDAO` | `getStudenteInfo()`, `getVotoByAppello()`, `rifiutaVoto()` |
| `CorsoDAO` | `getCorsiByStudente()` |
| `AppelloDAO` | `getAppelliByCorsoAndStudente()`, `isVerbalizzabile()` |
| `VerbaleDAO` | `getVerbaliByDocente()`, `getVerbaleInfo()`, `findDatiVerbale()` |

---

## ⚙️ Technologies Used

| Category | Technologies |
|-----------|---------------|
| Language | Java (Servlets, JSP/HTML) |
| Framework | Jakarta EE / Java EE |
| Database | MySQL |
| Frontend | HTML, CSS, JavaScript (AJAX, DOM manipulation) |
| Architecture | MVC Pattern |
| Security | Session management, servlet filters |
| Build | Maven |

---

## 🚀 How to Run the Project

1. **Clone the repository**
   ```bash
   git clone https://github.com/AndreaPianini/TIW_Project.git
   ```
2. **Import into an IDE** such as Eclipse or IntelliJ as a Maven Web Application.
3. **Configure the database**:
   - Import the SQL schema (if provided)
   - Update connection parameters in the DAO classes.
4. **Deploy on Tomcat (v9 or newer)**.
5. Access the app via:
   - `http://localhost:8080/TIW_Project/`
   - Login as `docente` or `studente`.

---

## 📄 License
This project was developed for academic purposes as part of the *Tecnologie Informatiche per il Web* course at the University of Milan.  
All rights reserved to the authors.
