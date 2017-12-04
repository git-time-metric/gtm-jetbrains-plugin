package io.edgeg.gtm.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class GTMRecord {
    private static final String GTM_VER_REQ = ">= 1.2.5";

    private static final Long RECORD_MIN_THRESHOLD = 30000L; // 30 seconds
    private static final String RECORD_COMMAND = "record";
    private static final String STATUS_OPTION = "--status";
    private static final String VERIFY_COMMAND = "verify";

    private static String gtmExePath = null;
    private static String lastRecordPath = null;
    private static Long lastRecordTime = null;

    static Boolean gtmExeFound = false;
    static Boolean gtmVersionOK = true;
    private static GTMConfig cfg = new GTMConfig();

    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future recordTask;
    private static Long lastRunTime = null;
    private static final Long MAX_RUN_TIME = 2000L; // 2 seconds

    static void record(String path, Project project) {
        Runnable r = new Runnable() {
            public void run() {
                runRecord(path, project);
            }
        };
//        GTMConfig.LOG.info(String.format( "Submit record %s", path));
        submitRecord(r);
    }

    private static synchronized void submitRecord(Runnable r) {

        // is there a task running
        if (recordTask != null && !recordTask.isDone()) {
            // make sure it's not a hung process
            if (lastRunTime != null && System.currentTimeMillis() - lastRunTime > MAX_RUN_TIME) {
                // process is hung, cancel it
                recordTask.cancel(true);
                GTMConfig.LOG.warn("Record task was hung, task cancelled");
                recordTask = executor.submit(r);
            }
            return;
        }

        // only submit a task if there isn't one running already
        if (recordTask == null || recordTask.isDone()) {
            recordTask = executor.submit(r);
        }
    }

    private static void runRecord(String path, Project project) {
//        GTMConfig.LOG.info(String.format("Run record %s", path));

        String status;
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
                    lastRunTime = System.currentTimeMillis();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while (null != (line = reader.readLine())) {
                        builder.append(line);
                    }
                    status = builder.toString();
                } else {

//                    GTMConfig.LOG.info(
//                            String.format(
//                                    "Executing %s %s %s",
//                                    gtmExePath, RECORD_COMMAND, path));

                    Process process = new ProcessBuilder(gtmExePath, RECORD_COMMAND, path).start();
                    lastRunTime = System.currentTimeMillis();
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
            if (widget != null) {
                widget.setText(status);
                widget.updateStatusBar();
            }
        }
    }

    static Boolean initGtmExePath() {
        String gtmExeName = System.getProperty("os.name").startsWith("Windows") ? "gtm.exe" : "gtm";
        String[] gtmPath;
        StringBuilder pathVar = new StringBuilder(System.getenv("PATH"));

        if (System.getProperty("os.name").startsWith("Windows")) {
            // Setup an additional Windows user path
            String userWinBin = System.getProperty("user.home") + File.separator + "gtm";
            gtmPath = new String[]{
                    Paths.get(System.getenv("ProgramFiles"), "gtm").toString(),
                    Paths.get(System.getenv("ProgramFiles(x86)"), "gtm").toString(),
                    userWinBin};
        } else {
            // Setup additional common *nix user paths
            String userBin = System.getProperty("user.home") + File.separator + "bin";
            String userLocalBin = System.getProperty("user.home") + File.separator + "local" + File.separator + "bin";
            gtmPath = new String[]{"/usr/bin", "/bin", "/usr/sbin", "/sbin", "/usr/local/bin/", userBin, userLocalBin};
        }

        for (String aGtmPath : gtmPath) {
            if (!pathVar.toString().contains(aGtmPath)) {
                pathVar.append(File.pathSeparator).append(aGtmPath);
            }
        }

        String result = null;
        String[] pathDirs = pathVar.toString().split(File.pathSeparator);
        for (String pathDir : pathDirs) {
            File exeFile = Paths.get(pathDir).resolve(gtmExeName).toFile();
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

    static Boolean checkVersion() {
        try {
            Process process = new ProcessBuilder(gtmExePath, VERIFY_COMMAND, GTM_VER_REQ).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
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
