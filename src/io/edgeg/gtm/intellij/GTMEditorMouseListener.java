package io.edgeg.gtm.intellij;

import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.math.BigDecimal;

public class GTMEditorMouseListener implements EditorMouseListener {
    @Override
    public void mousePressed(EditorMouseEvent editorMouseEvent) {
        final FileDocumentManager instance = FileDocumentManager.getInstance();
        final VirtualFile file = instance.getFile(editorMouseEvent.getEditor().getDocument());
        if (file != null) {
            GTMRecord.record(file.getPath());
        }
    }

    @Override
    public void mouseClicked(EditorMouseEvent editorMouseEvent) {
    }

    @Override
    public void mouseReleased(EditorMouseEvent editorMouseEvent) {
    }

    @Override
    public void mouseEntered(EditorMouseEvent editorMouseEvent) {
    }

    @Override
    public void mouseExited(EditorMouseEvent editorMouseEvent) {
    }
}
