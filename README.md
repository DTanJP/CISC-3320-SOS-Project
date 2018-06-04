# CISC-3320-SOS-Project
A operating system simulation program
The project is a simulation of an operating system. Your program works together with an existing program (SOS) that simulates a job stream. SOS feeds your program a series of jobs and job requests; like a real operating system, your program must field those requests and handle the life cycle of each of the jobs, from system entry to termination.

This project features multiple schedulers, but it uses the multilevel feedback queue as it produces the best result.
There is another unreleased version of this where the multilevel feedback queue is optimized to complete an additional 2 jobs. Producing a result of 451/458 jobs completed. However that version only features the multilevel feedback queue scheduler.

Scheduler name  :: Completed jobs :: Total jobs
====================================================
First Come First Serve: Completes 159/209 jobs.  
Multilevel Queue: Completes 325/375 jobs.  
Shortest Remaining Time First: Completes 431/458 jobs.  
Round Robin: Completes 439/458 jobs.  
Priority Scheduling: Completes 441/458 jobs.  
Shortest Job First: Completes 447/458 jobs.  
Multilevel Feedback Queue: Completes 449/458 jobs.  

Reasons why you're browsing this
====================================================
1. You're just browsing around and stumbled upon this. Feel free to look around.
2. You're actually doing this project and need some tips/hints on getting started. Feel free to look around.
