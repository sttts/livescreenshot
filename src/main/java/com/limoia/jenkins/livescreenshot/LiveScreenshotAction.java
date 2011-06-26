/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.limoia.jenkins.livescreenshot;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @author sts
 */
public class LiveScreenshotAction implements Action {
	private AbstractBuild build;
	private final String fullscreenFilename;
	private final String thumbnailFilename;
	
	public String getFullscreenFilename() {
		return fullscreenFilename;
	}

	public String getThumbnailFilename() {
		return thumbnailFilename;
	}
	
	public LiveScreenshotAction(AbstractBuild build, String fullscreenFilename, String thumbnailFilename) {
		this.build = build;
		this.fullscreenFilename = fullscreenFilename;
		this.thumbnailFilename = thumbnailFilename;
	}

	public AbstractBuild getBuild() {
		return this.build;
	}
	
	public String getDisplayName() {
		return "Screenshot";
	}

	public String getIconFileName() {
		return "monitor.gif";
	}

	public String getUrlName() {
		return "screenshot";
	}

	public void doDynamic(StaplerRequest request, StaplerResponse rsp)
			throws Exception {
		// which file to load?
		String path = request.getRestOfPath();
		String filename = null;
		if (path.equals("/thumb")) {
			filename = this.thumbnailFilename;
		} else if (path.equals("/full")) {
			filename = this.fullscreenFilename;
		} else {
			return;
		}
			
		// load image
		byte[] bytes = new byte[0];
		try {
			bytes = screenshot(this.thumbnailFilename);
		}
		catch (IOException e) {
			return;
		}
			
		// output image
		if (filename.endsWith(".PNG") || filename.endsWith(".png"))
			rsp.setContentType("image/png");
		else if (filename.endsWith(".JPG") || filename.endsWith(".jpg"))
			rsp.setContentType("image/jpeg");
		else
			return;
		rsp.setContentLength(bytes.length);
		ServletOutputStream sos = rsp.getOutputStream();
		sos.write(bytes);
		sos.flush();
		sos.close();
	}

	public byte[] screenshotArtifact(String filename) throws IOException {
		// does artifact exist?
		String path = this.build.getArtifactsDir().getCanonicalPath() + "/screenshots";
		File file = new File(path + "/" + filename);
		if (file.isFile()) {
			// return artifact file
			FileInputStream fis = new FileInputStream(file);
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			fis.close();
			return bytes;
		}
		
		return null;
	}

	public byte[] liveScreenshot(String filename) throws IOException {
		// return workspace file
		FilePath fp = build.getWorkspace().child(filename);
		if (fp == null)
			return null;
		InputStream is = fp.read();
        byte[] bytes = new byte[is.available()];
		is.read(bytes);
		return bytes;

	}
	
	public byte[] screenshot(String filename) throws IOException {
		// try to find artifact
		byte[] bytes = screenshotArtifact(filename);
		if (bytes != null)
			return bytes;
		
		// build inactive, but not artifact?
		if (!this.build.isBuilding())
			return null;
				
		return liveScreenshot(filename);
	}
}
