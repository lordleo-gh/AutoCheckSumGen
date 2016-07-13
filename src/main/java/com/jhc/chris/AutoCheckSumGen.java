package com.jhc.chris;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

/**
 * Example to watch a directory (or tree) for changes to files and to create MD5 hash
 */

public class AutoCheckSumGen {

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private final String ext;
    private ThreadList runningWriterThreads = ThreadList.getInstance();

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    AutoCheckSumGen(Path dir, boolean recursive, String ext) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;
        this.ext = ext;

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
        } else {
            register(dir);
        }

        createMetafilesOnStartup(dir);

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        System.out.format("Start watching for .%s changes...\n", ext);

        for (;;) {
            // wait for key to be signalled
            WatchKey key;

            try {
                key = watcher.take();
            }
            catch (InterruptedException x) {
               return ;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.out.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                ChangedFile changedfile = new ChangedFile(child.toUri());
                changedfile.kind = kind;

                if (!changedfile.isDirectory()) {
                    // note that .exe or any watched extension as directory will go through when ENTRY_DELETED
                    startWritingMeta(changedfile);
                }
                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    static void usage() {
        System.out.println("usage: java -jar AutoCheckSumGen [-r] <VALID-DIR> <EXT-TO-WATCH>");
        System.exit(-1);
    }

    void createMetafilesOnStartup(Path dir) {
        for (final File file : dir.toFile().listFiles()) {
            if (file.isDirectory() && recursive) {
                createMetafilesOnStartup(dir);
            } else {
                // treat all as modified to force create metadata
                ChangedFile watchedFile = new ChangedFile(file.toURI());
                watchedFile.kind = ENTRY_MODIFY;
                startWritingMeta(watchedFile);
            }
        }
    }

    void startWritingMeta(ChangedFile file){
        if (ChangedFile.getFileExtension(file).equals(ext)) {
            Boolean ThreadExists = false;
            for (Thread WriterThread : runningWriterThreads.getRunningWriterThreads()) {
                if (WriterThread.getName().equals(file.getAbsolutePath())) {
                    ThreadExists = true;
                    break;
                }
            }
            if (!ThreadExists) {
                Thread t = new Thread(new MetadataFileWriter(file));
                t.setName(file.getAbsolutePath());
                runningWriterThreads.getRunningWriterThreads().add(t);
                t.start();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // parse arguments
        if (args.length <= 1 || args.length > 3)
            usage();
        boolean recursive = false;
        int dirArg = 0;
        if (args[0].equals("-r")) {
            if (args.length < 3)
                usage();
            recursive = true;
            dirArg++;
        }

        System.out.println("Initialising");
        // register directory and process its events
        Path dir = Paths.get(args[dirArg]);
        if (Files.exists(dir)) {
            String ext = args[++dirArg].toUpperCase();
            new AutoCheckSumGen(dir, recursive, ext).processEvents();
        } else { usage(); }
    }
}