package io.edgeg.gtm.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import org.jetbrains.annotations.NotNull;

public class GTMAction extends AnAction {
    static {
        EditorActionManager actionManager = EditorActionManager.getInstance();
        TypedAction typedAction = actionManager.getTypedAction();
        typedAction.setupHandler(new GTMTypedActionHandler(typedAction.getHandler()));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    }
}
