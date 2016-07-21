package io.edgeg.gtm.intellij;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import org.jetbrains.annotations.NotNull;

public class GitTimeMetric implements ApplicationComponent {
    private EditorMouseListener mouseListener = new GTMEditorMouseListener();
    private VisibleAreaListener visibleAreaListener = new GTMVisibleAreaListener();

    public GitTimeMetric() {
    }

    public void initComponent() {
        EditorFactory.getInstance().getEventMulticaster().addEditorMouseListener(mouseListener);
        EditorFactory.getInstance().getEventMulticaster().addVisibleAreaListener(visibleAreaListener);
    }

    public void disposeComponent() {
        EditorFactory.getInstance().getEventMulticaster().removeEditorMouseListener(mouseListener);
        EditorFactory.getInstance().getEventMulticaster().removeVisibleAreaListener(visibleAreaListener);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "GitTimeMetric";
    }
}
