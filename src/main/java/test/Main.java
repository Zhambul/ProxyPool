package test;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import test.database.ProxyRepositoryImpl;

import static org.jgroups.util.Util.assertFalse;
import static org.jgroups.util.Util.assertTrue;

public class Main {

    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser() {
            {
                accepts("check");
            }
        };

        OptionSet optionSet = parser.parse(args);

        if(optionSet.has("check")) {
            System.out.println("checking");
            new ProxyManager(new ProxyRepositoryImpl()).startMonitoring();
        }
        else {
            System.out.println("b");
        }
    }
}
