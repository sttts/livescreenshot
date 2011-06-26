/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.limoia.jenkins.livescreenshot;

import hudson.Extension;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import java.util.List;

/**
 *
 * @author sts
 */
public class LiveScreenshotColumn extends ListViewColumn {
	public String collectScreenshots(Run run) {
		String html = "";
		// sub-runs of matrix?
		if (run instanceof MatrixBuild) {
			MatrixBuild mb = (MatrixBuild)run;
			List<MatrixRun> childRuns = mb.getRuns();
			for (MatrixRun child : childRuns) {
				html = html + collectScreenshots(child);
			}
		} else {
			// add screenshot of current job	
			if (run.isBuilding()) {
				html = html + "<a href=\"" + run.getUrl() + "screenshot\">" +
						"<img src=\"" + run.getUrl() + "screenshot/thumb\" /></a>";
			}
		}
		return html;
	}
	
    public String getScreenshots(Job job) {
		String s = "";
		RunList runs = job.getBuilds();
		for (Object o : runs) {
			Run r = (Run)o;
			s = s + collectScreenshots(r);
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
