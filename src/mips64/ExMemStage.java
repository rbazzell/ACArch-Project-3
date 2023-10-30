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
            storeIntData = previous.regBData;
            switch (Instruction.getNameFromOpcode(opcode)) {
                case "LW":
                case "SW":
                case "ADDI":
                    aluIntData = previous.regAData + previous.immediate;
                    break;
                case "ADD":
                    aluIntData = previous.regAData + previous.regBData;
                    break;
                case "SUB":
                    aluIntData = previous.regAData - previous.regBData;
                    break;
                case "MUL":
                    aluIntData = previous.regAData * previous.regBData;
                    break;
                case "DIV":
                    aluIntData = previous.regAData / previous.regBData;
                    break;
                case "AND":
                    aluIntData = previous.regAData & previous.regBData;
                    break;
                case "OR":
                    aluIntData = previous.regAData | previous.regBData;
                    break;
                case "XOR":
                    aluIntData = previous.regAData ^ previous.regBData;
                    break;
                case "ANDI":
                    aluIntData = previous.regAData & previous.immediate;
                    break;
                case "ORI":
                    aluIntData = previous.regAData | previous.immediate;
                    break;
                case "XORI":
                    aluIntData = previous.regAData ^ previous.immediate;
                    break;
                case "SLL":
                    aluIntData = previous.regAData << previous.immediate;
                    break;
                case "SRL":
                    aluIntData = previous.regAData >>> previous.immediate;
                    break;
                case "SRA":
                    aluIntData = previous.regAData >> previous.immediate;
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
}
