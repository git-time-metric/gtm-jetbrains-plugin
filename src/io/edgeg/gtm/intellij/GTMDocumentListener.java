package io.edgeg.gtm.intellij;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.math.BigDecimal;

public class GTMDocumentListener implements DocumentListener {
    @Override
    public void beforeDocumentChange(DocumentEvent documentEvent) {
    }

    @Override
    public void documentChanged(DocumentEvent documentEvent) {
        final FileDocumentManager instance = FileDocumentManager.getInstance();
        final VirtualFile file = instance.getFile(documentEvent.getDocument());
        if (file != null) {
            GTMRecord.record(file.getPath());
        }
    }
}