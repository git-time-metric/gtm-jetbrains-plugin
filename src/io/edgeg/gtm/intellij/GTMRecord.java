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

class GTMRecord {
    private static final String GTM_VER_REQ = ">= 1.2.5";
    private static final String RECORD_COMMAND = "record";
    private static final String STATUS_OPTION = "--status";
    private static final String VERIFY_COMMAND = "verify";
    private static String gtmExePath = null;

    static Boolean gtmExeFound = false;
    static Boolean gtmVersionOK = true;
    private static GTMConfig cfg = new GTMConfig();

    static void record(String path, Project project) {
        String status;
        if (StringUtils.isBlank(path)) return;
        if (!gtmExeFound) {
            status = "Error!";
        } else {
            try {
                if (cfg.statusEnabled) {

//                    GTMConfig.LOG.info(
//                            String.format(
//                                    "Executing %s %s %s %s",
//                                    gtmExePath, RECORD_COMMAND, STATUS_OPTION, path));

                    Process process = new ProcessBuilder(gtmExePath, RECORD_COMMAND, STATUS_OPTION, path).start();
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
