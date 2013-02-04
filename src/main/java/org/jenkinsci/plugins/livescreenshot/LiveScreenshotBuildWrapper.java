package org.jenkinsci.plugins.livescreenshot;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import java.io.IOException;
import java.io.OutputStream;

import org.kohsuke.stapler.DataBoundConstructor;

public class LiveScreenshotBuildWrapper extends BuildWrapper {
	private final String fullscreenFilename;
	private final String thumbnailFilename;
	
	public String getFullscreenFilename() {
		return fullscreenFilename;
	}

	public String getThumbnailFilename() {
		return thumbnailFilename;
	}
	
	@DataBoundConstructor
	public LiveScreenshotBuildWrapper(String fullscreenFilename, String thumbnailFilename) {
		this.fullscreenFilename = fullscreenFilename;
		this.thumbnailFilename = thumbnailFilename;
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {

		// add action to job
		final LiveScreenshotAction action = new LiveScreenshotAction(build, 
				this.fullscreenFilename, this.thumbnailFilename);
		build.addAction(action);

		// copy screenshots to artifact dir on finish, because the workspace
		// might be reused by another build.
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener)
					throws IOException, InterruptedException {
				
				// create artifacts directory
				String path = build.getArtifactsDir().getCanonicalPath() + "/screenshots";
				File screenshotDir = new File(path);
				screenshotDir.mkdirs();
				
				// store kvmtest.png as artifacts
				try {
					byte[] bytes = action.liveScreenshot(fullscreenFilename);
					if (bytes != null) {
						OutputStream os = new FileOutputStream(path + "/" + fullscreenFilename);
						os.write(bytes);
						os.close();
					}
				}
				catch (IOException e) {
				}
				
				// store kvmtest-thumb.png as artifacts
				try {
					byte[] bytes = action.liveScreenshot(thumbnailFilename);
					if (bytes != null) {
						OutputStream os = new FileOutputStream(path + "/" + thumbnailFilename);
						os.write(bytes);
						os.close();
					}
				}
				catch (IOException e) {
				}
				
				return true;
			}
		};
	}

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {
		@Override
		public String getDisplayName() {
			return "Show screenshot during build";
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}
	}
}
