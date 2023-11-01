package mips64;

public class ProgramCounter {

    PipelineSimulator simulator;
    int pc;
    boolean stalled = false;

    public ProgramCounter(PipelineSimulator sim) {
        pc = 0;
        simulator = sim;
    }

    public int getPC () {
        return pc;
    }

    public void setPC (int newPC) {
        pc = newPC;
    }

    public void incrPC () {
        pc += 4;
    }

    public void update() {
        IdExStage idEx = simulator.getIdExStage();
        if (!stalled) {
            this.incrPC();
            if (idEx.jumpPC != -1) {
                setPC(idEx.jumpPC);
            }
        } else if (stalled) {
            stalled = false;
        }
    }
}
