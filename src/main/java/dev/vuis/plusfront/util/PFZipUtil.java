package dev.vuis.plusfront.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class PFZipUtil {
	private PFZipUtil() {
		throw new AssertionError();
	}

	public static void copyIntoZip(ZipOutputStream output, Path sourcePath, Path zipPath) throws IOException {
		output.putNextEntry(new ZipEntry(zipPath.toString().replace("\\", "/")));
		Files.copy(sourcePath, output);
		output.closeEntry();
	}

	public static void saveFolderAsZip(Path sourcePath, Path targetPath) throws IOException {
		try (
			ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(targetPath));
			Stream<Path> walkStream = Files.walk(sourcePath)
		) {
			walkStream
				.filter(path -> !Files.isDirectory(path))
				.forEach(path -> {
					try {
						copyIntoZip(output, path, sourcePath.relativize(path));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}
}
