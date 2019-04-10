/*
 * Copyright (c) 2015 - Present. The STARTS Team. All Rights Reserved.
 */

package edu.illinois.starts.jdeps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.io.*;

import edu.illinois.yasgl.GraphUtils;
import edu.illinois.yasgl.DirectedGraph;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.maven.AgentLoader;
import edu.illinois.starts.util.Logger;
import edu.illinois.starts.util.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Prepares for test runs by writing non-affected tests in the excludesFile.
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.TEST)
public class RunMojo extends DiffMojo {
    /**
     * Set this to "false" to prevent checksums from being persisted to disk. This
     * is useful for "dry runs" where one may want to see the non-affected tests that
     * STARTS writes to the Surefire excludesFile, without updating test dependencies.
     */
    @Parameter(property = "updateRunChecksums", defaultValue = "true")
    protected boolean updateRunChecksums;

    /**
     * Set this option to "true" to run all tests, not just the affected ones. This option is useful
     * in cases where one is interested to measure the time to run all tests, while at the
     * same time measuring the times for analyzing what tests to select and reporting the number of
     * tests it would select.
     * Note: Run with "-DstartsLogging=FINER" or "-DstartsLogging=FINEST" so that the "selected-tests"
     * file, which contains the list of tests that would be run if this option is set to false, will
     * be written to disk.
     */
    @Parameter(property = "retestAll", defaultValue = "false")
    protected boolean retestAll;

    protected Set<String> nonAffectedTests;
    protected Set<String> changedClasses;
    private Logger logger;

    public void execute() throws MojoExecutionException {
        Logger.getGlobal().setLoggingLevel(Level.parse(loggingLevel));
        logger = Logger.getGlobal();
        long start = System.currentTimeMillis();
        setChangedAndNonaffected();
        List<String> excludePaths = Writer.fqnsToExcludePath(nonAffectedTests);
        setIncludesExcludes();
        if (logger.getLoggingLevel().intValue() <= Level.FINEST.intValue()) {
            Writer.writeToFile(nonAffectedTests, "non-affected-tests", getArtifactsDir());
        }
        run(excludePaths);
        Set<String> allTests = new HashSet<>(getTestClasses("checkIfAllAffected"));
        if (allTests.equals(nonAffectedTests)) {
            logger.log(Level.INFO, "********** Run **********");
            logger.log(Level.INFO, "No tests are selected to run.");
        } else{
            eriksExtraction(allTests);
        }
        long end = System.currentTimeMillis();
        System.setProperty("[PROFILE] END-OF-RUN-MOJO: ", Long.toString(end));
        logger.log(Level.FINE, "[PROFILE] RUN-MOJO-TOTAL: " + Writer.millsToSeconds(end - start));
    }
    private void eriksExtraction(Set<String> allTests) throws MojoExecutionException{
        Set<String> affectedTestsErik = new HashSet<>(allTests);
        DirectedGraph<String> graphErik = getGraphErik();
        logger.log(Level.INFO, "********** changedClasses_erik (RUN) **********");
        Set<String> changedFileErik = new HashSet<>();
        for (String listItem : changedClasses) {
                String eriksString = listItem.replace("/" , ".");
                int eriksSubstringIndex = eriksString.indexOf("target.classes.");
                int lastIndex = eriksString.lastIndexOf(".class");
                int indexErik = eriksSubstringIndex + 15;
                String eriksSubstringClass = eriksString.substring(indexErik,lastIndex);
                String erikChangedClasses = eriksSubstringClass.trim();
                changedFileErik.add(erikChangedClasses);
        }
        int fileCardinality = changedClasses.size();
        int targetCardinality = affectedTestsErik.size();
        for (String affectedTest : affectedTestsErik) {
            if (graphErik != null && changedFileErik.size() > 0){
                List<String> shortPath = GraphUtils.computeShortestPath(graphErik,affectedTest,changedFileErik);
            int distance = shortPath.size() - 1;
            String stringToWriteToFile = "emptyslot,emptyslot,emptyslot,emptyslot," 
                + Integer.toString(fileCardinality) + "," + Integer.toString(targetCardinality)
                + "," + Integer.toString(distance) + "," + affectedTest;
            try{
                exportData(stringToWriteToFile);
            } catch (IOException e){}
            }
            logger.log(Level.INFO, "computeShortestPath(erik): " + shortPath.toString());
        }
    }
    private void exportData (String stringToWriteToFile) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter("/home/erik/Desktop/testWrite.txt",true));
        writer.write(stringToWriteToFile);
        writer.newLine();
        writer.close();
    }
    protected void run(List<String> excludePaths) throws MojoExecutionException {
        if (retestAll) {
            dynamicallyUpdateExcludes(new ArrayList<String>());
        } else {
            dynamicallyUpdateExcludes(excludePaths);
        }
        long startUpdateTime = System.currentTimeMillis();
        if (updateRunChecksums) {
            updateForNextRun(nonAffectedTests);
        }
        long endUpdateTime = System.currentTimeMillis();
        logger.log(Level.FINE, "[PROFILE] STARTS-MOJO-UPDATE-TIME: "
                + Writer.millsToSeconds(endUpdateTime - startUpdateTime));
    }

    private void dynamicallyUpdateExcludes(List<String> excludePaths) throws MojoExecutionException {
        if (AgentLoader.loadDynamicAgent()) {
            logger.log(Level.FINEST, "AGENT LOADED!!!");
            System.setProperty(STARTS_EXCLUDE_PROPERTY, Arrays.toString(excludePaths.toArray(new String[0])));
        } else {
            throw new MojoExecutionException("I COULD NOT ATTACH THE AGENT");
        }
    }

    protected void setChangedAndNonaffected() throws MojoExecutionException {
        nonAffectedTests = new HashSet<>();
        changedClasses = new HashSet<>();
        Pair<Set<String>, Set<String>> data = computeChangeData();
        nonAffectedTests = data == null ? new HashSet<String>() : data.getKey();
        changedClasses  = data == null ? new HashSet<String>() : data.getValue();
    }
}
