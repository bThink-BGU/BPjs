===============
Verification
===============

BPjs programs can be verified to conform to a formal requirement. For example, it is possible to verify a b-program cannot get to a deadlock, or that a certain event is never selected. If, during verification, the verifier finds an example for violating the requirement being verified, it returns the trace of events that lead to the illegal situation.

Sample verification code can be found in the test code, in package ``il.ac.bgu.cs.bp.bpjs.verification.examples``.

.. note:: This description is a stub. Longer, more detailed description will be added once the first BPjs paper is out (already submitted, BTW).