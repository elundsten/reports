import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.commons.io.FileUtils;
public class Decompile {

    public Decompile(){

    }
    public void decompileFile(String FullpathToMutant) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String commandLineDecompiler = "java -jar /home/erik/Desktop/jd-cli-0.9.2-dist/jd-cli.jar " +
                "-od /home/erik/Desktop -n -dm " +
                "-rn " + FullpathToMutant;
        /*
        /home/erik/Desktop/commons-math/target/pit-reports/export/org/apache
        /commons/math4/util/TransformerMap/mutants/9/org.apache.commons.math4.util.TransformerMap.class
         */
        Process pr = rt.exec(commandLineDecompiler);
    }
    public static void getSubdirs(File rootDir)  {
        final String[] SUFFIX = {"class"};  // use the suffix to filter
        Collection<File> files = FileUtils.listFiles(rootDir, SUFFIX, true);
        for(File file : files){
            System.out.println(file.toString());
        }
    }
    public static void main(String[] args){
        String pathToAllMutants = "/home/erik/Desktop/commons-math/target/pit-reports/export";
        File dir = new File(pathToAllMutants);
        getSubdirs(dir);
    }
}


