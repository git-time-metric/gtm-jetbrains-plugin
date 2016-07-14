package io.edgeg.gtm.intellij;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.ui.MessageType.ERROR;
import static com.intellij.openapi.ui.MessageType.WARNING;

public class GTMProject extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    private GTMStatusWidget myStatusWidget;

    public GTMProject(@NotNull Project project) {
        super(project);
    }

    @Override
    public void initComponent() {
//        LOG.info("initComponent");
        myStatusWidget = GTMStatusWidget.create(myProject);
    }

    @Override
    public void disposeComponent() {
//        LOG.info("disposeComponent");
        uninstallGtmWidget();
    }

    @Override
    public void projectOpened() {
//        LOG.info("projectOpened");
        installGtmWidget();
    }

    @Override
    public void projectClosed() {
//        LOG.info("projectClosed");
        uninstallGtmWidget();
    }

    private void installGtmWidget() {
//        LOG.info("installGtmWidget");
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
        if (statusBar != null) {
            statusBar.addWidget(myStatusWidget, myProject);
            myStatusWidget.installed();
            if (!GTMRecord.initGtmExePath()) {
                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder(
                                "<B>Git Time Metric (GTM) executable not found.</B>\n" +
                                "Install GTM and/or update your system path.\n" +
                                "Make sure to restart after installing GTM.\n\n" +
                                "See https://github.com/git-time-metric/gtm", ERROR, null)
                        .setFadeoutTime(30000)
                        .createBalloon()
                        .show(RelativePoint.getSouthEastOf(statusBar.getComponent()),
                                Balloon.Position.atRight);
                return;
            }
            if (!GTMRecord.checkVersion()) {
                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder(
                                "<B>Git Time Metric (GTM) executable is out of date.</B>\n" +
                                "The plug-in may not work properly.\n" +
                                "Please install the latest GTM version and restart.\n\n" +
                                "See https://github.com/git-time-metric/gtm", WARNING, null)
                        .setFadeoutTime(30000)
                        .createBalloon()
                        .show(RelativePoint.getSouthEastOf(statusBar.getComponent()),
                                Balloon.Position.atRight);
            }
        }
    }

    private void uninstallGtmWidget() {
//        LOG.info("uninstallGtmWidget");
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
        if (statusBar != null) {
            statusBar.removeWidget(myStatusWidget.ID());
            myStatusWidget.uninstalled();
        }

    }
}
