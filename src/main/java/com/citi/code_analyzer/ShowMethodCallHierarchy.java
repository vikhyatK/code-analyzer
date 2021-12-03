package com.citi.code_analyzer;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import com.citi.code_analyzer.service.MethodService;

import spoon.Launcher;

public class ShowMethodCallHierarchy {
	
	private static final String SRC = "\\src\\main\\java";
	
	@Option(name = "-s", aliases = "--source-folder", metaVar = "SOURCE_FOLDER",
			usage = "source folder for the analyzed project",
			required = true)
	private String sourceFolder;

	@Option(name = "-t", aliases = "--table-name", metaVar = "TABLE_NAME",
			usage = "table name to figure out the data path",
			required = true)
	private String tableName;

    public static void main(String[] args) {
    	ShowMethodCallHierarchy showMethodCallHierarchy = new ShowMethodCallHierarchy();
    	showMethodCallHierarchy.parse(args).doMain();
    }
    
    private ShowMethodCallHierarchy parse(String[] args) {
        
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.print("Usage: java -jar <CA_JAR_PATH>" + parser.printExample(OptionHandlerFilter.REQUIRED));
            System.err.println();
            System.err.println();
            System.err.println("Options:");
            parser.printUsage(System.err);
            System.exit(1);
        }
        return this;
    }
    
    public void doMain() {
    	Launcher launcher = new Launcher();
    	if(StringUtils.isBlank(sourceFolder) && !sourceFolder.contains(SRC)) {
    		sourceFolder = sourceFolder + SRC;
    	}
    	File file = new File(sourceFolder);
    	if(file.exists() && file.isDirectory()) {
    		launcher.addInputResource(sourceFolder);
            launcher.buildModel();
            parse(launcher);	
    	} else {
    		System.err.println("The source folder path is not correct");
    	}
    }

	private void parse(Launcher launcher) {
    	Map<String, List<String>> classToMethodNameMap = MethodService.findManipulatingMethodsOfTable(tableName);
    	MethodService.getStack(launcher, classToMethodNameMap);
	}
}