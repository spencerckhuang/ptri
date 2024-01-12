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
  
