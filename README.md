
# Bureaucracy Manager - Part 2 
# Bureaucracy Manager - CEBP PROJECT

This repository contains the __**second**__ part of the Concurrent and Event-Based Programming project.

 ⚠️ The first part of the project, which addresses concurrency issues, can be found at: 🔗[Bureaucracy Manager - Part 1](https://github.com/MalinaNeag/bureaucratic-system)


![ezgif com-animated-gif-maker](https://github.com/user-attachments/assets/ab50ae2a-5bb7-4931-960d-46e5b19fd0f1)

Here’s the updated README in .readme.rm format:


## Overview
The Bureaucracy Manager is a backend system designed to streamline interactions within a bureaucratic environment. Using the context of a public library system, it enables citizens to request services like book loans, enrollment, and fee management while simulating real-world complexities like concurrency and coffee breaks.

---

## Key Features

- **Office Management**:  
  Manages multiple offices with specific functions for document and request processing.  
  Dynamically configurable via a configuration file.

- **Document Dependencies**:  
  Handles dependencies between documents where certain prerequisites must be met.

- **Queue Management**:  
  Utilizes queues for efficient handling of user requests at counters.

- **Concurrency Control**:  
  Ensures data consistency and system reliability during multiple simultaneous interactions.

- **Coffee Break Simulation**:  
  Allows counters to be paused or resumed to simulate temporary closures.

- **Role-Based Access Control (RBAC)**:  
  - **Admins**: Manage configurations, books, citizens, and fees.  
  - **Citizens**: Submit loan requests and pay fees.

---

## API Endpoints

### Admin APIs
- **Configuration**:
  - `POST /api/admin/config`  
    Configure counters dynamically using a simple text format.

- **Counter Management**:
  - `POST /api/admin/pause-counter`  
    Pause a counter for coffee breaks.
  - `POST /api/admin/resume-counter`  
    Resume a paused counter.

- **Book Management**:
  - `POST /api/admin/add-book`  
    Add a new book to the library.
  - `PUT /api/admin/update-book`  
    Update book details.
  - `DELETE /api/admin/delete-book/{bookId}`  
    Delete a book.

- **Citizen Management**:
  - `POST /api/admin/add-citizen`  
    Add a new citizen.
  - `PUT /api/admin/update-citizen`  
    Update citizen details.
  - `DELETE /api/admin/delete-citizen/{citizenId}`  
    Delete a citizen.

- **Fee Management**:
  - `POST /api/admin/add-fee`  
    Add a fee.
  - `PUT /api/admin/update-fee`  
    Update fee details.
  - `DELETE /api/admin/delete-fee/{feeId}`  
    Delete a fee.

### Citizen APIs
- **Enrollment**:
  - `POST /api/citizens/enroll`  
    Enroll a new citizen.

- **Loan Request**:
  - `POST /api/citizens/loan-request`  
    Submit a book loan request.

- **Fee Management**:
  - `GET /api/citizens/fees/{borrowId}`  
    Get fee details by borrow ID.
  - `POST /api/citizens/mark-as-paid/{borrowId}`  
    Mark a fee as paid.

---

## Departments

### Enrollment Department
**Counters**: 1  
Handles citizen registration, verifying identities, and managing membership details.

### Book Loaning Department
**Counters**: 2  
Manages book loans, returns, and penalties. Includes queue management and counter pauses for coffee breaks.

### Library Administration Department
**Counters**: 2  
Oversees library operations, including book availability, loan histories, and administrative updates.

---

## Concurrency Issues and Solutions

- **Simultaneous Book Loan Requests**  
  **Problem**: Multiple requests for the same book could lead to inconsistencies.  
  **Solution**: A `ConcurrentHashMap` is used to lock individual books, ensuring only one thread can modify the book's state at a time.

![image](https://github.com/user-attachments/assets/55afa82a-c6e0-43b5-a993-38f8d433e314)

- **Concurrent Citizen Enrollment**  
  **Problem**: Duplicate records due to simultaneous enrollment requests.  
  **Solution**: Synchronized methods ensure atomic operations for checking and updating citizen records.

- **Updating Book Database**  
  **Problem**: Conflicts from concurrent admin updates.  
  **Solution**: A write lock ensures no simultaneous updates.

   ![image](https://github.com/user-attachments/assets/48bed96b-3cb4-479b-bf22-94a2f677d70c)


- **Queue Management for Loan Requests**  
  **Problem**: Concurrent queue operations may result in lost updates or inconsistencies.  
  **Solution**: A `LinkedBlockingQueue` is used for thread-safe addition and removal.

- **Pausing and Resuming Counters**  
  **Problem**: Improper synchronization could lead to deadlocks.  
  **Solution**: Boolean flags with synchronized blocks ensure safe transitions between paused and active states.

---

## System Configuration

### Configuration Format
The system is configured using a simple text file (`config/config.txt`) in the following format:

counters=number

### Example

counters=2

This specifies the number of counters available in the system. Changes to the configuration file are dynamically loaded.

---

## Conclusion
The Bureaucracy Manager backend is a robust system for managing library operations in a simulated bureaucratic environment. Its focus on concurrency, realistic counter management, and role-based access control makes it an ideal solution for simulating complex workflows in public services.