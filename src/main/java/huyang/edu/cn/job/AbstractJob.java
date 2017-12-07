package huyang.edu.cn.job;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AbstractJob {

    private static final Logger log = LoggerFactory.getLogger(AbstractJob.class);

    private static Map<String, String> argMap = argMap = new HashMap<String, String>();

    protected static final Options options = new Options();

    protected AbstractJob() {
    }

    protected static void addOption(String name, String shortName, String description) {
        options.addOption(shortName,name,true,description);
    }

    public static Map<String, String> parseArguments(String[] args) throws Exception{
        options.addOption(new Option("h","help",false,"print help message"));
        final CommandLineParser parser = new PosixParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException e) {
            throw new Exception("parser command line error,params error Please -h  get help",e);
        }
        if (cmd.hasOption("h")) {
            argMap.put("help","help");
        } else if (cmd.hasOption("i")) {
            argMap.put("inputPath",cmd.getOptionValue("i"));
        } else if (cmd.hasOption("o")) {
            argMap.put("outputPath",cmd.getOptionValue("o"));
        } else if (cmd.hasOption("k")) {
            argMap.put("topic",cmd.getOptionValue("k"));
        } else if (cmd.hasOption("b")) {
            argMap.put("beginSave",cmd.getOptionValue("b"));
        } else if (cmd.hasOption("s")) {
            argMap.put("saveNum",cmd.getOptionValue("s"));
        } else if (cmd.hasOption("it")) {
            argMap.put("Iterations",cmd.getOptionValue("it"));
        } else if (cmd.hasOption("mr")) {
            argMap.put("runMR",cmd.getOptionValue("mr"));
        }
        if (argMap.size()==1 && argMap.containsKey("help")) {
            System.out.printf("%-8s%-25s","  -"+options.getOption("h").getOpt(),options.getOption("h").getLongOpt());
            System.out.println(options.getOption("h").getDescription());
            System.out.printf("%-8s%-25s","  -"+options.getOption("i").getOpt(),options.getOption("i").getLongOpt());
            System.out.println(options.getOption("i").getDescription());
            System.out.printf("%-8s%-25s","  -"+options.getOption("o").getOpt(),options.getOption("o").getLongOpt());
            System.out.println(options.getOption("o").getDescription());
            System.out.printf("%-8s%-25s","  -"+options.getOption("k").getOpt(),options.getOption("k").getLongOpt());
            System.out.println(options.getOption("k").getDescription());
            System.out.printf("%-8s%-25s","  -"+options.getOption("b").getOpt(),options.getOption("b").getLongOpt());
            System.out.println(options.getOption("b").getDescription());
            System.out.printf("%-8s%-25s","  -"+options.getOption("s").getOpt(),options.getOption("s").getLongOpt());
            System.out.println(options.getOption("s").getDescription());
            System.out.printf("%-8s%-25s","  -"+options.getOption("it").getOpt(),options.getOption("it").getLongOpt());
            System.out.println(options.getOption("it").getDescription());
            System.out.printf("%-8s%-25s","  -"+options.getOption("mr").getOpt(),options.getOption("mr").getLongOpt());
            System.out.println(options.getOption("mr").getDescription());
        }
        return argMap;
    }
}
