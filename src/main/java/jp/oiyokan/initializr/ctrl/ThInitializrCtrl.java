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
        // [IYI6101] INFO: ルート `/initializr`(GET) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6101);

        model.addAttribute("initializrBean", initializrBean);

        // [IYI7101] INFO: 最初にデータベース設定をセットアップしてください。
        initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7101);
        log.info(OiyokanInitializrMessages.IYI7101);
        initializrBean.setMsgError(null);

        // 一旦内容クリア
        settingsBean.setSettings(new OiyoSettings());
        settingsBean.setCurrentDbSettingName(null);

        model.addAttribute("settings", settingsBean.getSettings());

        return "oiyokan/initializrTop";
    }

    @RequestMapping(value = { "/initializrExit" }, method = { RequestMethod.GET })
    public String exit(Model model, ThInitializrBean initializrBean, BindingResult result) {
        // [IYI6102] INFO: Exit `/initializrExit`(GET) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6102);

        initializrBean = new ThInitializrBean();
        model.addAttribute("initializrBean", initializrBean);

        // [IYI7102] INFO: Oiyokan Initializr のセッション情報を初期化しました。
        initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7102);
        log.info(OiyokanInitializrMessages.IYI7102);
        initializrBean.setMsgError(null);

        // セッション情報の内容も一旦クリア
        settingsBean.setSettings(new OiyoSettings());
        model.addAttribute("settings", settingsBean.getSettings());

        return "oiyokan/initializrTop";
    }

    @RequestMapping(value = { "/initializr" }, params = "generate", method = { RequestMethod.POST })
    public String generate(Model model, ThInitializrBean initializrBean, HttpServletResponse response)
            throws IOException {
        // [IYI6103] INFO: GENERATE `/initializr`(POST:generate) がクリックされた.
        log.info(OiyokanInitializrMessages.IYI6103);

        model.addAttribute("settings", settingsBean.getSettings());

        for (OiyoSettingsDatabase database : settingsBean.getSettings().getDatabase()) {
            // JSON書き込み直前に、JDBCの暗号化パスワードが未設定であればこれを設定
            if (database.getJdbcPassEnc() == null || database.getJdbcPassEnc().trim().length() == 0) {
                if (database.getJdbcPassPlain() == null) {
                    database.setJdbcPassPlain("");
                }
                database.setJdbcPassEnc(
                        OiyoEncryptUtil.encrypt(database.getJdbcPassPlain(), new OiyoInfo().getPassphrase()));
            }
            // JSON書き込み直前に、JDBCの平文パスワードを除去
            database.setJdbcPassPlain(null);
        }

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

            // [IYI5102] Check the `oiyokan-demo.zip`.
            initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI5102);
            log.info(OiyokanInitializrMessages.IYI5102 + ": oiyokan-demo.zip");
        } catch (IOException ex) {
            // [IYI5201] ERROR: Fail to generate zip file.
            log.error(OiyokanInitializrMessages.IYI5201, ex);
            throw ex;
        }

        // [IYI1009] Download ZIP successfully
        log.info(OiyokanInitializrMessages.IYI1009);
        return null;
    }
}
