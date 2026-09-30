import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public abstract class RemapDirTask extends RemapTask {
	@InputDirectory
	public abstract DirectoryProperty getInput();
	@OutputDirectory
	public abstract DirectoryProperty getOutput();

	@TaskAction
	public void run() {
		remap(
			getInput().get().getAsFile().toPath(),
			getOutput().get().getAsFile().toPath(),
			false
		);
	}
}
