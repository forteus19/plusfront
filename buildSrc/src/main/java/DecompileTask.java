import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.java.decompiler.api.Decompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.decompiler.SingleFileSaver;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

public abstract class DecompileTask extends DefaultTask {
	@InputFile
	public abstract RegularFileProperty getInput();
	@InputFiles
	public abstract ConfigurableFileCollection getLibraries();
	@OutputFile
	public abstract RegularFileProperty getOutput();

	@TaskAction
	public void run() throws IOException {
		File input = getInput().get().getAsFile();
		File[] libraries = getLibraries().getFiles().toArray(File[]::new);
		File output = getOutput().get().getAsFile();

		Decompiler decompiler = Decompiler.builder()
			.option(IFernflowerPreferences.INDENT_STRING, "\t")
			.option(IFernflowerPreferences.SOURCE_FILE_COMMENTS, true)
			.inputs(input)
			.libraries(libraries)
			.output(new SingleFileSaver(output))
			.logger(new PrintStreamLogger(System.out))
			.build();

		Files.deleteIfExists(output.toPath());

		decompiler.decompile();
	}
}
