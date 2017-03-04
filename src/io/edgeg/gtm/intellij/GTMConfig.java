package io.edgeg.gtm.intellij;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(
    name = "GTMAppSettings",
    storages = {
        @Storage(StoragePathMacros.WORKSPACE_FILE)
    }
)

public class GTMConfig implements PersistentStateComponent<GTMConfig> {
    static Logger LOG = Logger.getInstance("#io.edgeg.gtm.intellij");
    private static GTMConfig CFG = null;

    Boolean statusEnabled = true;

    String gtmNotFound =
            "<B>Git Time Metric (GTM) executable not found.</B>\n" +
            "Install GTM and/or update your system path.\n" +
            "Make sure to restart after installing GTM.\n\n" +
            "See https://github.com/git-time-metric/gtm";

    String gtmVerOutdated =
            "<B>Git Time Metric (GTM) executable is out of date.</B>\n" +
            "The plug-in may not work properly.\n" +
            "Please install the latest GTM version and restart.\n\n" +
            "See https://github.com/git-time-metric/gtm";

    static GTMConfig getInstance() {
        if (CFG == null) {
            CFG = new GTMConfig();
        }
        return CFG;
    }

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
