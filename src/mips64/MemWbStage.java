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
    int destReg;
    MemWbStage old;

    public MemWbStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public boolean isHalted() {
        return halted;
    }

    public void update() {
        if (!halted && !stalled && !squashed) {
            //TODO: add squashed insts
            //TODO: add oldMEMWB stuff (I think it only needs data and dest reg)

            ExMemStage previous = simulator.getExMemStage();
            MemWbStage old = new MemWbStage(simulator);
            old.instPC = instPC;
            old.opcode = opcode;
            old.loadIntData = loadIntData;
            old.aluIntData = aluIntData;
            old.destReg = destReg;
            old.shouldWriteback = shouldWriteback;
            
            if (Instruction.getNameFromOpcode(previous.opcode) == "LW" && Instruction.getNameFromOpcode(old.opcode) == "LW" ) {
                simulator.getIdExStage().setIntRegister(destReg, loadIntData);
            } else if (shouldWriteback && Instruction.getNameFromOpcode(opcode) != "LW") {
                simulator.getIdExStage().setIntRegister(destReg, aluIntData);
            }
            
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
                    if (old.destReg == destReg && Instruction.getNameFromOpcode(old.opcode) == "LW") {
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
            halted = Instruction.getNameFromOpcode(opcode) == "HALT";
        } else if (stalled) {
            stalled = false;
        }
    }
}
