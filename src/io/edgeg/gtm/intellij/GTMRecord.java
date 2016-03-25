package io.edgeg.gtm.intellij;

import com.intellij.ide.plugins.PluginManager;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class GTMRecord {
    private static final Long RECORD_MIN_THRESHOLD = 30000L; // 30 seconds
    private static final String GTM_EXE_NAME = "gtm";
    private static final String GTM_COMMAND = "record";

    private static String gtmExePath = null;
    private static String lastRecordPath = null;
    private static Long lastRecordTime = null;
    private static Boolean unableToFindGtmExe = false;

    public static void record(String path) {
        if (StringUtils.isBlank(path)) return;
        if (unableToFindGtmExe) return;
        try {
            Long currentTime = System.currentTimeMillis();
            if (Objects.equals(lastRecordPath, path)) {
                if (lastRecordTime != null && currentTime - lastRecordTime < RECORD_MIN_THRESHOLD) {
                    return;
                }
            }
            lastRecordPath = path;
            lastRecordTime = currentTime;
            if (gtmExePath == null) {
                gtmExePath = initGtmExePath(System.getenv("PATH"));
                if (StringUtils.isBlank(gtmExePath)) return;
            }
            Process process = new ProcessBuilder(GTM_EXE_NAME, GTM_COMMAND, path).start();
        } catch (IOException e) {
            PluginManager.getLogger().error(String.format("Error executing %s %s with parameter %s", gtmExePath, GTM_COMMAND, path), e);
        }
    }

    private static String initGtmExePath(String pathVar) {
        String result = null;
        String[] pathDirs = pathVar.split(File.pathSeparator);
        if (pathDirs != null) {
            for (String pathDir : pathDirs) {
                Path toExe = Paths.get(pathDir, GTM_EXE_NAME);
                File exeFile = toExe.toFile();
                if (exeFile.exists() && exeFile.canExecute()) {
                    result = exeFile.getAbsolutePath();
                    break;
                }
            }
        }
        if (result == null) {
            unableToFindGtmExe = true;
            PluginManager.getLogger().error("Unable to find executable gtm in PATH");
        }
        return result;
    }
}
