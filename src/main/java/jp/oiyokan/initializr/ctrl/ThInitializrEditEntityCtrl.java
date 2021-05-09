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
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(ThInitializrEditEntityCtrl.class);

    @Autowired
    private OiyokanSettingsWrapper settingsBean;

    /////////////////////////
    // Select table

    @RequestMapping(value = { "/initializrEditEntity" }, params = { "edit" }, method = { RequestMethod.POST })
    public String selectEntity(Model model, ThInitializrBean initializrBean, @RequestParam("edit") String entityName,
            BindingResult result) throws IOException {
        log.info("INFO: `/initializrEditEntity`(POST:edit) が開かれた.");

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

    @RequestMapping(value = { "/initializrEditEntity" }, params = { "applyChanges" }, method = { RequestMethod.POST })
    public String applyEntityChanges(Model model, ThInitializrBean initializrBean, OiyoSettingsEntitySet entitySet,
            BindingResult result) throws IOException {
        log.info("INFO: `/initializrEditEntity`(POST:applyChanges) が開かれた.");

        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        OiyoSettingsEntitySet entitySetTarget = null;
        for (OiyoSettingsEntitySet lookup : settingsBean.getSettings().getEntitySet()) {
            if (lookup.getName().equals(entitySet.getName())) {
                entitySetTarget = lookup;
            }
        }
        if (entitySetTarget == null) {
            initializrBean.setMsgSuccess("ERROR.");
            return "oiyokan/initializrEditEntity";
        }

        entitySetTarget.setDescription(entitySet.getDescription());
        entitySetTarget.setCanCreate(entitySet.getCanCreate());
        entitySetTarget.setCanRead(entitySet.getCanRead());
        entitySetTarget.setCanUpdate(entitySet.getCanUpdate());
        entitySetTarget.setCanDelete(entitySet.getCanDelete());
        entitySetTarget.setOmitCountAll(entitySet.getOmitCountAll());
        entitySetTarget.setJdbcStmtTimeout(entitySet.getJdbcStmtTimeout());

        initializrBean.setMsgSuccess("Entity Change applied.");
        return "oiyokan/initializrTop";
    }
}
