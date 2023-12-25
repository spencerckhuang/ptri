# ptri 🌱

### Objective: To create an accessible and informative visual that aids students in exploring classes they could take. To be achieved using a flow chart-style topologically-ordered diagram that shows what courses a student can take based on course prerequisites.
Something I noticed was that services such as Degree Audit allowed us to see which courses we *had* to take, but it was sometimes difficult to cleanly see which courses we *could* take. This project aims to help students consider more diverse options when selecting classes, finding courses that may have been hard to stumble across while looking manually.

#### Backend: SpringBoot API written in Java
- Utilizes Johns Hopkins SIS API to gather course data. After receiving, my backend parses this data for the parts that are most important, and also make the "prerequisite connections".
- Try for yourself (after deployment, though 😊)! Endpoints:
    - /courses: Returns all processed courses in JSON format
    - (I will create more soon probably 🙂)

#### Frontend: (havent started yet but will probably be using React Flow to achieve visual effects)
- Goal is to create a topological graph that can show prerequisite relationships
  
