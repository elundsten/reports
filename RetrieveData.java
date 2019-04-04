import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.util.*;
import java.lang.String;

public class RetrieveData {

    private String packageName;
    public RetrieveData(){

    }

    public File[] findAllFiles(){
        packageName = "/home/erik/Desktop/commons-math/target/surefire-reports";
        File dir = new File(packageName);
        File[] txtFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });
        return txtFiles;
    }

    public void readFiles(File[] listOfFiles){
        for (File child : listOfFiles){
            try{
                BufferedReader br = new BufferedReader(new FileReader("/home/erik/Desktop/testWrite.txt"));
                Scanner scanner = new Scanner(child);
                BufferedWriter writer = new BufferedWriter(new FileWriter("/home/erik/Desktop/allData.txt",true));
                HashMap<String, String> dataWithEmptySlots = new HashMap<String, String>();
                String line;
                while ((line = br.readLine()) != null){
                    //System.out.println("fail: " + line);
                    String testClass = line.split(",")[7];
                    dataWithEmptySlots.put(testClass, line);
                }
                while (scanner.hasNextLine()) {
                    String testLine = scanner.nextLine();
                    String[] tokens = testLine.split(",");
                    String[] testRunTokens = tokens[0].split(":");
                    int numberOfTests = 0;
                    if (testRunTokens[0].equals("Tests run")){
                        String stringNumTests = testRunTokens[1].replaceAll("\\s+","");
                        numberOfTests = Integer.parseInt(stringNumTests);
                        String[] testRunTokensRuns = tokens[0].split(":");
                        String[] testRunTokensFailures = tokens[1].split(":");
                        String[] testRunTokensErrors = tokens[2].split(":");
                        String[] testRunTokensSkipped = tokens[3].split(":");
                        String[] timeElapsed = tokens[4].split(":");
                        String[] testTarget = timeElapsed[1].split("-");
                        String runs = testRunTokensRuns[1].replaceAll("\\s+","");
                        String failures = testRunTokensFailures[1].replaceAll("\\s+","");
                        String errors = testRunTokensErrors[1].replaceAll("\\s+","");
                        String testName = testTarget[1].split(" ")[2];
                        String timeTmp = testTarget[0].replaceAll("s","");
                        String time = timeTmp.replaceAll("\\s+","");
                        String[] listData = dataWithEmptySlots.get(testName).split(",");

                        listData[0] = runs;
                        listData[1] = failures;
                        listData[2] = errors;
                        listData[3] = time;

                        /*System.out.println("Failures: " + Arrays.toString(testRunTokensFailures));
                        System.out.println("Errors: " + Arrays.toString(testRunTokensErrors));
                        System.out.println("Skipped: " + Arrays.toString(testRunTokensSkipped));
                        System.out.println("time elapsed: " + Arrays.toString(timeElapsed));
                        System.out.println("test target: " + Arrays.toString(testTarget));*/
                        String stringToWriteToFile = String.join(",",listData);
                        /* TODO the fourth column says failure sometimes (i guess if the test fails) */
                        System.out.println(Arrays.toString(listData));
                        writer.write(stringToWriteToFile);
                        writer.newLine();
                    }
                }
                writer.close();
                scanner.close();
            } catch (IOException e){

            }
        }
    }

    public static void main(String[] args){
        RetrieveData ob = new RetrieveData();
        File[] listOfFiles = ob.findAllFiles();
        String listOfPaths[] = Arrays.stream(listOfFiles).map(File::getAbsolutePath).toArray(String[]::new);
        //System.out.println("listOfPaths: " + listOfPaths[0]);
        ob.readFiles(listOfFiles);
    }
    public void readFile(){

    }
}
