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
            aluIntData = previous.aluIntData;
            switch (Instruction.getNameFromOpcode(opcode)) {
                case "LW":
                    // this needs to be in the case statement because of our limited memory size
                    loadIntData = simulator.getMemory().getIntDataAtAddr(aluIntData);
                    //need to check if we have already generated the stall
                    if (oldDestReg == destReg && Instruction.getNameFromOpcode(oldOpcode) == "LW") {
                        loadIntData = -1;
                        return;
                    }
                    //otherwise, stall to wait one extra cycle
                    previous.stalled = true;
                    simulator.getIdExStage().stalled = true;
                    simulator.getIfIdStage().stalled = true;
                    simulator.getPCStage().stalled = true;
                    break;
                case "SW":
                    simulator.getMemory().setIntDataAtAddr(previous.aluIntData, previous.storeIntData);
                default:
                    loadIntData = -1;
                    break;
            }

            if (Instruction.getNameFromOpcode(opcode) == "LW") {
                data = loadIntData;
            } else {
                data = aluIntData;
            }
            
            halted = Instruction.getNameFromOpcode(opcode) == "HALT";
        } else if (stalled) {
            stalled = false;
        } else if (squashed) {

        }
    }

    private void update_old() {
        oldData = data;
        oldDestReg = destReg;
        oldOpcode = opcode;
        oldShouldWriteBack = shouldWriteback;

        if (oldShouldWriteBack) {
            simulator.getIdExStage().setIntRegister(oldDestReg, oldData);
        }
    }
}
