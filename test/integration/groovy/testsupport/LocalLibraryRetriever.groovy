package testsupport

import hudson.FilePath
import hudson.model.Job
import hudson.model.Run
import hudson.model.TaskListener
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.Nonnull
import org.jenkinsci.plugins.workflow.libs.LibraryRetriever

class LocalLibraryRetriever extends LibraryRetriever {
  @Override
  void retrieve(
      @Nonnull final String name,
      @Nonnull final String version,
      @Nonnull final boolean changelog,
      @Nonnull final FilePath target,
      @Nonnull final Run<? extends Job, ? extends Run> run,
      @Nonnull final TaskListener listener
  ) throws Exception {
    doRetrieve(target, listener)
  }

  @Override
  void retrieve(
      @Nonnull final String name,
      @Nonnull final String version,
      @Nonnull final FilePath target,
      @Nonnull final Run<? extends Job, ? extends Run> run,
      @Nonnull final TaskListener listener
  ) throws Exception {
    doRetrieve(target, listener)
  }

  private static void doRetrieve(
    final FilePath target,
    final TaskListener listener
  ) {
    // Use running directory and copy from that
    final Path runningDirectory = Paths.get(System.getProperty('user.dir'))
    listener.logger.format('Creating to filepath at %s', runningDirectory)
    final FilePath localFilePath = new FilePath(runningDirectory.toFile())
    listener.logger.format('Copying from local path %s to workspace path %s', runningDirectory, target)
    // Copied from SCMSourceRetriever
    localFilePath.copyRecursiveTo('src/**/*.groovy,vars/*.groovy,vars/*.txt,resources/', null, target)
  }
}
