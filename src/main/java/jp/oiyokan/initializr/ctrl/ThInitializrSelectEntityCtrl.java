package jp.oiyokan.initializr.ctrl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.initializr.OiyokanInitializrConstants;
import jp.oiyokan.initializr.OiyokanInitializrMessages;
import jp.oiyokan.initializr.OiyokanInitializrUtil;
import jp.oiyokan.initializr.ctrl.ThInitializrBean.TableInfo;
import jp.oiyokan.oiyogen.OiyokanSettingsGenUtil;

@Controller
@SessionAttributes("scopedTarget.settingsBean")
public class ThInitializrSelectEntityCtrl {
    private static final Log log = LogFactory.getLog(ThInitializrSelectEntityCtrl.class);

    @Autowired
    private OiyokanSettingsWrapper settingsBean;

    /////////////////////////
    // Select table

    @RequestMapping(value = { "/initializrSelectEntity" }, params = { "new" }, method = { RequestMethod.POST })
    public String selectEntity(Model model, ThInitializrBean initializrBean, @RequestParam("new") String dbName,
            BindingResult result) throws IOException {
        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        settingsBean.setCurrentDbSettingName(dbName);
        log.info("dbName:" + dbName);
        log.info("processView:" + initializrBean.isProcessView());

        try {
            OiyoSettingsDatabase database = null;
            for (OiyoSettingsDatabase lookup : settingsBean.getSettings().getDatabase()) {
                if (lookup.getName().equals(dbName)) {
                    database = lookup;
                }
            }
            if (database == null) {
                initializrBean.setMsgError("データベース発見できず。");
                return "oiyokan/initializrTop";
            }

            selectEntityInternal(initializrBean, database);

            Collections.sort(initializrBean.getTableInfos(), new Comparator<ThInitializrBean.TableInfo>() {
                @Override
                public int compare(TableInfo o1, TableInfo o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });

            // TODO Entityの設定状況により分岐
            return "oiyokan/initializrSelectEntity";
        } catch (ODataApplicationException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "oiyokan/initializrTop";
        }
    }

    @RequestMapping(value = { "/initializrSelectEntity" }, params = { "applyEntitySelection" }, method = {
            RequestMethod.POST })
    public String applyEntitySelection(Model model, ThInitializrBean initializrBean, BindingResult result)
            throws IOException {
        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        log.info("選択したEntitySetをマーク");
        Map<String, String> mapNameFilter = new HashMap<>();
        for (String opts : initializrBean.getCheckboxes()) {
            mapNameFilter.put(opts, opts);
        }

        final OiyoSettings oiyoSettings = settingsBean.getSettings();

        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");

        //////////////////////////////////////////////////////////
        // Setup basic settings info
        OiyoInfo oiyoInfo = new OiyoInfo();

        try {
            // 常にVIEWつきでトラバース。ただし先にて名前フィルタリングあるため大丈夫。
            OiyokanInitializrUtil.traverseTable(oiyoInfo, oiyoSettings, settingsBean.getCurrentDbSettingName(), true,
                    initializrBean.isReadWriteAccess(), mapNameFilter);

            OiyokanInitializrUtil.tuneSettings(oiyoInfo, oiyoSettings, initializrBean.isConvertCamel(),
                    initializrBean.isFilterTreatNullAsBlank);
            initializrBean.setMsgSuccess("Entity selection applied");
            return "oiyokan/initializrTop";
        } catch (ODataApplicationException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "oiyokan/initializrSelectEntity";
        }

    }

    public static void selectEntityInternal(ThInitializrBean initializrBean, OiyoSettingsDatabase database)
            throws IOException, ODataApplicationException {

        ThInitializrSetupDatabaseCtrl.connTestInternal(initializrBean, database, null);

        initializrBean.getTableInfos().clear();

        // [IYI2111] Connect to database.
        log.info(OiyokanInitializrMessages.IYI2111 + ": " + database.getName());
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            // [IYI2102] DEBUG: Traverse TABLE.
            log.debug(OiyokanInitializrMessages.IYI2102);
            ResultSet rsTables = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "TABLE" });
            for (; rsTables.next();) {
                final String tableName = rsTables.getString("TABLE_NAME");

                try {
                    // [IYI2112] DEBUG: Read table.
                    log.info(OiyokanInitializrMessages.IYI2112 + ": " + tableName);
                    final OiyoSettingsEntitySet entitySet = OiyokanSettingsGenUtil.generateSettingsEntitySet(
                            connTargetDb, tableName, OiyokanConstants.DatabaseType.valueOf(database.getType()));

                    initializrBean.getTableInfos()
                            .add(new ThInitializrBean.TableInfo(entitySet.getEntityType().getDbName(), true, false));
                } catch (Exception ex) {
                    // [IYI2113] WARN: Fail to read table.
                    log.warn(OiyokanInitializrMessages.IYI2113 + ": " + tableName);
                }
            }

            if (initializrBean.isProcessView()) {
                // [IYI2103] DEBUG: Traverse VIEW.
                log.debug(OiyokanInitializrMessages.IYI2103);
                ResultSet rsViews = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "VIEW" });
                for (; rsViews.next();) {
                    final String viewName = rsViews.getString("TABLE_NAME");

                    try {
                        // [IYI2114] DEBUG: Read view.
                        log.info(OiyokanInitializrMessages.IYI2114 + ": " + viewName);
                        final OiyoSettingsEntitySet entitySet = OiyokanSettingsGenUtil.generateSettingsEntitySet(
                                connTargetDb, viewName, OiyokanConstants.DatabaseType.valueOf(database.getType()));

                        initializrBean.getTableInfos()
                                .add(new ThInitializrBean.TableInfo(entitySet.getEntityType().getDbName(), true, true));
                    } catch (Exception ex) {
                        // [IYI2115] WARN: Fail to read view.
                        log.warn(OiyokanInitializrMessages.IYI2115 + ": " + viewName);
                    }
                }
            }
        } catch (Exception ex) {
            // TODO message
            log.warn(OiyokanMessages.IY9999 + ": ");
        }
    }
}
