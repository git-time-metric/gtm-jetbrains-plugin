package io.edgeg.gtm.intellij;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;

import java.math.BigDecimal;

public class GTMSaveListener extends FileDocumentManagerAdapter {

    @Override
    public void beforeDocumentSaving(Document document) {
        PluginManager.getLogger().info("beforeDocumentSaving");
        String currentFile = FileDocumentManager.getInstance().getFile(document).getPath();
        GTMRecord.record(currentFile);
    }
}