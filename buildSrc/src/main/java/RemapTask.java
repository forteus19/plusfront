import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Stream;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import net.fabricmc.tinyremapper.extension.mixin.MixinExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;

public abstract class RemapTask extends DefaultTask {
	@InputFile
	public abstract RegularFileProperty getMappings();
	@InputFiles
	public abstract ConfigurableFileCollection getClasspath();
	@Input
	public abstract Property<String> getFrom();
	@Input
	public abstract Property<String> getTo();
	@Input
	public abstract Property<Boolean> getNonClassFiles();
	@Input
	public abstract Property<Boolean> getMixinExtension();

	public RemapTask() {
		getNonClassFiles().convention(true);
		getMixinExtension().convention(true);
	}

	protected void remap(Path input, Path output, boolean isJar) {
		Path mappings = getMappings().get().getAsFile().toPath();
		Path[] classpath = getClasspath().getFiles().stream().map(File::toPath).toArray(Path[]::new);
		String from = getFrom().get();
		String to = getTo().get();
		boolean nonClassFiles = getNonClassFiles().get();
		boolean mixinExtension = getMixinExtension().get();

		TinyRemapper.Builder builder = TinyRemapper.newRemapper()
			.withMappings(TinyUtils.createTinyMappingProvider(mappings, from, to))
			.ignoreConflicts(true);

		if (mixinExtension) {
			new MixinExtension().attach(builder);
		}

		TinyRemapper remapper = builder.build();

		try {
			if (isJar) {
				Files.deleteIfExists(output);
			} else if (Files.isDirectory(output)) {
				deleteDir(output);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output).assumeArchive(isJar).build()) {
			if (nonClassFiles) {
				outputConsumer.addNonClassFiles(input);
			}

			remapper.readInputs(input);
			remapper.readClassPath(classpath);

			remapper.apply(outputConsumer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			remapper.finish();
		}
	}

	private static void deleteDir(Path path) throws IOException {
		try (Stream<Path> walkStream = Files.walk(path)) {
			Iterator<Path> files = walkStream.sorted(Comparator.reverseOrder()).iterator();

			for (Iterator<Path> it = files; it.hasNext(); ) {
				Files.deleteIfExists(it.next());
			}
		}
	}
}
