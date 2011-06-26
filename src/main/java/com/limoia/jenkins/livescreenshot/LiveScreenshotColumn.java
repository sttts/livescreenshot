/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.limoia.jenkins.livescreenshot;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;

/**
 *
 * @author sts
 */
public class LiveScreenshotColumn extends ListViewColumn {
    public String getScreenshots(Job job) {
		String s = "";
		RunList runs = job.getBuilds();
		for (Object o : runs) {
			Run r = (Run)o;
			if (r.isBuilding()) {
				s = s + "<a href=\"" + r.getUrl() + "screenshot\"><img src=\"" + r.getUrl() + "/screenshot/thumb\" /></a>";
			}
		}
        return s;
    }
    
    @Extension
    public static class KvmtestScreenshotColumnDescriptor extends ListViewColumnDescriptor {

        @Override
        public String getDisplayName() {
            return "Screenshots";
        }
    }
}
