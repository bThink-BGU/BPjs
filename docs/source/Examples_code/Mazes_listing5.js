SingleResourceBProgram bprog = new SingleResourceBProgram("mazes.js");
bprog.putInGlobalScope("MAZE_NAME", mazeName);
bprog.putInGlobalScope("TARGET_FOUND_EVENT", targetFoundEvent);
DfsBProgramVerifier vfr = new DfsBProgramVerifier();
vfr.setRequirement(new EventNotPresent(targetFoundEvent));
vfr.setVisitedNodeStore(new FullVisitedNodeStore());
VerificationResult res = vfr.verify(bprog);
