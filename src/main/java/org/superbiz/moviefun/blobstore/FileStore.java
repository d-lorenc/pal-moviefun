package org.superbiz.moviefun.blobstore;

import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = getCoverFile(blob.name);

        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            StreamUtils.copy(blob.inputStream, outputStream);
        }

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        try {
            Path coverFilePath = getExistingCoverPath(name);
            FileInputStream inputStream = new FileInputStream(coverFilePath.toFile());
            return Optional.of(new Blob(name, inputStream, "blob"));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {
        // ...
    }

    private Path getExistingCoverPath(String name) throws URISyntaxException {
        File coverFile = getCoverFile(name);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }

    private File getCoverFile(String name) {
        return new File(name);
    }

}