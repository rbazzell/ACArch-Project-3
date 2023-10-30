package mips64;

public class IdExStage {
    int[] registers = new int[32];

    PipelineSimulator simulator;
    Instruction inst;
    boolean halted = false, squashed = false, stalled = false;
    boolean shouldWriteback = false;
    int instPC = -1;
    int opcode = 62;
    int regAData;
    int regBData;
    int immediate;
    int destReg;

    public IdExStage(PipelineSimulator sim) {
        simulator = sim;
        for (int i = 0; i < 32; i++) {
            registers[i] = 0;
        }
    }

    public int getIntRegister(int regNum) {
        return registers[regNum];
    }

    public void setIntRegister(int regNum, int data) {
        if (regNum > 0 && regNum < 32) {
            registers[regNum] = data;
        }
    }

    public void update() {
        int regA = 0, regB = 0;
        if (!halted && !stalled && !squashed) {
            IfIdStage previous = simulator.getIfIdStage();
            instPC = previous.instPC;
            opcode = previous.opcode;
            inst = previous.inst;
            if (inst == null) {
                inst = Instruction.getInstructionFromName("NOP");
            } else if (inst.getClass() == RTypeInst.class) {
                regA = ((RTypeInst)inst).getRS();
                regB = ((RTypeInst)inst).getRT();
                immediate = ((RTypeInst)inst).shamt;
                destReg = ((RTypeInst)inst).rd;
                shouldWriteback = true;
            } else if (inst.getClass() == ITypeInst.class) {
                regA = ((ITypeInst)inst).getRS();
                regB = ((ITypeInst)inst).getRT();
                immediate = ((ITypeInst)inst).immed;
                destReg = ((ITypeInst)inst).rt;
                switch (Instruction.getNameFromOpcode(opcode)) {
                    case "LW":
                    case "ADDI":
                    case "ANDI":
                    case "ORI":
                    case "XORI":
                        shouldWriteback = true;
                        break;
                    default:
                        shouldWriteback = false;
                }
            } else if (inst.getClass() == JTypeInst.class) { //JTypeInst.class
                immediate = ((JTypeInst)inst).offset;
                destReg = 0;
                shouldWriteback = false;
            } else {
                System.err.println("NOT REGISTERING INST TYPE");
            }

            //testing if we need to forward
            MemWbStage memWb = simulator.getMemWbStage();
            if (regA == memWb.destReg && memWb.shouldWriteback) {
                regAData = memWb.aluIntData;
            } else {
                regAData = registers[regA];
            }
            if (regB == memWb.destReg && memWb.shouldWriteback) {
                regBData = memWb.aluIntData;
            } else {
                regBData = registers[regB];
            }
            halted = Instruction.getNameFromOpcode(opcode) == "HALT";
        } else if (stalled) {
            stalled = false;
        }
    }
}
