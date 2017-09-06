# BruteForce-Password-cracker
Distributed password cracker with load balancing and fault tolerance.
The server kept state of the amount of work completed by worker nodes, so only 
work that remains is reassigned in case of failure.
