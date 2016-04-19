package test;

import akka.actor.UntypedActor;

/**
 * Created by 10 on 19.04.2016.
 */
public class Worker extends UntypedActor{

    public void onReceive(Object message) throws Exception {
        if(message instanceof Integer) {

        }
    }
}
