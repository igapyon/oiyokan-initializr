package jp.oiyokan.initializr.ctrl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import jp.oiyokan.initializr.OiyokanInitializrConstants;

/**
 * Thymeleaf画面とやりとりに利用するBean.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION)
public class ThInitializrBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String msgSuccess = null;
    private String msgError = null;

    private static final boolean isSaveOiyokanSettingsEnabled = OiyokanInitializrConstants.SAVE_OIYOKAN_SETTINGS_ENABLED;

    ///////////////////////////////////////////////
    // 画面項目

    boolean processView = false;
    boolean isReadWriteAccess = true;
    boolean convertCamel = false; // EntitySetなどの名称を Camel case にするかどうか。通常は false で良い
    boolean isFilterTreatNullAsBlank = false; // Support Salesforce or not.

    private List<String> checkboxes = new ArrayList<>();
    private List<TableInfo> tableInfos = new ArrayList<>();

    ///////////////////////////////////////////////
    // Method

    public boolean isSaveOiyokanSettingsEnabled() {
        return isSaveOiyokanSettingsEnabled;
    }

    public String getMsgSuccess() {
        return msgSuccess;
    }

    public void setMsgSuccess(String msgSuccess) {
        this.msgSuccess = msgSuccess;
    }

    public String getMsgError() {
        return msgError;
    }

    public void setMsgError(String msgError) {
        this.msgError = msgError;
    }

    public boolean isConvertCamel() {
        return convertCamel;
    }

    public void setConvertCamel(boolean convertCamel) {
        this.convertCamel = convertCamel;
    }

    public boolean isFilterTreatNullAsBlank() {
        return isFilterTreatNullAsBlank;
    }

    public void setFilterTreatNullAsBlank(boolean isFilterTreatNullAsBlank) {
        this.isFilterTreatNullAsBlank = isFilterTreatNullAsBlank;
    }

    public boolean isProcessView() {
        return processView;
    }

    public void setProcessView(boolean processView) {
        this.processView = processView;
    }

    public boolean isReadWriteAccess() {
        return isReadWriteAccess;
    }

    public void setReadWriteAccess(boolean isReadWriteAccess) {
        this.isReadWriteAccess = isReadWriteAccess;
    }

    public List<String> getCheckboxes() {
        return checkboxes;
    }

    public void setCheckboxes(List<String> checkboxes) {
        this.checkboxes = checkboxes;
    }

    public List<TableInfo> getTableInfos() {
        return tableInfos;
    }

    ///////////////////////////////////////////////
    // 画面のチェックボッスで利用する EntitySet

    public static class TableInfo {
        private String name = null;
        private boolean selected = false;
        private boolean isView = false;

        public TableInfo(String name, boolean selected, boolean isView) {
            this.name = name;
            this.selected = selected;
            this.isView = isView;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isView() {
            return isView;
        }

        public void setView(boolean isView) {
            this.isView = isView;
        }
    }
}
