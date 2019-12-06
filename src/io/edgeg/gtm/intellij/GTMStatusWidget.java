package io.edgeg.gtm.intellij;

import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class GTMStatusWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe, StatusBarWidget.TextPresentation {
    private static final String id = GTMStatusWidget.class.getName();
    private final AtomicBoolean opened = new AtomicBoolean();
    private String myText = "";

    private GTMStatusWidget(@NotNull Project project) {
        super(project);
    }

    private void runUpdateLater() {
        UIUtil.invokeLaterIfNeeded(() -> {
            if (opened.get()) {
                runUpdate();
            }
        });
    }

    static GTMStatusWidget create(@NotNull Project project) {
        return new GTMStatusWidget(project);
    }

    @Override
    public StatusBarWidget copy() {
        return new GTMStatusWidget(myProject);
    }

    @NotNull
    @Override
    public String ID() {
        return id;
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
        return this;
    }

    @NotNull
    @Override
    public String getText() {
        return myText;
    }

    @NotNull
    @Override
    public String getMaxPossibleText() {
        return "";
    }

    void setText(String txt) {
        if (txt == null) { return; }
        if (Objects.equals(txt,"")) { myText = "GTM"; return; }
        myText = txt.replaceAll("\\s*\\d*s\\s*$", "");
        myText = "GTM: " + StringUtils.join(myText.split("\\s+"), " ");
    }

    @Override
    public float getAlignment() {
        return Component.LEFT_ALIGNMENT;
    }

    @Nullable
    @Override
    public String getTooltipText() {
        if (!GTMRecord.gtmExeFound) { return GTMConfig.getInstance().gtmNotFound; }
        if (!GTMRecord.gtmVersionOK) { return GTMConfig.getInstance().gtmVerOutdated; }
        return "<B>Git Time Metric (GTM)</B>";
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        runUpdate();
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        runUpdate();
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        runUpdate();
    }

    void updateStatusBar() {
        if (myStatusBar != null) {
            myStatusBar.updateWidget(ID());
        }
    }

    private void runUpdate() {
        updateStatusBar();
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> runUpdate();
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        myConnection.subscribe(UISettingsListener.TOPIC, uiSettings -> runUpdateLater());
    }

    void installed() {
        opened.compareAndSet(false, true);
    }

    void uninstalled() {
        opened.compareAndSet(true, false);
    }
}
