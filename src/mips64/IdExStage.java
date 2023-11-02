package mips64;

public class IdExStage {
    int[] registers = new int[32];

    PipelineSimulator simulator;
    Instruction inst;
    boolean halted = false, squashed = false, stalled = false;
    boolean shouldWriteback = false;
    int instPC = -1;
    int opcode = 62;
    int regAData, tempAData;
    int regBData, tempBData;
    int immediate, tempImmediate;
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
        IfIdStage previous = simulator.getIfIdStage();
        if (!halted && !stalled) {
            opcode = previous.opcode;
            squashed = previous.squashed;
            inst = previous.inst;
            if (inst == null) {
                inst = Instruction.getInstructionFromName("NOP");
            } else if (inst.getClass() == RTypeInst.class) {
                regA = ((RTypeInst)inst).getRS();
                regB = ((RTypeInst)inst).getRT();
                tempImmediate = ((RTypeInst)inst).getShamt();
                destReg = ((RTypeInst)inst).getRD();
                shouldWriteback = true;
            } else if (inst.getClass() == ITypeInst.class) {
                regA = ((ITypeInst)inst).getRS();
                regB = ((ITypeInst)inst).getRT();
                tempImmediate = ((ITypeInst)inst).getImmed();
                destReg = ((ITypeInst)inst).getRT();
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
                regA = 0;
                regB = 0;
                tempImmediate = ((JTypeInst)inst).getOffset();
                destReg = 0;
                shouldWriteback = false;
            } else {
                System.err.println("NOT REGISTERING INST TYPE");
            }
            tempAData = registers[regA];
            tempBData = registers[regB];
            
            control(previous); //Mux & comparators for control logic - doesn't execute if squashed!
            
            halted = Instruction.getNameFromOpcode(opcode) == "HALT";
        }
        if (stalled) {
            stalled = false;
        } else {
            regAData = tempAData;
            regBData = tempBData;
            immediate = tempImmediate;
            instPC = previous.instPC;
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

    private void insert_stall() {
        ExMemStage exMem = simulator.getExMemStage();
        if ((exMem.destReg == regA || exMem.destReg == regB) && exMem.shouldWriteback && isBranch(opcode)) {
            //stall
            stalled = true;
            simulator.getIfIdStage().stalled = true;
            simulator.getPCStage().stalled = true;
        }
    }

    private boolean isBranch(int opcode) {
        switch(Instruction.getNameFromOpcode(opcode)) {
            case "BEQ":
            case "BNE":
            case "BLTZ":
            case "BLEZ":
            case "BGEZ":
            case "BGTZ":
            case "J":
            case "JR":
            case "JAL":
            case "JALR":
                return true;
            default:
                return false;
        }
    } 

    private void control(IfIdStage previous) {
        ProgramCounter pc = simulator.getPCStage();
        insert_stall();
        if (squashed || stalled) {
            pc.jumpPC = -1;
            pc.branch = false;
            previous.squashed = false;
            return;
        }
        switch (Instruction.getNameFromOpcode(opcode)) {
            case "BEQ":
                if (forward(regA, tempAData) == forward(regB, tempBData)) {
                    pc.branch = true;
                    pc.jumpPC = pc.getPC() + tempImmediate;
                    previous.squashed = true;
                }
                break;
            case "BNE":
                if (forward(regA, tempAData) != forward(regB, tempBData)) {
                    pc.branch = true;
                    pc.jumpPC = pc.getPC() + tempImmediate;
                    previous.squashed = true;
                }
                break;
            case "BLTZ":
                
                if (forward(regA, tempAData) < 0) {
                    pc.branch = true;
                    pc.jumpPC = pc.getPC() + tempImmediate;
                    previous.squashed = true;
                }
                break;
            case "BLEZ":
                
                if (forward(regA, tempAData) <= 0) {
                    pc.branch = true;
                    pc.jumpPC = pc.getPC() + tempImmediate;
                    previous.squashed = true;
                }
                break;
            case "BGEZ":
                if (forward(regA, tempAData) > 0) {
                    pc.branch = true;
                    pc.jumpPC = pc.getPC() + tempImmediate;
                    previous.squashed = true;
                }
                break;
            case "BGTZ":
                if (forward(regA, tempAData) >= 0) {
                    pc.branch = true;
                    pc.jumpPC = pc.getPC() + tempImmediate;
                    previous.squashed = true;
                }
                break;
            case "J":
                pc.jumpPC = pc.getPC() + tempImmediate;
                pc.branch = true;
                previous.squashed = true;    
                break;
            case "JR":
                pc.jumpPC = forward(regA, tempAData);
                pc.branch = true;
                previous.squashed = true;    
                break;
            case "JAL":
                registers[31] = pc.getPC();
                pc.jumpPC = pc.getPC() + tempImmediate;
                pc.branch = true;
                previous.squashed = true;    
                break;
            case "JALR":
                registers[31] = pc.getPC();
                pc.jumpPC = forward(regA, tempAData);
                pc.branch = true;
                previous.squashed = true;    
                break;
            default:
                pc.jumpPC = -1;
                pc.branch = false;
                previous.squashed = false;
                break;
        }
    }
}
