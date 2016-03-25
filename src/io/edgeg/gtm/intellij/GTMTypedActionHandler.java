package io.edgeg.gtm.intellij;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE;

public class GTMTypedActionHandler implements TypedActionHandler {
    private TypedActionHandler delegate;

    public GTMTypedActionHandler(TypedActionHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
        VirtualFile file = (VirtualFile) dataContext.getData(VIRTUAL_FILE.getName());
        if (file == null) return;
        String path = file.getPath();
        GTMRecord.record(path);
        if (delegate != null) {
            delegate.execute(editor, charTyped, dataContext);
        }
    }
}
