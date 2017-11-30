package io.edgeg.gtm.intellij;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.Semaphore;

public class GTMBackgroundRunner extends Task.Backgroundable {
    private static final Long RECORD_MIN_UNIT = 1000L; // 1 second
    private static final Long RECORD_MIN_THRESHOLD = 30000L; // 30 seconds
    private Project project;
    private Semaphore semaphore = new Semaphore(1);
    private String queuedPath = "";
    private String lastPath = "";
    private Boolean running = false;
    private Long lastRecordTime = null;


    GTMBackgroundRunner(Project project) {
        super(project, "GTM Record");
        this.project = project;
    }

    public void run(@NotNull ProgressIndicator indicator) {
        do {
            Long startTime = System.currentTimeMillis();
            String path = queuedPath;
            queuedPath = "";
            semaphore.release();
            GTMRecord.record(path, project);
            Long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime < RECORD_MIN_UNIT) {
                try {
                    Thread.sleep(RECORD_MIN_UNIT - elapsedTime);
                } catch (InterruptedException e) {
                    GTMConfig.LOG.error(e);
                }
            }
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                GTMConfig.LOG.error(e);
            }
        } while (!queuedPath.isEmpty());
        running = false;
        semaphore.release();
    }
    void record(String path) {
        Long currentTime = System.currentTimeMillis();
        if (Objects.equals(lastPath, path) && lastRecordTime != null && currentTime - lastRecordTime <= RECORD_MIN_THRESHOLD) {
            return;
        }
        lastRecordTime = currentTime;

        try {
            semaphore.acquire();
            queuedPath = path;
            lastPath = path;
            if (!running) {
                running = true;
                ProgressManager.getInstance().run(this);
            } else {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            GTMConfig.LOG.error(e);
        }
    }
}
