package org.jenkinsci.plugins.zap;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.slaves.SlaveComputer;

public class ZAPDriver extends AbstractDescribableImpl<ZAPDriver> implements Serializable {
    /* ZAP executable files */
    private static final String ZAP_PROG_NAME_BAT = "zap.bat";
    private static final String ZAP_PROG_NAME_SH = "zap.sh";

    @DataBoundConstructor
    public ZAPDriver(String zapHome) {
        this.zapHome = zapHome;
    }

    public Proc startZAP(AbstractBuild<?, ?> build, BuildListener listener, Launcher launcher)
            throws IOException, InterruptedException {
        FilePath ws = build.getWorkspace();
        if (ws == null) {
            Node node = build.getBuiltOn();
            if (node == null)
                throw new NullPointerException("No such build node: " + build.getBuiltOnStr());
            throw new NullPointerException("No workspace from node " + node + " which is computer " + node.toComputer()
                    + " and has channel " + node.getChannel());
        }

        String zapProgram = retrieveZapHomeWithToolInstall(build, listener);

        FilePath zapPathWithProgName = new FilePath(ws.getChannel(),
                zapProgram + getZAPProgramNameWithSeparator(build));

        List<String> cmd = new ArrayList<String>();
        cmd.add(zapPathWithProgName.getRemote());
        EnvVars envVars = build.getEnvironment(listener);
        FilePath workDir = new FilePath(ws.getChannel(), zapProgram);
        Proc proc = launcher.launch().cmds(cmd).envs(envVars).stdout(listener).pwd(workDir).start();
        return proc;
    }

    public Boolean executeZAP(BuildListener listener, FilePath filePath) {
        boolean buildSuccess = true;

        /* Do Stuff */

        return buildSuccess;
    }

    private String retrieveZapHomeWithToolInstall(AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException {
        EnvVars env = null;
        Node node = null;
        String installPath = null;
        installPath = build.getEnvironment(listener).get(this.zapHome);
        return installPath;
    }

    private String getZAPProgramNameWithSeparator(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
        Node node = build.getBuiltOn();
        String zapProgramName = "";

        /* Append zap program following Master/Slave and Windows/Unix */
        if ("".equals(node.getNodeName())) { // Master
            if (File.pathSeparatorChar == ':')
                zapProgramName = "/" + ZAP_PROG_NAME_SH;
            else
                zapProgramName = "\\" + ZAP_PROG_NAME_BAT;
        } else if ("Unix".equals(((SlaveComputer) node.toComputer()).getOSDescription()))
            zapProgramName = "/" + ZAP_PROG_NAME_SH;
        else
            zapProgramName = "\\" + ZAP_PROG_NAME_BAT;
        return zapProgramName;
    }

    private final String zapHome; /* Environment variable for the ZAP path. */

    public String getZapHome() {
        return zapHome;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ZAPDriver> implements Serializable {

        private FilePath workspace;

        public void setWorkspace(FilePath ws) {
            this.workspace = ws;
        }

        @Override
        public String getDisplayName() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
