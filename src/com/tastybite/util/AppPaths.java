package com.tastybite.util;

import java.io.File;

/**
 * Where the app keeps its settings (db.properties) and the manager's food photos (menu_images).
 *
 * - Started from a project or a folder that contains db.properties (Eclipse, java -jar, a launcher script):
 *   that folder is used, exactly as before.
 * - Started like a normal Mac or Windows app (double-click): the folder "TastyBite" inside the
 *   person's home folder is used, because such apps start in an unrelated folder.
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
        File here = new File("").getAbsoluteFile();
        if (new File(here, "db.properties").isFile()) {
            return here;
        }
        File home = new File(System.getProperty("user.home"), "TastyBite");
        home.mkdirs();
        return home;
    }
}
