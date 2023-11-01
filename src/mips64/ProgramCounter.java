package mips64;

public class ProgramCounter {

    PipelineSimulator simulator;
    int pc;
    boolean stalled = false, branch = false;
    int jumpPC;

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
        if (!stalled && !branch) {
            this.incrPC();
        } else if (stalled) {
            stalled = false;
        } else if (branch) {
            setPC(jumpPC);
            branch = false;
        }
    }
}
