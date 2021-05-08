package jp.oiyokan.initializr.ctrl;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import jp.oiyokan.dto.OiyoSettings;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OiyokanSettingsWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    private final OiyoSettings settings = new OiyoSettings();

    public OiyoSettings getSettings() {
        return settings;
    }
}
