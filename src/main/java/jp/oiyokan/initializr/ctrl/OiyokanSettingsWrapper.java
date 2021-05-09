package jp.oiyokan.initializr.ctrl;

import java.io.Serializable;
import java.util.ArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import jp.oiyokan.dto.OiyoSettings;

/**
 * セッション上に記憶する情報。
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OiyokanSettingsWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    private String currentDbSettingName = null;

    private OiyoSettings settings = new OiyoSettings();

    public OiyoSettings getSettings() {
        if (settings.getNamespace() == null) {
            settings.setNamespace("Oiyokan"); // Namespace of OData
        }
        if (settings.getContainerName() == null) {
            settings.setContainerName("Container"); // Container of OData
        }
        if (settings.getDatabase() == null) {
            settings.setDatabase(new ArrayList<>());
        }
        if (settings.getEntitySet() == null) {
            settings.setEntitySet(new ArrayList<>());
        }

        return settings;
    }

    public void setSettings(OiyoSettings settings) {
        this.settings = settings;
    }

    public String getCurrentDbSettingName() {
        return currentDbSettingName;
    }

    public void setCurrentDbSettingName(String currentDbSettingName) {
        this.currentDbSettingName = currentDbSettingName;
    }
}
