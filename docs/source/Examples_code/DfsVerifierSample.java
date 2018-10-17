
BProgram program = new SingleResourceBProgram("....");         // create a regular b-program
DfsBProgramVerifier vrf = new DfsBProgramVerifier();           // ... and a verifier
vrf.setProgressListener(new BriefPrintDfsVerifierListener());  // add a listener to print progress
VerificationResult res = vrf.verify(program);                  // this might take a while

res.isCounterExampleFound();  // true iff a counter example was found
res.getViolationType();       // Failed assertion, or a deadlock
res.getCounterExampleTrace(); // trace of events leading to the violation.
