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
    int tempAluIntData, aluIntData;
    int tempStoreIntData, storeIntData;
    int shamt;
    int destReg;
    int regA, regB;
    
    public ExMemStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public void update() {
        IdExStage previous = simulator.getIdExStage();
        if (!halted && !stalled) {
            instPC = previous.instPC;
            opcode = previous.opcode;
            squashed = previous.squashed;
            shouldWriteback = previous.shouldWriteback;
            destReg = previous.destReg;
            regA = previous.regA;
            regB = previous.regB;
            tempStoreIntData = forward(regB, previous.regBData);
            switch (Instruction.getNameFromOpcode(opcode)) {
                case "LW":
                case "SW":
                case "ADDI":
                    tempAluIntData = forward(regA, previous.regAData) + previous.immediate;
                    break;
                case "ADD":
                    tempAluIntData = forward(regA, previous.regAData) + forward(regB, previous.regBData);
                    break;
                case "SUB":
                    tempAluIntData = forward(regA, previous.regAData) - forward(regB, previous.regBData);
                    break;
                case "MUL":
                    tempAluIntData = forward(regA, previous.regAData) * forward(regB, previous.regBData);
                    break;
                case "DIV":
                    tempAluIntData = forward(regA, previous.regAData) / forward(regB, previous.regBData);
                    break;
                case "AND":
                    tempAluIntData = forward(regA, previous.regAData) & forward(regB, previous.regBData);
                    break;
                case "OR":
                    tempAluIntData = forward(regA, previous.regAData) | forward(regB, previous.regBData);
                    break;
                case "XOR":
                    tempAluIntData = forward(regA, previous.regAData) ^ forward(regB, previous.regBData);
                    break;
                case "ANDI":
                    tempAluIntData = forward(regA, previous.regAData) & previous.immediate;
                    break;
                case "ORI":
                    tempAluIntData = forward(regA, previous.regAData) | previous.immediate;
                    break;
                case "XORI":
                    tempAluIntData = forward(regA, previous.regAData) ^ previous.immediate;
                    break;
                case "SLL":
                    tempAluIntData = forward(regA, previous.regAData) << previous.shamt;
                    break;
                case "SRL":
                    tempAluIntData = forward(regA, previous.regAData) >>> previous.shamt;
                    break;
                case "SRA":
                    tempAluIntData = forward(regA, previous.regAData) >> previous.shamt;
                    break;
                default:
                    tempAluIntData = 0;
                    break;
            }
            halted = Instruction.getNameFromOpcode(opcode) == "HALT" && !squashed;
        }
        if (stalled) {
            stalled = false;
        } else {
            aluIntData = tempAluIntData;
            storeIntData = tempStoreIntData;
        }
    }

    private int forward(int destReg, int defaultData) {
        MemWbStage memWb = simulator.getMemWbStage();
        if (destReg == 0) {
            return 0;
        } else if (memWb.destReg == destReg && memWb.shouldWriteback && !memWb.squashed) {
            return memWb.data;
        } else if (memWb.oldDestReg == destReg && memWb.oldShouldWriteBack && !memWb.oldSquashed) {
            return memWb.oldData;
        } else if (simulator.idEx.getIntRegister(destReg) != defaultData) {
            return simulator.idEx.getIntRegister(destReg);
        } else {
            return defaultData;
        }
    }

}
