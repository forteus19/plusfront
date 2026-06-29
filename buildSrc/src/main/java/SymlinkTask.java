import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class SymlinkTask extends DefaultTask {
	@Input
	public abstract Property<String> getTarget();
	@OutputFile
	public abstract RegularFileProperty getLink();

	@TaskAction
	public void run() throws IOException {
		Path target = Path.of(getTarget().get());
		Path link = getLink().get().getAsFile().toPath();

		Files.deleteIfExists(link);

		Path linkParent = link.getParent();
		if (linkParent != null) {
			Files.createDirectories(link.getParent());
		}

		Files.createSymbolicLink(link, target);
	}
}
