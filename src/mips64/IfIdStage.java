package mips64;

public class IfIdStage {
  PipelineSimulator simulator;
  int instPC;
  int opcode;
  Instruction inst;


  public IfIdStage(PipelineSimulator sim) {
    simulator = sim;

  }

  public void update() {
    instPC = simulator.getPCStage().getPC();
    opcode = simulator.getMemory().getInstAtAddr(instPC).getOpcode();
    
  }
}
