package io.edgeg.gtm.intellij;

import com.intellij.AppTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

public class GitTimeMetric implements ApplicationComponent {

    public static MessageBusConnection connection;

    public GitTimeMetric() {
    }

    public void initComponent() {
        setupEventListeners();
    }

    private void setupEventListeners() {
        ApplicationManager.getApplication().invokeLater(new Runnable(){
            public void run() {

                // save file
                MessageBus bus = ApplicationManager.getApplication().getMessageBus();
                connection = bus.connect();
                connection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, new GTMSaveListener());

                // edit document
                EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new GTMDocumentListener());

                // mouse press
                EditorFactory.getInstance().getEventMulticaster().addEditorMouseListener(new GTMEditorMouseListener());

                // scroll document
                EditorFactory.getInstance().getEventMulticaster().addVisibleAreaListener(new GTMVisibleAreaListener());
            }
        });
    }

    public void disposeComponent() {
        try {
            connection.disconnect();
        } catch(Exception e) { }
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "GitTimeMetric";
    }
}
