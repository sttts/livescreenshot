/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.limoia.jenkins.livescreenshot;

import hudson.Extension;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author sts
 */
public class LiveScreenshotColumn extends ListViewColumn {
	
	@DataBoundConstructor
    public LiveScreenshotColumn() {
    }
	
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
		// collect builds
		RunList runs = job.getBuilds();
		Map<MatrixBuild, String> matrixJobStrings = new HashMap<MatrixBuild, String>();
		matrixJobStrings.put(null, "");
		for (Object o : runs) {
			Run r = (Run)o;
			if (!r.isBuilding())
				continue;
			String rs = this.collectScreenshots(r);
			if (rs.isEmpty())
				continue;
			if (r instanceof MatrixBuild) {
				MatrixBuild mb = (MatrixBuild)r;
				if (matrixJobStrings.containsKey(mb)) {
					matrixJobStrings.put(mb, matrixJobStrings.get(mb) + rs);
				} else {
					matrixJobStrings.put(mb, rs);
				}
			} else {
				matrixJobStrings.put(null, matrixJobStrings.get(null) + rs);
			}
		}
		
		// row for each matrix job
		String s = matrixJobStrings.get(null);
		for (Map.Entry<MatrixBuild, String> pair : matrixJobStrings.entrySet()) {
			// newline?
			if (!s.isEmpty()) {
				s += "<br/>";
			}
			
			// add link matrix job and "stop" action 
			MatrixBuild mb = pair.getKey();
			if (mb != null) {
				s += "<a href=\"" + mb.getUrl() + "\">" + mb.getDisplayName() + "</a> ";
				Executor executor = mb.getOneOffExecutor();
				if (executor != null) {
					Computer computer = executor.getOwner();
					if (computer != null) {
						// find number of oneOffExecutor on that computer. No function for that!
						int num = computer.getOneOffExecutors().indexOf(executor);
						s += "<a href=\"" + computer.getUrl() + "oneOffExecutors/" + num + "/stop\">Stop</a> ";
					}
				}
			}
			
			// append screenshots
			s += pair.getValue();
		}
		
        return s;
    }
    
    @Extension
    public static class LiveScreenshotColumnDescriptor extends ListViewColumnDescriptor {

        @Override
        public String getDisplayName() {
            return "Screenshots";
        }
    }
}
