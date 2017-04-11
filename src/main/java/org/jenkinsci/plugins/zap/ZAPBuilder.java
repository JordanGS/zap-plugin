/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Goran Sarenkapa (JordanGS), and a number of other of contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jenkinsci.plugins.zap;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;

import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Proc;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

public class ZAPBuilder extends Builder {

    private final ZAPDriver zaproxy;
    // private final @CheckForNull ZAPDriver zaproxy;
    private @CheckForNull String stuff;

    @DataBoundConstructor
    public ZAPBuilder(ZAPDriver zaproxy, String stuff) {
        this.zaproxy = zaproxy;
        this.stuff = stuff;
    }

    public @CheckForNull String getStuff() {
        return stuff;
    }

    // public @CheckForNull ZAPDriver zaproxy() {
    // return zaproxy;
    // }
    public ZAPDriver getZaproxy() {
        return zaproxy;
    }

    @DataBoundSetter
    public void setStuff(@CheckForNull String stuff) {
        this.stuff = Util.fixNull(stuff);
    }

    private Proc proc;

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
        boolean result = false;
        proc = this.zaproxy.startZAP(build, listener, launcher);
        result = build.getWorkspace().act(new ZAPDriverCallable(listener, this.zaproxy));
        proc.joinWithTimeout(60L, TimeUnit.MINUTES, listener);
        listener.getLogger().println("Stuff was " + stuff + "?");
        return result;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public String getDisplayName() {
            return "CUSTOM BUILD STEP";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }
    }

    /**
     * Used to execute ZAP remotely.
     */
    private static class ZAPDriverCallable implements FileCallable<Boolean> {
        private BuildListener listener;
        private ZAPDriver zaproxy;

        public ZAPDriverCallable(BuildListener listener, ZAPDriver zaproxy) {
            this.listener = listener;
            this.zaproxy = zaproxy;
        }

        @Override
        public Boolean invoke(File f, VirtualChannel channel) {
            return zaproxy.executeZAP(listener, new FilePath(f));
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {
            /* N/A */ }
    }
}
