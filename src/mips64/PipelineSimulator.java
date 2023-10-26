package mips64;

import java.io.*;
import java.util.Scanner;

public class PipelineSimulator {

  private enum Command {
    EXIT, LOAD, RESET, RUN, STEP, SETBREAK, RUNBREAK, SHOWMEM, QUIET, UNUSED} ;

    MemoryModel memory;
    boolean isMemoryLoaded;

    ProgramCounter pc;
    IfIdStage ifId;
    IdExStage idEx;
    ExMemStage exMem;
    MemWbStage memWb;

    int register[] = new int[32];

    int breakAddress = -1;
    boolean quietMode = true;
    int instExec = 0;
   
    public PipelineSimulator() {
      this("");
      reset();
      try {
        jbInit();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
       
    public PipelineSimulator(String fileName) {

      if (fileName != "") {
        memory = new MemoryModel(fileName);
        isMemoryLoaded = true;
      }
      else {
        memory = null;
        isMemoryLoaded = false;
      }
      reset();
    }

    public MemoryModel getMemory() {
      return memory;
    }

    public ProgramCounter getPCStage() {
      return pc;
    }

    public IfIdStage getIfIdStage() {
      return ifId;
    }

    public IdExStage getIdExStage() {
      return idEx;
    }

    public ExMemStage getExMemStage() {
      return exMem;
    }

    public MemWbStage getMemWbStage() {
      return memWb;
    }

    public boolean getQuiet () {
      return quietMode;
    }

    private void simulate() {

      Command command = Command.UNUSED;

      while (command != command.EXIT) {
        printMenu();
        command = getCommand();

        switch (command) {
          case EXIT:
            break;
          case LOAD:
            loadMemory();
            break;
          case RESET:
            reset();
            System.out.println("Simulation reset");
            break;
          case RUN:
            run();
            break;
          case STEP:
            step();
            break;
          case SETBREAK:
            setBreak();
            break;
          case RUNBREAK:
            runToBreak();
            break;
          case SHOWMEM:
            showMemory();
            break;
          case QUIET:
            quietMode = ! quietMode;
          default:
            command = Command.UNUSED;
        }
      }
      System.out.println("Ya'll come back real soon, ya hear!");

    }

    private void printMenu() {
      System.out.println();
      System.out.println();
      System.out.println();
      System.out.println("What would you like to do?");
      System.out.println("   0. Exit");
      System.out.println("   1. Load");
      if (isMemoryLoaded) {
        System.out.println("   2. Reset");
        System.out.println("   3. Run");
        System.out.println("   4. Single Step");
        System.out.println("   5. Set Breakpoint");
        System.out.println("   6. Run To Breakpoint");
        System.out.println("   7. Show Memory");
        if (quietMode) {
          System.out.println("   8. Go to Verbose Mode");
        }
        else {
          System.out.println("   8. Go to Quite Mode");
        }
        System.out.println();
        System.out.print("Please enter choice (0-8)  ");
      }
      else {
        System.out.println();
        System.out.print("Please enter choice (0-1)  ");
      }
    }

    private Command getCommand() {
      Command comm = Command.UNUSED;
      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isr);
      boolean goodInput = false;
      while (!goodInput) {
        int maxInputValue = 8;
        if (!isMemoryLoaded) {
          maxInputValue = 1;
        }

        try {
          String inString = br.readLine();
          int inChar = Integer.parseInt(inString);
          if (inChar >= 0 && inChar <= maxInputValue) {
//          System.out.println("length = "+Command.values().length+ "   " + Command.values()[3]);
            comm = Command.values()[inChar];
            goodInput = true;
          }
          else {
            System.out.println();
            System.out.println("Bad input; try again");
            System.out.println();

          }
        }

        catch (Exception ioe) {
          System.out.println();
          System.out.println("Bad input; try again");
          System.out.println();

        }
      }
      return comm;
    }

    private void reset() {
      pc = new ProgramCounter(this);
      ifId = new IfIdStage(this);
      idEx = new IdExStage(this);
      exMem = new ExMemStage(this);
      memWb = new MemWbStage(this);
      pc.setPC(0);
      instExec = 0;

      for (int i = 0; i < register.length; i ++) {
        register[i] = 0;
      }
    }

    public void loadMemory() {
      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isr);

      try {
        System.out.print(
            "Please enter filename to load (include .mo suffix) ");
        String fileName = br.readLine();
        
        memory = new MemoryModel(fileName);
        isMemoryLoaded = true;
        //reset();
      }
      catch (Exception mioe) {
        System.out.println("Problem opening file");
        isMemoryLoaded = false;
      }
    }

    public void loadMemoryGUI(String fileName) {

      try {
        

        memory = new MemoryModel(fileName);
        isMemoryLoaded = true;
        //reset();
      }
      catch (Exception mioe) {
        System.out.println("Problem opening file");
        isMemoryLoaded = false;
      }
    }

    private void showMemory() {
      this.getMemory().printObjectCode();
    }

    public void dumpStatus() {
      for (int row = 0; row < 8; row++) {
        System.out.print("R" + (row * 4) + "\t");
        for (int reg = 0; reg < 4; reg++) {
          int regNum = row * 4 + reg;
          System.out.print(idEx.getIntRegister(regNum) + "  ");
        }
        System.out.println();
      }
//      System.out.println();
//      for (int row = 0; row < 8; row++) {
//        System.out.print("FR" + (row * 4) + "\t");
//        for (int reg = 0; reg < 4; reg++) {
//          int regNum = row * 4 + reg;
//          System.out.print(idEx.getFloatRegister(regNum) + "  ");
//        }
//        System.out.println();
//      }
//      System.out.println();
//      System.out.println("Next oper to WB is " + memWb.instPC);
    }

    public void run() {
      
      while (!memWb.isHalted()) {
        step();
      }
      if (quietMode) {
        dumpStatus();
      }
      System.out.println("Total instructions executed = " + instExec);
    }

    public void step() {
      if (!memWb.isHalted()) {
        // work backwards
        memWb.update();
        exMem.update();
        idEx.update();
        ifId.update();
        pc.update();
        instExec++;

        if (! quietMode) {
          dumpStatus();
        }
      }
    }

    private void setBreak() {
      // this routine asks for a breakpoint, which will be an address in the
      // program (a multiple of 4)
      Scanner myScanner;
      int address = -1;

      boolean goodAddress = false;
      while (!goodAddress) {
        myScanner = new Scanner(System.in);
        System.out.print(
            "Please enter an address for breakpoint (multiple of 4)  ");

        if (myScanner.hasNextInt()) {
          address = myScanner.nextInt();
          if (address % 4 == 0) {
            goodAddress = true;
            breakAddress = address;
          }
          else {
            System.out.println(
                "A breakpoint address must be a multiple of 4");
            System.out.print(
                "Please enter an address for breakpoint (multiple of 4)  ");
          }
        }
        else {
          System.out.println(
                "You must enter an integer address");
            System.out.print(
                "Please enter an address for breakpoint (multiple of 4)  ");
        }

      }
    }

    private void runToBreak () {
        // always step once, to allow you to run when sitting on breakpoint
      step();

      while (!memWb.isHalted() && pc.getPC() != breakAddress) {
        step();
      }
      if (memWb.isHalted()) {
        System.out.println("Program halted prior to hitting breakpoint");
      }
      else if (pc.getPC() == breakAddress) {
        System.out.println("Hit breakpoint at address" + breakAddress);
      }
      else {
        throw new MIPSException ("I'm so confused in runToBreak");
      }

    }

    public static void main(String[] args) {
      PipelineSimulator sim = new PipelineSimulator();
//    sim.getMemory().printObjectCode();
      sim.simulate();
    }

    private void jbInit() throws Exception {
    }

  }
