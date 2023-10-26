package mips64;

public class MemWbStage {
    //needs does wb
    //needs dest reg
    //needs alu data
    //needs ld data
    //needs is load
    //needs is squashed
    //needs wb data

    //probably add an old WB reg in here too for forwarding purposes <-- do this (otherwise Dr. G will say its a bad emulation)
    PipelineSimulator simulator;
    boolean halted;
    boolean shouldWriteback = false;
    int instPC;
    int opcode;
    int aluIntData;
    int loadIntData;

    public MemWbStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public boolean isHalted() {
        return halted;
    }

    public void update() {
    }
}
