package mips64;

public class IfIdStage {
  PipelineSimulator simulator;
  int instPC;
  int opcode;


  public IfIdStage(PipelineSimulator sim) {
    simulator = sim;

  }

  public void update() {
    instPC = simulator.getPCStage().getPC();
    opcode = simulator.getMemory().getInstAtAddr(instPC).getOpcode();
    
  }
}
