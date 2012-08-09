Live Screenshot Plugin for Jenkins
----------------------------------

The plugin adds another column to the job list view to show a thumbnail of
a screenshot. Moreover, an "Screenshot" action is added to each build which leads
to a page with the fullsize screenshot.

The screenshot images are read from the workspace of a build, either stored as png 
or jpeg files. These files must be updated by the running build. The refresh mechanism 
of the Jenkins user interface makes sure that you see an up to date screenshot on screen.

By default, the files are called screenshot.png and screenshot-thumb.png. But, this can
easily changed in the build wrapper configuration of a job.

![Screenshot](https://raw.github.com/sttts/livescreenshot/master/screenshot.png "Screenshot")
