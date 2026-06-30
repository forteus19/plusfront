import java.io.File;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.java.decompiler.api.Decompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.decompiler.SingleFileSaver;

public abstract class DecompileTask extends DefaultTask {
	@InputFile
	public abstract RegularFileProperty getInput();
	@OutputFile
	public abstract RegularFileProperty getOutput();

	@TaskAction
	public void run() {
		File input = getInput().get().getAsFile();
		File output = getOutput().get().getAsFile();

		Decompiler decompiler = Decompiler.builder()
			.inputs(input)
			.output(new SingleFileSaver(output))
			.logger(new PrintStreamLogger(System.out))
			.build();

		decompiler.decompile();
	}
}
