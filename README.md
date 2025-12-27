<h1>📚AI Study Buddy</h1>  
A Smart Learning Companion Powered by "Gemini AI" + "Spring Boot"

AI Study Buddy is an intelligent learning tool that helps students study smarter using advanced AI capabilities.  
Users can sign up, log in, and access multiple AI-powered tools from a clean dashboard UI.

## 🚀 Features

### 🔐 1. User Authentication (Signup + Login)
- Users register using name, email, password  
- Data stored securely in **MongoDB**  
- After login, users are redirected to AI Dashboard

## 🤖 2. AI Tools (Gemini AI Integrated)

### **🧠 General Chat**
- Ask doubts on any topic  
- Gets clean, structured AI-generated answers  
- Markdown responses supported  

### **💻 Code Helper**
- Debug code  
- Ask programming questions  
- Get optimized code suggestions  

### **❓ Quiz Master**
- Generates MCQs or Subjective Questions  
- User selects:
  - topic  
  - question type (MCQ/Subjective)  
  - number of questions  
- Backend uses **Gemini 2.5 Flash** to generate questions  
- Auto-structured JSON output

### **📄 Document Analyzer**
- Upload PDF/Image  
- Extract text  
- Ask queries based on the content  
- Useful for notes summarization, Q&A, and revisions  

## 🎨 UI Flow

### 1️⃣ **Signup Page**
User registers → Data goes to MongoDB  

### 2️⃣ **Login Page**
After login → Redirect to dashboard  

### 3️⃣ **Dashboard / Home Mode**
User sees this screen:

From the left sidebar, user chooses:

- Home  
- General Chat  
- Code Helper  
- Quiz Master  
- Document Analyzer  

## 🛠️ Tech Stack Used

### **Backend**
- Java 21  
- Spring Boot  3.5.7
- REST APIs  
- Gemini AI SDK  
- MongoDB  

### **Frontend**
- HTML, CSS, JavaScript  
- Clean dark UI  
- Responsive layout  

### **Other Tools**
- IntelliJ IDEA  
- Git + GitHub  
- Maven  
## 📸 Screenshots

## 📸 Screenshots

### 🔐 Authentication (Login)
<img src="https://github.com/user-attachments/assets/71d331e1-bc09-4e9b-830c-6c5229edb0c0" alt="Login Page" width="1000"/>

### 🏠 Dashboard
<img src="https://github.com/user-attachments/assets/688267c1-7550-42b5-be24-bfcc43aca8fa" alt="Dashboard" width="1000"/>

### 💻 Code Helper
<img src="https://github.com/user-attachments/assets/0f724e09-60b9-4d29-924a-68262b47611f" alt="Code Helper" width="1000"/>

### 🧠 Quiz Master
<img src="https://github.com/user-attachments/assets/336ebcde-24f3-48ad-b811-b58f87ecb41d" alt="Quiz Master" width="1000"/>

### 📄 Document Analyzer
<img src="https://github.com/user-attachments/assets/7fa4e3da-cbdb-4b9f-a561-efe4ae8d89ed" alt="Document Analyzer" width="1000"/>
