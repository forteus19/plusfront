import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class RemapJarTask extends RemapTask {
	@InputFile
	public abstract RegularFileProperty getInput();
	@OutputFile
	public abstract RegularFileProperty getOutput();

	@TaskAction
	public void run() {
		remap(
			getInput().get().getAsFile().toPath(),
			getOutput().get().getAsFile().toPath(),
			true
		);
	}
}
