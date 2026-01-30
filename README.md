# **StoryTeller - Installation and User Guide**

## **1. Project Installation**

### **Prerequisites**

* Python 3.10+ or 3.11 ( Spacy droken in 3.12 and higher )
* Node.js 18+ and npm
* Git (optional)

### **Backend (FastAPI + SQLite)**

1. Clone the repository or download the files.

2. Create a virtual environment and activate it:

```
py -m venv .venv
.venv\Scripts\Activate.ps1
```

3. Install dependencies:

place you in backend folder

```
pip install -r requirements.txt
```

4. Install the French spaCy model (required for the AI Assistant):

```
python -m spacy download fr_core_news_md
```

5. Initialize the database:
   (The creation of tables and loading of test data are now automatic at server startup if the DB is empty.)

```
# No 'flask db' command is required with the current SQLAlchemy configuration.
# Tables are created on the first server launch.
```

6. **Start the backend server:**

```
python run.py
```

The backend will be available at [http://127.0.0.1:5000/](http://127.0.0.1:5000/).

### **Frontend (React + Vite)**

1. Go to the frontend/ folder:

```
cd frontend
```

2. Install dependencies:

```
npm install
```

3. Start the React application:

```
npm run dev
```

4. Open your browser at the URL shown by Vite (e.g. [http://localhost:5173/](http://localhost:5173/)).

## **2. Usage**

### **2.1 Navigation**

The navigation bar lets you switch between the panels of the selected novel:

* **Story**: Synopsis, Back cover.
* **Manuscript**: Chapter editing.
* **Characters**: Detailed profiles.
* **Locations**: Key places in the story.
* **Lore**: Encyclopedic entries (factions, magic, objects).
* **Timeline**: Ordered events.
* **AI Assistant**: Consistency assistance tools.

### **2.2 Managing Sheets (Characters, Locations, Lore)**

* **Add / Edit:** Fill in the form fields and click Save or Update.
* **Select:** Click an item in the list to load its edit form.
* **Delete:** Click Delete on the desired item.

### **2.3 Timeline**

* Add an event and associate it with a date and characters/locations.
* The list is displayed in chronological order.

### **2.4 Story / Manuscript**

* The **Story** panel lets you edit the novel title, synopsis, and blurb.
* The **Manuscript** panel provides an enhanced Markdown editor for chapters.
* **Saving:** Automatic every 10 seconds (or via the Save button).
* **Export:** Markdown or PDF export via the buttons in the Story panel.

### **2.5 AI Assistant**

* **Find character links:** Suggests relationships based on the data.
* **Check timeline:** Detects temporal inconsistencies (age, birth dates vs events).
* The **Manuscript** panel also uses AI for quick or detailed text analysis.

## **3. Screenshots (example)**

### **Main navigation**

![img.png](public/img.png)

### **Character list**

![img\_1.png](public/img_1.png)

### **Editing a manuscript chapter**

![img\_2.png](public/img_2.png)

### **AI Assistant**

![img\_3.png](public/img_3.png)
