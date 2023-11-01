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
    int regA = 0, regB = 0;

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
        if (!halted && !stalled) {
            IfIdStage previous = simulator.getIfIdStage();
            squashed = previous.squashed;
            instPC = previous.instPC;
            opcode = previous.opcode;
            inst = previous.inst;
            if (inst == null) {
                inst = Instruction.getInstructionFromName("NOP");
            } else if (inst.getClass() == RTypeInst.class) {
                regA = ((RTypeInst)inst).getRS();
                regB = ((RTypeInst)inst).getRT();
                immediate = ((RTypeInst)inst).getShamt();
                destReg = ((RTypeInst)inst).getRD();
                shouldWriteback = true;
            } else if (inst.getClass() == ITypeInst.class) {
                regA = ((ITypeInst)inst).getRS();
                regB = ((ITypeInst)inst).getRT();
                immediate = ((ITypeInst)inst).getImmed();
                destReg = ((ITypeInst)inst).getRT();
                switch (Instruction.getNameFromOpcode(opcode)) {
                    case "LW":
                    case "ADDI":
                    case "ANDI":
                    case "ORI":
                    case "XORI":
                        shouldWriteback = true;
                        break;
                    case "JR":
                    case "JALR":
                    default:
                        shouldWriteback = false;
                }
            } else if (inst.getClass() == JTypeInst.class) { //JTypeInst.class
                regA = 0;
                regB = 0;
                immediate = ((JTypeInst)inst).getOffset();
                destReg = 0;
                shouldWriteback = false;
            } else {
                System.err.println("NOT REGISTERING INST TYPE");
            }

            regAData = registers[regA];
            regBData = registers[regB];
            ProgramCounter pc = simulator.getPCStage();
            switch (Instruction.getNameFromOpcode(opcode)) {
                /*case "BEQ":
                    previous.squashed = true;
                    stall();
                    if (forward(regA, regAData) == forward(regB, regBData)) {
                        pc.branch = true;
                        pc.jumpPC = pc.getPC() + immediate;
                    }
                case "BNE":
                    previous.squashed = true;
                    stall();
                    if (forward(regA, regAData) != forward(regB, regBData)) {
                        pc.branch = true;
                        pc.jumpPC = pc.getPC() + immediate;
                    }
                case "BLTZ":
                    previous.squashed = true;
                    stall();
                    if (forward(regA, regAData) < 0) {
                        pc.branch = true;
                        pc.jumpPC = pc.getPC() + immediate;
                    }
                case "BLEZ":
                    previous.squashed = true;
                    stall();
                    if (forward(regA, regAData) <= 0) {
                        pc.branch = true;
                        pc.jumpPC = pc.getPC() + immediate;
                    }
                case "BGEZ":
                    previous.squashed = true;
                    stall();
                    if (forward(regA, regAData) > 0) {
                        pc.branch = true;
                        pc.jumpPC = pc.getPC() + immediate;
                    }
                case "BGTZ":
                    previous.squashed = true;
                    stall();
                    if (forward(regA, regAData) >= 0) {
                        pc.branch = true;
                        pc.jumpPC = pc.getPC() + immediate;
                    }*/
                case "J":
                    pc.jumpPC = pc.getPC() + immediate;
                    pc.branch = true;
                    previous.squashed = true;    
                    break;
                case "JR":
                    pc.jumpPC = forward(regA, regAData);
                    pc.branch = true;
                    previous.squashed = true;    
                    break;
                case "JAL":
                    registers[31] = pc.getPC();
                    pc.jumpPC = pc.getPC() + immediate;
                    pc.branch = true;
                    previous.squashed = true;    
                    break;
                case "JALR":
                    registers[31] = pc.getPC();
                    pc.jumpPC = forward(regA, regAData);
                    pc.branch = true;
                    previous.squashed = true;    
                    break;
                default:
                    pc.jumpPC = -1;
                    pc.branch = false;
                    previous.squashed = false;
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
        } else if (memWb.destReg == destReg && memWb.shouldWriteback && !memWb.squashed) {
            return memWb.data;
        } else if (memWb.oldDestReg == destReg && memWb.oldShouldWriteBack && !memWb.oldSquashed) {
            return memWb.oldData;
        } else {
            return defaultData;
        }
        
    }

    private void stall() {
        simulator.getIfIdStage().stalled = true;
        simulator.getPCStage().stalled = true;
    }
}
