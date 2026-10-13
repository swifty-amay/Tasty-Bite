package com.tastybite.util;

import java.io.File;

/**
 * Where the app keeps its settings (db.properties) and the manager's food photos (menu_images).
 *
 * Looked for in this order, the first folder that contains db.properties wins:
 * 1. the folder the app was started from (Eclipse, command line),
 * 2. the folder of the app itself, next to TastyBite.exe (so a settings file shipped inside the
 *    download is found no matter how the app was started),
 * 3. the folder "TastyBite" in the person's home folder, which is created if needed.
 */
public final class AppPaths {

    private static final File DATA_DIR = findDataDir();

    private AppPaths() { }

    public static File dataDir() { return DATA_DIR; }

    public static File settingsFile() { return new File(DATA_DIR, "db.properties"); }

    public static File imageDir() { return new File(DATA_DIR, "menu_images"); }

    /** Photo paths are stored relative (menu_images/abc.jpg); this finds the real file. */
    public static File resolve(String path) {
        File f = new File(path);
        return f.isAbsolute() ? f : new File(DATA_DIR, path);
    }

    private static File findDataDir() {
        // 1. the folder the app was started from (Eclipse, command line)
        File here = new File("").getAbsoluteFile();
        if (hasSettings(here)) {
            return here;
        }
        // 2. next to the app itself (TastyBite.exe and its app folder), wherever it was started from
        for (File dir : appFolders()) {
            if (hasSettings(dir)) {
                return dir;
            }
        }
        // 3. the TastyBite folder in the person's home folder
        File home = new File(System.getProperty("user.home"), "TastyBite");
        home.mkdirs();
        return home;
    }

    private static boolean hasSettings(File dir) {
        return dir != null && new File(dir, "db.properties").isFile();
    }

    /** The folder holding the jar and its parent (for a packaged app: the app folder and the folder with the .exe). */
    private static java.util.List<File> appFolders() {
        java.util.List<File> dirs = new java.util.ArrayList<>();
        try {
            File jar = new File(AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File dir = jar.isFile() ? jar.getParentFile() : null;
            if (dir != null) {
                dirs.add(dir);
                if (dir.getParentFile() != null) dirs.add(dir.getParentFile());
            }
        } catch (Exception ignored) {
            // running from a folder of classes or from an unusual location: nothing to add
        }
        return dirs;
    }
}
