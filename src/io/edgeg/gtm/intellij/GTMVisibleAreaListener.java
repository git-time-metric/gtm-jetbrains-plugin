package io.edgeg.gtm.intellij;

import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

class GTMVisibleAreaListener implements VisibleAreaListener {

    @Override
    public void visibleAreaChanged(VisibleAreaEvent visibleAreaEvent) {
        final FileDocumentManager instance = FileDocumentManager.getInstance();
        final VirtualFile file = instance.getFile(visibleAreaEvent.getEditor().getDocument());
        if (file != null) {
            Project proj = visibleAreaEvent.getEditor().getProject();
            if (proj != null) {
                proj.getComponent(GTMProject.class).getGTMBackgroundRunner().record(file.getPath());
            }
        }
    }
}
