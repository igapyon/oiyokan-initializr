package jp.oiyokan.initializr.ctrl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import jp.oiyokan.dto.OiyoSettingsEntitySet;

@Controller
@SessionAttributes("scopedTarget.settingsBean")
public class ThInitializrEditEntityCtrl {
    private static final Log log = LogFactory.getLog(ThInitializrEditEntityCtrl.class);

    @Autowired
    private OiyokanSettingsWrapper settingsBean;

    /////////////////////////
    // Select table

    @RequestMapping(value = { "/initializrEditEntity" }, params = { "edit" }, method = { RequestMethod.POST })
    public String selectEntity(Model model, ThInitializrBean initializrBean, @RequestParam("edit") String entityName,
            BindingResult result) throws IOException {
        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        OiyoSettingsEntitySet entitySet = null;
        for (OiyoSettingsEntitySet look : settingsBean.getSettings().getEntitySet()) {
            if (look.getName().equals(entityName)) {
                entitySet = look;
                break;
            }
        }
        if (entitySet == null) {
            return "oiyokan/initializrTop";
        }

        model.addAttribute("entitySet", entitySet);
        return "oiyokan/initializrEditEntity";
    }
}
