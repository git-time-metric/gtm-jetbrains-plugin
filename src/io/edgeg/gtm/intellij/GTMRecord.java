package io.edgeg.gtm.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

class GTMRecord {
    private static final String GTM_VER_REQ = ">= 1.0-beta.6";

    private static final Long RECORD_MIN_THRESHOLD = 30000L; // 30 seconds
    private static final String RECORD_COMMAND = "record";
    private static final String STATUS_OPTION = "--status";
    private static final String VERIFY_COMMAND = "verify";

    private static String gtmExePath = null;
    private static String lastRecordPath = null;
    private static Long lastRecordTime = null;

    public static Boolean gtmExeFound = true;
    public static Boolean gtmVersionOK = true;
    private static GTMConfig cfg = new GTMConfig();

    static void record(String path, Project project) {
        String status = "";
        if (StringUtils.isBlank(path)) return;
        if (!gtmExeFound) {
            status = "Error!";
        } else {
            try {
                Long currentTime = System.currentTimeMillis();
                if (Objects.equals(lastRecordPath, path)) {
                    if (lastRecordTime != null && currentTime - lastRecordTime <= RECORD_MIN_THRESHOLD) {
                        return;
                    }
                }
                lastRecordPath = path;
                lastRecordTime = currentTime;
                if (cfg.statusEnabled) {

//                    GTMConfig.LOG.info(
//                            String.format(
//                                    "Executing %s %s %s %s",
//                                    gtmExePath, RECORD_COMMAND, STATUS_OPTION, path));

                    Process process = new ProcessBuilder(gtmExePath, RECORD_COMMAND, STATUS_OPTION, path).start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    status = builder.toString();
                } else {

//                    GTMConfig.LOG.info(
//                            String.format(
//                                    "Executing %s %s %s",
//                                    gtmExePath, RECORD_COMMAND, path));

                    Process process = new ProcessBuilder(gtmExePath, RECORD_COMMAND, path).start();
                    status = "";
                }

            } catch (IOException e) {
                status = "Error!";
                if (initGtmExePath()) {
                    checkVersion();
                }
                GTMConfig.LOG.warn(String.format(
                        "Error executing %s %s %s", gtmExePath, RECORD_COMMAND, path), e);
            }
        }
        if (project != null) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            GTMStatusWidget widget = (GTMStatusWidget) statusBar.getWidget(GTMStatusWidget.class.getName());
            widget.setText(status);
            widget.updateStatusBar();
        }
    }

    public static Boolean initGtmExePath() {
        String gtmExeName;
        String[] gtmPath;
        if (System.getProperty("os.name").startsWith("Windows")) {
            gtmExeName = "gtm.exe";
            gtmPath = new String[]{
                Paths.get(System.getenv("ProgramFiles"), "gtm", "bin").toString(),
                Paths.get(System.getenv("ProgramFiles(x86)"), "gtm", "bin").toString()};
        } else {
            gtmExeName = "gtm";
            gtmPath = new String[]{"/usr/bin", "/bin", "/usr/sbin", "/sbin", "/usr/local/bin/"};
        }

        String pathVar = System.getenv("PATH");
        for (int i = 0; i < gtmPath.length; i++) {
            if (!pathVar.contains(gtmPath[i])) {
                pathVar += File.pathSeparator + gtmPath[i];
            }
        }

        String result = null;
        String[] pathDirs = pathVar.split(File.pathSeparator);
        for (String pathDir : pathDirs) {
            Path toExe = Paths.get(pathDir, gtmExeName);
            File exeFile = toExe.toFile();
            if (exeFile.getAbsoluteFile().exists() && exeFile.getAbsoluteFile().canExecute()) {
                result = exeFile.getAbsolutePath();
                break;
            }
        }
        gtmExeFound = (result != null);
        gtmExePath = result;
        if (!gtmExeFound) {
            GTMConfig.LOG.warn("Unable to find executable gtm in PATH");
        }
        return gtmExeFound;
    }

    public static Boolean checkVersion() {
        try {
            Process process = new ProcessBuilder(gtmExePath, VERIFY_COMMAND, GTM_VER_REQ).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
            }
            gtmVersionOK = (Objects.equals(builder.toString(), "true"));
        } catch (IOException e) {
            GTMConfig.LOG.warn(String.format(
                    "Error executing %s %s %s", gtmExePath, VERIFY_COMMAND, GTM_VER_REQ), e);
            gtmVersionOK = false;
        }
        return gtmVersionOK;
    }
}
