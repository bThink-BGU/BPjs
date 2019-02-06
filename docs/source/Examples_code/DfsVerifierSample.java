
BProgram program = new SingleResourceBProgram("....");         // create a regular b-program
DfsBProgramVerifier vrf = new DfsBProgramVerifier();           // ... and a verifier
vrf.setProgressListener(new BriefPrintDfsVerifierListener());  // add a listener to print progress
VerificationResult res = vrf.verify(program);                  // this might take a while

res.isViolationFound();  // true iff a counter example was found
res.getViolation();      // an Optional<Violation>
res.getViolation().ifPresent( v -> v.getCounterExampleTrace() );
     // ExecutionTrace leading to the violation.
