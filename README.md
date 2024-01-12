# ptri 🌱

### Objective: To create an accessible and informative visual that aids students in exploring classes they could take. To be achieved using a flow chart-style topologically-ordered diagram that shows what courses a student can take based on course prerequisites.
Something I noticed was that services such as Degree Audit allowed us to see which courses we *had* to take, but it was sometimes difficult to cleanly see which courses we *could* take. This project aims to help students consider more diverse options when selecting classes, finding courses that may have been hard to stumble across while looking manually.

#### Backend: SpringBoot API written in Java
- Utilizes Johns Hopkins SIS API to gather course data. After receiving, my backend parses this data for the parts that are most important, and also make the "prerequisite connections".
- Try for yourself (after deployment, though 😊)! Endpoints:
    - /courses: Returns all processed courses in JSON format
    - (I will create more soon probably as the project goes on, but for now /courses is the only one being used)

#### Frontend:
- Goal is to create a topological graph that can show prerequisite relationships. Progress so far:
![image](https://github.com/spencerckhuang/ptri/assets/112358919/bdeb3fd9-8305-4608-84ef-f39ad33f6a6f)
- Great start and was basically what I was visualizing going into this whole project. Some things that are left to do:
  1. Resolve minor course-specific issues (e.g. courses on the far right should be in different columns -- arrows should only point forwards)
  2. Improve styling of course nodes (very basic CSS used for now, just for proof-of-concept)
  3. *Include interactivity: Allow users to select courses they have taken, and show the user what courses they can take based off of the clicked courses*
 
  
#### Instructions for use: Backend
1. If using VSCode: Install [Extention Pack for Java by Microsoft](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
2. Using the Java Project Manager in the left sidebar, navigate to CourseAPI.java:

![image](https://github.com/spencerckhuang/ptri/assets/112358919/2eb94993-955e-46cf-9619-871c907ce9e6)

3. Hit "run" right above the main method:
   
![image](https://github.com/spencerckhuang/ptri/assets/112358919/50701fd3-7e8c-42a3-b726-7e15a9e57a2b)

#### Instructions for use: Frontend
Open a terminal and utilize the following commands once in the ptri directory:
```
cd ptri-frontend
npm run dev
```
Note that the frontend will not render anything without the backend running concurrently.
