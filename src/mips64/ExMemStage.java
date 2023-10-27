package mips64;

public class ExMemStage {
    //for all of this reference A.18 --> the map
    //needs store data
    //needs alu data


    PipelineSimulator simulator;
    boolean shouldWriteback = false;
    int instPC;
    int opcode;
    int aluIntData;
    int storeIntData;
    Instruction inst;

    public ExMemStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public void update() {
    }
}
