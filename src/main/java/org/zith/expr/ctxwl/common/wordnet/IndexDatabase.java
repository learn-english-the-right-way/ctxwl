package org.zith.expr.ctxwl.common.wordnet;

import com.google.common.primitives.Bytes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

class IndexDatabase implements AutoCloseable {

    private final String filename;
    private Path localFile;
    private FileChannel channel;
    private volatile MappedByteBuffer buffer;

    IndexDatabase(String filename) {
        this.filename = Objects.requireNonNull(filename);
    }

    Optional<String> lookup(String word) {
        var target = word.getBytes(StandardCharsets.UTF_8);

        var current = getBuffer().slice();

        do {
            current.position(current.remaining() / 2);
            seekBackwardToNewLine(current);
            var pivot = current.position();
            boolean narrowed = false;
            while (!narrowed) {
                if (!current.hasRemaining()) {
                    current = current.slice(0, pivot).rewind();
                    narrowed = true;
                } else {
                    var lineStart = current.position();
                    seekForwardUntilNewLine(current);
                    var lineEnd = current.position();

                    var line = new byte[lineEnd - lineStart];
                    current.position(lineStart).get(line);
                    if (!(line.length >= 2 && line[0] == ' ' && line[1] == ' ')) {
                        var pos = Bytes.indexOf(line, (byte) ' ');
                        if (pos >= 0) {
                            var lemma = new byte[pos];
                            System.arraycopy(line, 0, lemma, 0, pos);
                            var cmp = compare(target, lemma);
                            if (cmp < 0) {
                                current = current.slice(0, lineStart).rewind();
                                narrowed = true;
                            } else if (cmp > 0) {
                                current = current.position(lineEnd).slice();
                                narrowed = true;
                            } else {
                                return Optional.of(word);
                            }
                        }
                    }
                }
            }
        } while (current.hasRemaining());

        return Optional.empty();
    }

    private int compare(byte[] a, byte[] b) {
        for (int i = 0; i < Integer.min(a.length, b.length); i++) {
            if (a[i] < b[i]) return -1;
            else if (a[i] > b[i]) return 1;
        }

        if (a.length < b.length) return -1;
        else if (a.length > b.length) return 1;

        return 0;
    }

    private static void seekBackwardToNewLine(ByteBuffer current) {
        var buffer = new byte[32];
        while (current.position() > 0) {
            int n = Integer.min(current.position(), buffer.length);
            current.position(current.position() - n);
            current.slice().get(buffer, buffer.length - n, n);
            var pos = Bytes.lastIndexOf(buffer, (byte) '\n');
            if (pos > 0) {
                current.position(current.position() + pos + 1);
                break;
            }
        }
    }

    private static void seekForwardUntilNewLine(MappedByteBuffer current) {
        var buffer = new byte[32];
        while (current.hasRemaining()) {
            current.mark();
            var n = Integer.min(current.remaining(), buffer.length);
            current.get(buffer, 0, n);
            var pos = Bytes.indexOf(buffer, (byte) '\n');
            if (pos >= n) pos = -1;
            if (pos > 0) {
                current.reset();
                current.position(current.position() + pos + 1);
                break;
            }
        }
    }

    private MappedByteBuffer getBuffer() {
        var buffer = this.buffer;
        if (buffer == null) {
            synchronized (this) {
                buffer = this.buffer;
                if (buffer == null) {
                    var localFile = this.localFile;
                    long size = -1;
                    if (localFile == null) {
                        var extsep = filename.lastIndexOf('.');
                        Path tmpfile;
                        try {
                            tmpfile = Files.createTempFile(
                                    extsep >= 0 ? filename.substring(0, extsep) : filename,
                                    (extsep > 0 ? filename.substring(extsep) : "") + ".wndb"
                            );
                            try (var resource = Objects.requireNonNull(
                                    MorphologyExceptionDatabase.class
                                            .getResourceAsStream(Constants.RESOURCE_BASE + filename),
                                    "Failed to load the resource file")) {
                                size = Files.copy(resource, tmpfile, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                        this.localFile = localFile = tmpfile;
                    }

                    FileChannel channel;
                    try {
                        boolean owningChannel = true;
                        Throwable currentThrowable = null;
                        channel = FileChannel.open(localFile, StandardOpenOption.READ);
                        try {
                            if (size == -1) size = channel.size();
                            buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
                            owningChannel = false;
                        } catch (Throwable throwable) {
                            currentThrowable = throwable;
                            throw throwable;
                        } finally {
                            if (owningChannel) {
                                try {
                                    channel.close();
                                } catch (Throwable throwable) {
                                    currentThrowable.addSuppressed(throwable);
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }

                    this.channel = channel;
                    this.buffer = buffer;
                }
            }
        }

        return buffer;
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            var throwables = new ArrayList<Throwable>(2);
            this.buffer = null;

            try {
                var channel = this.channel;
                if (channel != null) {
                    channel.close();
                    this.channel = null;
                }
            } catch (Exception exception) {
                throwables.add(exception);
            }

            try {
                var localFile = this.localFile;
                if (localFile != null) {
                    Files.deleteIfExists(localFile);
                    this.localFile = null;
                }
            } catch (Exception exception) {
                throwables.add(exception);
            }

            Exceptions.rethrow(throwables);
        }
    }
}
