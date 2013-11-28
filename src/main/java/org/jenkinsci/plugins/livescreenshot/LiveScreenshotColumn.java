package org.jenkinsci.plugins.livescreenshot;

import hudson.Extension;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.util.RunList;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		// collect screenshot link strings for all active builds
		RunList runs = job.getBuilds();
		HashMap<AbstractBuild, String> runScreenshotStrings = new HashMap<AbstractBuild, String>();
		for (Object o : runs) {
			AbstractBuild b = (AbstractBuild)o;
			if (!b.isBuilding())
				continue;
			String rs = this.collectScreenshots(b);
			if (rs.isEmpty())
				continue;
			if (runScreenshotStrings.containsKey(b)) {
					runScreenshotStrings.put(b, runScreenshotStrings.get(b) + rs);
			} else {
				runScreenshotStrings.put(b, rs);
			}
		}
		
		// one row for each job
		String s = "";
		for (Map.Entry<AbstractBuild, String> pair : runScreenshotStrings.entrySet()) {
			// newline?
			if (!s.isEmpty()) {
				s += "<br/><br/>";
			}
			
			// first the screenshots
			s += pair.getValue();

			// then the line with the "stop" link and the changelog
			AbstractBuild r = pair.getKey();
			s += "<br/><a href=\"" + r.getUrl() + "\">" + r.getDisplayName() + "</a> ";

			// create link to executor stop action, or the oneOffExecutor for MatrixBuilds
			Executor executor = null;
			boolean isOneOffExecutor = r.getOneOffExecutor() != null;
			if (isOneOffExecutor) {
				executor = r.getOneOffExecutor();
			} else {
				executor = r.getExecutor();
			}
			if (executor != null) {
				Computer computer = executor.getOwner();
				if (computer != null) {
					s += "<a href=\"" + computer.getUrl() + 
							(isOneOffExecutor ? "oneOffExecutors" : "executors") +
							"/" + 
							(isOneOffExecutor ? computer.getOneOffExecutors().indexOf(executor) : executor.getNumber()) +
							"/stop\">Stop</a> ";
				}
			}
			
			// append changelog entries
			ChangeLogSet<? extends Entry> changeLogSet = r.getChangeSet();
			if (changeLogSet != null) {
				for (Object o : changeLogSet.getItems()) {
					if (o instanceof ChangeLogSet.Entry) {
						ChangeLogSet.Entry e = (ChangeLogSet.Entry)o;
						s += " - " + e.getMsgAnnotated();
					}
				}
			}
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
