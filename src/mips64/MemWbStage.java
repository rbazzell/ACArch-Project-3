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
    boolean halted = false, squashed = false, stalled = false;
    boolean shouldWriteback = false;
    int instPC = -1;
    int opcode = 62;
    int loadIntData;
    int aluIntData;
    int data;
    int destReg;
    int oldDestReg, oldData, oldOpcode = 62;
    boolean oldShouldWriteBack;
    boolean oldSquashed;

    public MemWbStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public boolean isHalted() {
        return halted;
    }

    public void update() {
        if (!halted && !stalled) {
            //TODO: add squashed insts
            //TODO: add oldMEMWB stuff (I think it only needs data and dest reg)
            update_old();

            ExMemStage previous = simulator.getExMemStage();
            squashed = previous.squashed;
            instPC = previous.instPC;
            opcode = previous.opcode;
            destReg = previous.destReg;
            shouldWriteback = previous.shouldWriteback;
            aluIntData = previous.tempAluIntData;
            switch (Instruction.getNameFromOpcode(opcode)) {
                case "LW":
                    // this needs to be in the case statement because of our limited memory size
                    if (!squashed) {
                        loadIntData = simulator.getMemory().getIntDataAtAddr(aluIntData);
                    } else {
                        loadIntData = -1;
                    }
                    break;
                case "SW":
                    if (!squashed) { 
                        simulator.getMemory().setIntDataAtAddr(previous.tempAluIntData, previous.storeIntData); 
                    }
                default:
                    loadIntData = -1;
                    break;
            }

            if (Instruction.getNameFromOpcode(opcode) == "LW") {
                data = loadIntData;
            } else {
                data = aluIntData;
            }
            insert_stall(previous);
            halted = Instruction.getNameFromOpcode(opcode) == "HALT";
        }
        if (stalled) {
            stalled = false;
        }
    }

    private void update_old() {
        oldData = data;
        oldDestReg = destReg;
        oldOpcode = opcode;
        oldShouldWriteBack = shouldWriteback;
        oldSquashed = squashed;

        if (oldShouldWriteBack && !oldSquashed) {
            simulator.getIdExStage().setIntRegister(oldDestReg, oldData);
        }
    }
    
    private void insert_stall(ExMemStage previous) {
        if (Instruction.getNameFromOpcode(opcode) != "LW" || squashed) {
            return;
        }
        IdExStage idEx = simulator.getIdExStage();
        if ((idEx.regA == destReg || idEx.regB == destReg)) {
            //stall
            stalled = true;
            previous.squashed = true;
            simulator.getExMemStage().stalled = true;
            simulator.getIdExStage().stalled = true;
            simulator.getIfIdStage().stalled = true;
            simulator.getPCStage().stalled = true;
        }
    }
}
