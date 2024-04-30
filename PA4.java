import soot.PackManager;
import soot.Transform;


public class PA4 {
    public static void main(String[] args) {

        String classPath = "."; // Assuming this is the correct path where your classes are
        String outputDirJimple = "./output/jimple"; // Directory for Jimple output
        String dir = "./testcase"; // Directory containing source classes to analyze

        // First run to generate Jimple files
        String[] sootArgsJimple = {
            "-cp", classPath,
            "-pp",
            "-w",
            "-f", "J", // Output format set to Jimple
            "-keep-line-number",
            "-main-class", "Test5",
            "-process-dir", dir,
            "-output-dir", outputDirJimple,
            "-allow-phantom-refs",
            "-p", "cg.spark", "enabled:true",
            "-p", "cg.spark", "on-fly-cg:false"
        };



        // Create transformer for analysis
        AnalysisTransformer analysisTransformer = new AnalysisTransformer();

        // Add transformer to appropriate pack in PackManager; PackManager will run all packs when soot.Main.main is called
        PackManager.v().getPack("jtp").add(new Transform("jtp.dfa", analysisTransformer));

        
        
        soot.Main.main(sootArgsJimple);
        
        // analysisTransformer.printVariablePointsToInfo();


    }
}

// import soot.PackManager;
// import soot.Transform;
// import soot.Main;

// public class PA4 {
//     public static void main(String[] args) {
//         // Setting up and starting a thread that can be stopped externally
//         MyRunnable myRunnable = new MyRunnable();
//         Thread thread = new Thread(myRunnable);
//         thread.start();

//         // Setup for Soot analysis
//         String classPath = ".";  // Assuming this is the correct path where your classes are
//         String outputDirJimple = "./output/jimple"; // Directory for Jimple output
//         String dir = "./testcase"; // Directory containing source classes to analyze

//         // Arguments for generating Jimple files
//         String[] sootArgsJimple = {
//             "-cp", classPath,
//             "-pp",
//             "-w",
//             "-f", "J", // Output format set to Jimple
//             "-keep-line-number",
//             "-main-class", "Test4", // Adjust the main class as necessary
//             "-process-dir", dir,
//             "-output-dir", outputDirJimple,
//             "-allow-phantom-refs",
//             "-p", "cg.spark", "enabled:true",
//             "-p", "cg.spark", "on-fly-cg:false"
//         };

//         // Create and add transformer for analysis
//         AnalysisTransformer analysisTransformer = new AnalysisTransformer();
//         PackManager.v().getPack("jtp").add(new Transform("jtp.dfa", analysisTransformer));

//         // Call Soot's main method with arguments
//         soot.Main.main(sootArgsJimple);

//         // Optionally, stop the running thread after Soot processing is done
//         myRunnable.stopRunning();
//         try {
//             thread.join();  // Wait for the thread to terminate
//         } catch (InterruptedException e) {
//             System.err.println("Thread interrupted: " + e.getMessage());
//         }

//         // Optional: additional steps after thread and analysis are complete
//         System.out.println("Analysis and thread processing complete.");
//     }
// }

// class MyRunnable implements Runnable {
//     private volatile boolean running = true;

//     public void run() {
//         while (running) {
//             // Your thread's work here
//             try {
//                 Thread.sleep(1000); // Simulate some work with sleep
//             } catch (InterruptedException e) {
//                 System.out.println("Thread interrupted during sleep.");
//                 break; // Optional: break the loop if interrupted
//             }
//         }
//         System.out.println("Thread is stopping.");
//     }

//     public void stopRunning() {
//         this.running = false;
//     }
// }

