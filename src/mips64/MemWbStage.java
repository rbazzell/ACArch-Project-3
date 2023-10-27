package mips64;

public class MemWbStage {
    //needs does wb - done
    //needs dest reg - from instruction
    //needs alu data - done
    //needs ld data - done
    //needs is load - done
    //needs is squashed - done
    //needs wb data - done

    //probably add an old WB reg in here too for forwarding purposes <-- do this (otherwise Dr. G will say its a bad emulation)
    PipelineSimulator simulator;
    boolean halted, squashed;
    boolean shouldWriteback = false;
    int destReg;
    int instPC;
    int opcode;
    int aluIntData;
    int loadIntData;
    Instruction inst;
    MemWbStage oldMemWb;

    public MemWbStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public boolean isHalted() {
        return halted;
    }

    public void update() {
        //TODO: add squashed insts
        //TODO: add oldMEMWB stuff (I think it only needs data and dest reg)
        inst = simulator.getExMemStage().inst;
        
        int data;
        if (isLoad()) {
            data = loadIntData;
        } else {
            data = aluIntData;
        }

        if (isWriteBack()) {
            simulator.setRegister(destReg, data);
        }

    }

    private boolean isLoad() {
        return Instruction.getNameFromOpcode(opcode) == "LW";
    }

    private boolean isWriteBack() {
        switch (Instruction.getNameFromOpcode(opcode)) {
            case "LW":
            case "ADD":
            case "ADDI":
            case "SUB":
            case "MUL":
            case "DIV":
            case "AND":
            case "ANDI":
            case "OR":
            case "ORI":
            case "XOR":
            case "XORI":
            case "SLL":
            case "SRL":
            case "SRA":
                shouldWriteback = true;
            default:
                shouldWriteback = false;
        }
        return shouldWriteback;
    }
}
