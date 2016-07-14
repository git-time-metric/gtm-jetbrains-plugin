package io.edgeg.gtm.intellij;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

class GTMVisibleAreaListener implements VisibleAreaListener {
    private final Logger LOG = Logger.getInstance(getClass());

    @Override
    public void visibleAreaChanged(VisibleAreaEvent visibleAreaEvent) {
//        LOG.info("visibleAreaChanged visibleAreaEvent");
        final FileDocumentManager instance = FileDocumentManager.getInstance();
        final VirtualFile file = instance.getFile(visibleAreaEvent.getEditor().getDocument());
        if (file != null) {
            GTMRecord.record(file.getPath(), visibleAreaEvent.getEditor().getProject());
        }
    }
}
