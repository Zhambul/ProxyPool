package test;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import static org.jgroups.util.Util.assertFalse;
import static org.jgroups.util.Util.assertTrue;

public class Main {

    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser(){
            {
                accepts("userId").withRequiredArg().required();
                accepts("pass").withRequiredArg().required();
                accepts("help").forHelp();
            }
        };

        parser.parse();


    }
}
