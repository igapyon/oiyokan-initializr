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
    public String index(Model model, ThInitializrBean initializrBean, BindingResult result) {
        log.info("INFO: ルート `/initializr`(GET) が開かれた.");
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess("最初にデータベース設定をセットアップしてください。");
        initializrBean.setMsgError(null);

        // 一旦内容クリア
        settingsBean.setSettings(new OiyoSettings());
        settingsBean.setCurrentDbSettingName(null);

        model.addAttribute("settings", settingsBean.getSettings());

        return "oiyokan/initializrTop";
    }

    @RequestMapping(value = { "/initializrExit" }, method = { RequestMethod.GET })
    public String exit(Model model, ThInitializrBean initializrBean, BindingResult result) {
        log.info("INFO: Exit `/initializrExit`(GET) が開かれた.");
        initializrBean = new ThInitializrBean();
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess("Oiyokan Initializr のセッション情報を初期化しました。");
        initializrBean.setMsgError(null);

        // セッション情報の内容も一旦クリア
        settingsBean.setSettings(new OiyoSettings());
        model.addAttribute("settings", settingsBean.getSettings());

        return "oiyokan/initializrTop";
    }

    @RequestMapping(value = { "/initializr" }, params = "generate", method = { RequestMethod.POST })
    public String generate(Model model, ThInitializrBean initializrBean, HttpServletResponse response)
            throws IOException {
        log.info("INFO: GENERATE `/initializr`(POST:generate) がクリックされた.");
        model.addAttribute("settings", settingsBean.getSettings());

        for (OiyoSettingsDatabase database : settingsBean.getSettings().getDatabase()) {
            // JSON書き込み直前に、プレーンパスワードを除去
            if (database.getJdbcPassEnc() == null || database.getJdbcPassEnc().trim().length() == 0) {
                // データベース設定を暗号化。もとのプレーンテキストパスワードは除去.
                if (database.getJdbcPassPlain() == null) {
                    database.setJdbcPassPlain("");
                }
                database.setJdbcPassEnc(
                        OiyoEncryptUtil.encrypt(database.getJdbcPassPlain(), new OiyoInfo().getPassphrase()));
                database.setJdbcPassPlain(null);
            }
        }

        //////////////////////////////////////////////////////////
        // Write settings info into oiyokan-settings.json

        String jsonString = null;
        try {
            jsonString = OiyokanInitializrUtil.oiyoSettings2String(settingsBean.getSettings());
        } catch (IOException ex) {
            // [IYI4201] ERROR: Fail to generate json file.
            log.error(OiyokanInitializrMessages.IYI4201 + ": " + ex.toString(), ex);
            throw ex;
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
            throw ex;
        }

        // [IYI1002] Oiyokan Initializr End.
        log.info(OiyokanInitializrMessages.IYI1002);
        return null;
    }
}
