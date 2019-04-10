import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReplaceCompiledFiles {
    public ReplaceCompiledFiles(){

    }
    public static void main (String[] args){
        int seed = 18;
        int iterations = 1;
        int leastAmountOfFiles = 2;
        int mostAmountOfFiles = 6;
        //Finding all files in given dirs
        File dir_realFiles = new File("/home/erik/Desktop/commons-math/target/classes");
        File dir_mutantFiles = new File("/home/erik/Desktop/commons-math/target/pit-reports/export");
        File dir_tmp_dir = new File("/home/erik/Desktop/tmp_dir");
        Collection<File> collAllReal = findAllFiles(dir_realFiles);
        List<File> allReal = new ArrayList(collAllReal);
        //Collection<File> collAllMutants = findAllFiles(dir_mutantFiles);
        //List<File> allMutants = new ArrayList(collAllMutants);
        for(int x = 1;x<=iterations;x++){
            Random rand = new Random();
            int amountOfFiles = rand.nextInt(mostAmountOfFiles-leastAmountOfFiles)+leastAmountOfFiles;
            System.out.println("Amount of files: " + amountOfFiles);
            allReal = runProgram(amountOfFiles,allReal, dir_mutantFiles,dir_tmp_dir);
        }
        //TODO: make sure that one file can't be chosen multiple times.
    }
    public static List<File> runProgram(int amountOfFiles,List<File> allReal, File dir_mutantFiles, File dir_tmp_dir){
        //INIT variable


        for (int i = 0; i<amountOfFiles;i++){
            //Getting a single file
            File one_file = getRandomFile(allReal);
            String nameOfFile = getNameOfFile(one_file);

            List<String> pathToMutants = new ArrayList(findSpecificFiles(dir_mutantFiles,nameOfFile));
            if(pathToMutants.size()>0) {

                File one_mutation_file = getRandomMutantFile(pathToMutants);
                System.out.println("size: " + pathToMutants.size());

                List<File> pathsToReal = new ArrayList<>();
                HashMap<File,File> hm = new HashMap();
                if(pathToMutants!=null){
                    System.out.println("before: " + one_file.getAbsolutePath());
                    moveFileToDir(dir_tmp_dir,one_file);
                    File tmpReal = new File(dir_tmp_dir.getAbsolutePath() + "/" + one_file.getName());
                    hm.put(one_file,tmpReal);
                    System.out.println("after: " + one_file.getAbsolutePath());
                    copyFileToDir(one_file,one_mutation_file);
                    pathsToReal.add(one_file);
                }
                allReal.remove(one_file);
            } else {
                allReal.remove(one_file);
            }
        }
        //TODO: run the starts here somewhere.
        //replaceFiles(hm,pathsToReal);
        return allReal;
    }
    public static Collection<File> findAllFiles(File rootDir) {
        final String[] SUFFIX = {"class"};  // use the suffix to filter
        Collection<File> files = FileUtils.listFiles(rootDir, SUFFIX, true);
        for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
            File file = (File) iterator.next();
            //System.out.println(file.getAbsolutePath());
        }
        return files;
    }
    public static Collection<String> findSpecificFiles(File rootDir, String fileName) {
        final String[] SUFFIX = {"class"};  // use the suffix to filter
        Collection<File> files = FileUtils.listFiles(rootDir, SUFFIX, true);
        String path = null;
        Collection<String> allPaths = new ArrayList<String>();
        for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
            File file = (File) iterator.next();
            if(!file.getName().equals(null) && file.getName().contains(fileName)){
                System.out.println("filename here: " + file.getAbsolutePath());
                path = file.getAbsolutePath();
                allPaths.add(path);

            }
        }

        return allPaths;
    }
    public static String getNameOfFile(File one_file){
        System.out.println("in getNameOfFile: " + one_file.getName());
        if(one_file.getName().contains("$")){
            //TODO:
            //make sure we cant modify one file twice?
        }
        return one_file.getName();
    }
    public static File getRandomMutantFile(List<String> stringMutants){
        int seed = 18;
        Random rand = new Random(seed);
        System.out.println(stringMutants.toString());
        String single_path = stringMutants.get(rand.nextInt(stringMutants.size()));
        File mutant_file = new File(single_path);
        return mutant_file;
    }
    public static File getRandomFile(List<File> allFiles){
        int seed = 18;
        Random rand = new Random(seed);
        return allFiles.get(rand.nextInt(allFiles.size()));
    }
    public static void moveFileToDir(File destination_dir,File file_to_copy){
        file_to_copy.renameTo(new File(destination_dir.getAbsolutePath() + "/" + file_to_copy.getName()));
        System.out.println("file_to_copy dir: " + file_to_copy.getParentFile().getAbsolutePath());
    }
    public static void copyFileToDir(File destination_dir, File file_to_copy){
        try{
            FileUtils.copyFile(file_to_copy,destination_dir);
            System.out.println("copyFileToDir: " + destination_dir.getAbsolutePath());
        }catch(IOException e){

        }
    }
    public static void replaceFiles(HashMap<File,File> dirToTmpFiles,List<File> dirToRealFiles){
        for(File realFile : dirToRealFiles){
            File tmpReal = dirToTmpFiles.get(realFile);
            realFile.delete();
            tmpReal.renameTo(realFile);
        }
    }
}
/*
* mvn -Dfeatures=+EXPORT org.pitest:pitest-maven:mutationCoverage
* Also added skip all tests in POM
* */