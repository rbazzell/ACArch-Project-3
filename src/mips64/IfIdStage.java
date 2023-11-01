package mips64;

public class IfIdStage {
    PipelineSimulator simulator;
    Instruction inst = Instruction.getInstructionFromName("NOP");
    boolean halted = false, squashed = false, stalled = false;
    int instPC = -1;
    int opcode = 62;


    public IfIdStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public void update() {
        if (!halted && !stalled) {
            ProgramCounter previous = simulator.getPCStage();
            instPC = previous.getPC();
            inst = simulator.getMemory().getInstAtAddr(instPC);
            opcode = inst.getOpcode();
            halted = Instruction.getNameFromOpcode(opcode) == "HALT";
        } else if (stalled) {
            stalled = false;
        }
    }
}
