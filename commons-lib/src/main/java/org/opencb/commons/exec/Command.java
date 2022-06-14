/*
 * Copyright 2015-2017 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.commons.exec;

import org.apache.tools.ant.types.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Command extends RunnableProcess {

    // protected String executable;
    // protected String pathScript;
    // protected String outDir;
    // protected Arguments arguments;

    private String commandLine;
    private Map<String, String> environment;
    private Process proc;
    private boolean clearEnvironment = false;

    protected static Logger logger = LoggerFactory.getLogger(Command.class);
    private StringBuffer outputBuffer = new StringBuffer();
    private StringBuffer errorBuffer = new StringBuffer();
    private OutputStream outputOutputStream = null;
    private OutputStream errorOutputStream = null;

    private final String[] cmdArray;
    private boolean printOutput = true;

    public Command(String commandLine) {
        this.commandLine = commandLine;
        cmdArray = Commandline.translateCommandline(getCommandLine());
    }

    public Command(String commandLine, List<String> environment) {
        this.commandLine = commandLine;
        this.environment = parseEnvironment(environment);
        cmdArray = Commandline.translateCommandline(getCommandLine());
    }

    public Command(String[] cmdArray, List<String> environment) {
        this.cmdArray = cmdArray;
        this.commandLine = Commandline.toString(cmdArray);
        this.environment = parseEnvironment(environment);
    }

    public Command(String[] cmdArray, Map<String, String> environment) {
        this.cmdArray = cmdArray;
        this.commandLine = Commandline.toString(cmdArray);
        this.environment = environment;
    }

    @Override
    public void run() {
        try {
            setStatus(Status.RUNNING);

            startTime();
            logger.debug(Commandline.describeCommand(cmdArray));
            if (environment != null && environment.size() > 0) {
                ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
                if (clearEnvironment) {
                    processBuilder.environment().clear();
                }
                processBuilder.environment().putAll(environment);
//                logger.debug("Environment variables:");
//                processBuilder.environment().forEach((k, v) -> logger.debug("\t" + k + "=" + v));
                proc = processBuilder.start();
            } else {
                proc = Runtime.getRuntime().exec(cmdArray);
            }

            InputStream is = proc.getInputStream();
            // Thread out = readOutputStream(is);
            Thread readOutputStreamThread = readOutputStream(is);
            InputStream es = proc.getErrorStream();
            // Thread err = readErrorStream(es);
            Thread readErrorStreamThread = readErrorStream(es);

            proc.waitFor();
            readOutputStreamThread.join();
            readErrorStreamThread.join();
            endTime();

            setExitValue(proc.exitValue());
            if (proc.exitValue() != 0) {
                status = Status.ERROR;
                // output = IOUtils.toString(proc.getInputStream());
                // error = IOUtils.toString(proc.getErrorStream());
                output = outputBuffer.toString();
                error = errorBuffer.toString();
            }
            if (status != Status.KILLED && status != Status.TIMEOUT && status != Status.ERROR) {
                status = Status.DONE;
                // output = IOUtils.toString(proc.getInputStream());
                // error = IOUtils.toString(proc.getErrorStream());
                output = outputBuffer.toString();
                error = errorBuffer.toString();
            }

        } catch (RuntimeException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            exception = e.toString();
            status = Status.ERROR;
            exitValue = -1;
            logger.error("Exception occurred while executing Command " + exception, e);
        }
    }

    @Override
    public void destroy() {
        proc.destroy();
    }

    private Thread readOutputStream(InputStream ins) throws IOException {
        return readStream("stdout", outputOutputStream, outputBuffer, ins);
    }

    private Thread readErrorStream(InputStream ins) throws IOException {
        return readStream("stderr", errorOutputStream, errorBuffer, ins);
    }

    private Thread readStream(String outputName, OutputStream outputStream, StringBuffer stringBuffer, InputStream in) {
        Thread thread = new Thread(() -> {
            try {
                int bytesRead = 0;
                int bufferLength;
                byte[] buffer;

                while (bytesRead != -1) {
                    // int x=in.available();
                    // if (x<=0)
                    // continue ;

                    bufferLength = in.available();
                    bufferLength = Math.max(bufferLength, 1);

                    buffer = new byte[bufferLength];
                    bytesRead = in.read(buffer, 0, bufferLength);

                    if (bytesRead == 0) {
                        Thread.sleep(500);
                        logger.trace(outputName + " - Sleep");
                    } else if (bytesRead > 0) {
                        logger.trace(outputName + " - last bytesRead = {})", bytesRead);
                        if (printOutput) {
                            System.err.print(new String(buffer));
                        }

                        if (outputStream == null) {
                            stringBuffer.append(new String(buffer));
                        } else {
                            outputStream.write(buffer);
                            outputStream.flush();
                        }
                    }
                }
                logger.debug("Read {} - Exit while", outputName);
            } catch (Exception ex) {
                ex.printStackTrace();
                exception = ex.toString();
            }
        }, outputName + "_reader");
        thread.start();
        return thread;
    }

    /**
     * @param commandLine the commandLine to set
     */
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    /**
     * @return the commandLine
     */
    public String getCommandLine() {
        return commandLine;
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironment(List<String> environment) {
        this.environment = parseEnvironment(environment);
    }

    /**
     * @return the environment
     */
    public List<String> getEnvironment() {
        return environment == null ? Collections.emptyList() : Collections.unmodifiableList(
                environment.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList()));
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironmentMap(Map<String, String> environment) {
        this.environment = environment;
    }

    /**
     * @return the environment as map
     */
    public Map<String, String> getEnvironmentMap() {
        return environment;
    }

    public boolean isClearEnvironment() {
        return clearEnvironment;
    }

    public Command setClearEnvironment(boolean clearEnvironment) {
        this.clearEnvironment = clearEnvironment;
        return this;
    }

    private Map<String, String> parseEnvironment(List<String> environmentList) {
        Map<String, String> environment = new HashMap<>();
        for (String s : environmentList) {
            String[] split = s.split("=");
            environment.put(split[0], split[1]);
        }
        return environment;
    }

    public OutputStream getOutputOutputStream() {
        return outputOutputStream;
    }

    public Command setOutputOutputStream(OutputStream outputOutputStream) {
        this.outputOutputStream = outputOutputStream;
        return this;
    }

    public OutputStream getErrorOutputStream() {
        return errorOutputStream;
    }

    public Command setErrorOutputStream(OutputStream errorOutputStream) {
        this.errorOutputStream = errorOutputStream;
        return this;
    }

    public Command setPrintOutput(boolean printOutput) {
        this.printOutput = printOutput;
        return this;
    }
}
