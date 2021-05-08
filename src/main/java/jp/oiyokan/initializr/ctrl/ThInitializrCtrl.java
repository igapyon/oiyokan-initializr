package jp.oiyokan.initializr.ctrl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.initializr.OiyokanInitializrMessages;
import jp.oiyokan.initializr.OiyokanInitializrUtil;
import jp.oiyokan.util.OiyoEncryptUtil;

@Controller
@SessionAttributes("scopedTarget.settingsBean")
public class ThInitializrCtrl {
    private static final Log log = LogFactory.getLog(ThInitializrCtrl.class);

    @Autowired
    private OiyokanSettingsWrapper settingsBean;

    @RequestMapping(value = { "/initializr" }, method = { RequestMethod.GET })
    public String index(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        return "oiyokan/initializrTop";
    }

    /////////////////////////
    // Select table

    // TODO これまだ実装してない
    @RequestMapping(value = { "/initializr" }, params = { "selectTable" }, method = { RequestMethod.POST })
    public String selectTable(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        log.info("processView:" + initializrBean.isProcessView());

        OiyoSettings oiyoSettings = settingsBean.getSettings();

        // ソートなど
        OiyoInfo oiyoInfo = new OiyoInfo();

        // TODO FIXME どのデータベースか確認。
        ThInitializrSetupDatabaseCtrl.connTestInternal(initializrBean, oiyoSettings.getDatabase().get(0), null);

        OiyokanInitializrUtil.tuneSettings(oiyoInfo, oiyoSettings, initializrBean.isConvertCamel(),
                initializrBean.isFilterTreatNullAsBlank);

        initializrBean.getEntitySets().clear();
        for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
            initializrBean.getEntitySets().add(new ThInitializrBean.EntitySet(entitySet.getName(), true, false));
        }

        OiyoSettingsDatabase database = oiyoSettings.getDatabase().get(0);
        if (database.getJdbcUser() == null) {
            log.info("selectTable: database.jdbcUser was null");
            database.setJdbcUser("");
        }
        if (database.getJdbcPassPlain() == null) {
            log.info("selectTable: database.jdbcPassPlain was null");
            database.setJdbcPassPlain("");
        }
        if (database.getJdbcPassEnc() == null) {
            log.info("selectTable: database.jdbcPassEnc was null");
            database.setJdbcPassPlain("");
        }

        // TODO Entityの設定状況により分岐
        return "oiyokan/initializrSelectEntity";
    }

    @RequestMapping(value = { "/initializr" }, params = "generate", method = { RequestMethod.POST })
    public String generate(Model model, ThInitializrBean initializrBean, HttpServletResponse response)
            throws IOException {
        model.addAttribute("settings", settingsBean.getSettings());

        // TODO すべてデータベースにこの同様処理が必要。
        OiyoSettingsDatabase database = settingsBean.getSettings().getDatabase().get(0);

        // JSON書き込み直前に、プレーンパスワードを除去
        if (database.getJdbcPassEnc() == null || database.getJdbcPassEnc().trim().length() == 0) {
            // データベース設定を暗号化。もとのプレーンテキストパスワードは除去.
            database.setJdbcPassEnc(
                    OiyoEncryptUtil.encrypt(database.getJdbcPassPlain(), new OiyoInfo().getPassphrase()));
            database.setJdbcPassPlain(null);
        }

        //////////////////////////////////////////////////////////
        // Write settings info into oiyokan-settings.json

        String jsonString = null;
        try {
            jsonString = OiyokanInitializrUtil.oiyoSettings2String(settingsBean.getSettings());
        } catch (IOException ex) {
            // [IYI4201] ERROR: Fail to generate json file.
            log.error(OiyokanInitializrMessages.IYI4201 + ": " + ex.toString(), ex);
        }

        try {
            final byte[] zipFile = OiyokanInitializrUtil
                    .packageZipFile(new File("./src/main/resources/oiyokan-web-template"), jsonString);

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=oiyokan-demo.zip");

            final OutputStream outStream = response.getOutputStream();
            IOUtils.copy(new ByteArrayInputStream(zipFile), outStream);
            outStream.flush();

            initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI5102);

            // [IYI5102] Check the `oiyokan-demo.zip`.
            log.info(OiyokanInitializrMessages.IYI5102 + ": oiyokan-demo.zip");
        } catch (IOException ex) {
            // [IYI5201] ERROR: Fail to generate zip file.
            log.error(OiyokanInitializrMessages.IYI5201, ex);
        }

        // [IYI1002] Oiyokan Initializr End.
        log.info(OiyokanInitializrMessages.IYI1002);
        return null;
    }
}
