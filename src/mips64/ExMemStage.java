package mips64;

public class ExMemStage {
    //for all of this reference A.18 --> the map
    //needs store data
    //needs alu data

    PipelineSimulator simulator;
    boolean halted = false, squashed = false, stalled = false;
    boolean shouldWriteback = false;
    int instPC = -1;
    int opcode = 62;
    int aluIntData;
    int storeIntData;
    int destReg;
    int regA, regB;
    
    public ExMemStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public void update() {
        if (!halted && !stalled && !squashed) {
            IdExStage previous = simulator.getIdExStage();
            instPC = previous.instPC;
            opcode = previous.opcode;
            shouldWriteback = previous.shouldWriteback;
            destReg = previous.destReg;
            regA = previous.regA;
            regB = previous.regB;
            storeIntData = forward(regB, previous.regBData);


            switch (Instruction.getNameFromOpcode(opcode)) {
                case "LW":
                case "SW":
                case "ADDI":
                    aluIntData = forward(regA, previous.regAData) + previous.immediate;
                    break;
                case "ADD":
                    aluIntData = forward(regA, previous.regAData) + forward(regB, previous.regBData);
                    break;
                case "SUB":
                    aluIntData = forward(regA, previous.regAData) - forward(regB, previous.regBData);
                    break;
                case "MUL":
                    aluIntData = forward(regA, previous.regAData) * forward(regB, previous.regBData);
                    break;
                case "DIV":
                    aluIntData = forward(regA, previous.regAData) / forward(regB, previous.regBData);
                    break;
                case "AND":
                    aluIntData = forward(regA, previous.regAData) & forward(regB, previous.regBData);
                    break;
                case "OR":
                    aluIntData = forward(regA, previous.regAData) | forward(regB, previous.regBData);
                    break;
                case "XOR":
                    aluIntData = forward(regA, previous.regAData) ^ forward(regB, previous.regBData);
                    break;
                case "ANDI":
                    aluIntData = forward(regA, previous.regAData) & previous.immediate;
                    break;
                case "ORI":
                    aluIntData = forward(regA, previous.regAData) | previous.immediate;
                    break;
                case "XORI":
                    aluIntData = forward(regA, previous.regAData) ^ previous.immediate;
                    break;
                case "SLL":
                    aluIntData = forward(regA, previous.regAData) << previous.immediate;
                    break;
                case "SRL":
                    aluIntData = forward(regA, previous.regAData) >>> previous.immediate;
                    break;
                case "SRA":
                    aluIntData = forward(regA, previous.regAData) >> previous.immediate;
                    break;
                /*case "BEQ":
                case "BNE":
                case "BLTZ":
                case "BLEZ":
                case "BGEZ":
                case "BGTZ":
                case "J":
                case "JAL":
                case "JR":
                case "JALR":*/
                case "NOP":
                case "HALT":
                default:
                    aluIntData = 0;
                    break;
            }
            halted = Instruction.getNameFromOpcode(opcode) == "HALT";
        } else if (stalled) {
            stalled = false;
        } 
    }

    private int forward(int destReg, int defaultData) {
        MemWbStage memWb = simulator.getMemWbStage();
        if (destReg == 0) {
            return 0;
        } else if (memWb.destReg == destReg && memWb.shouldWriteback) {
            return memWb.data;
        } else if (memWb.oldDestReg == destReg && memWb.oldShouldWriteBack) {
            return memWb.oldData;
        } else {
            return defaultData;
        }
    }
}
