package io.edgeg.gtm.intellij;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(
    name = "GTMAppSettings",
    storages = {
        @Storage(StoragePathMacros.WORKSPACE_FILE)
    }
)

public class GTMConfig implements PersistentStateComponent<GTMConfig> {
    public Boolean statusEnabled = true;

    @Nullable
    @Override
    public GTMConfig getState() {
        return this;
    }

    @Override
    public void loadState(GTMConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
