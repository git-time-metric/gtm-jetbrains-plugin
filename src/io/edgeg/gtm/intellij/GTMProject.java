package io.edgeg.gtm.intellij;

import com.intellij.openapi.components.AbstractProjectComponent;
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
    private GTMStatusWidget myStatusWidget;

    public GTMProject(@NotNull Project project) {
        super(project);
    }

    @Override
    public void initComponent() {
        myStatusWidget = GTMStatusWidget.create(myProject);
    }

    @Override
    public void disposeComponent() {
        uninstallGtmWidget();
    }

    @Override
    public void projectOpened() {
        installGtmWidget();
    }

    @Override
    public void projectClosed() {
        uninstallGtmWidget();
    }

    private void installGtmWidget() {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
        if (statusBar != null) {
            statusBar.addWidget(myStatusWidget, myProject);
            myStatusWidget.installed();
            if (!GTMRecord.initGtmExePath()) {
                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder(GTMConfig.getInstance().gtmNotFound, ERROR, null)
                        .setFadeoutTime(30000)
                        .createBalloon()
                        .show(RelativePoint.getSouthEastOf(statusBar.getComponent()),
                                Balloon.Position.atRight);
                return;
            }
            if (!GTMRecord.checkVersion()) {
                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder(GTMConfig.getInstance().gtmVerOutdated, WARNING, null)
                        .setFadeoutTime(30000)
                        .createBalloon()
                        .show(RelativePoint.getSouthEastOf(statusBar.getComponent()),
                                Balloon.Position.atRight);
            }
        }
    }

    private void uninstallGtmWidget() {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
        if (statusBar != null) {
            statusBar.removeWidget(myStatusWidget.ID());
            myStatusWidget.uninstalled();
        }

    }
}
