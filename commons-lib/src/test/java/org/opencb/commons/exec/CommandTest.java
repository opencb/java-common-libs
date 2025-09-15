package org.opencb.commons.exec;

import org.junit.Test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class CommandTest {


    @Test
    public void testCommand() throws Exception {
        // Test the Command class
//        String commandLine = "bash -c 'echo  Hello World ; echo Error Message >&2 ; cat -'";
//        String commandLine = "cat";
        String commandLine = "bash -c 'cat <(echo -n \"Hello - \") - <(echo -n \" - END\")'";
        Command command = new Command(commandLine);
        command.setPrintOutput(false);

        // Execute the command
        Future<RunnableProcess.Status> future = command.async();
        DataOutputStream stdin = command.getStdin();
        stdin.write("World from STDIN".getBytes());
        stdin.close();
        future.get(1000, TimeUnit.MILLISECONDS);
        assertEquals(RunnableProcess.Status.DONE, command.getStatus());
        assertEquals("Hello - World from STDIN - END", command.getOutput());
        assertEquals("", command.getError());
    }

}