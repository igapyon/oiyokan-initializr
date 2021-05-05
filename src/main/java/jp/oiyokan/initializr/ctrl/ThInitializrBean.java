package jp.oiyokan.initializr.ctrl;

import java.util.ArrayList;
import java.util.List;

public class ThInitializrBean {
    private String msgSuccess = null;
    private String msgError = null;

    boolean processView = false;
    boolean convertCamel = false; // EntitySetなどの名称を Camel case にするかどうか。通常は false で良い
    boolean isFilterTreatNullAsBlank = false; // Support Salesforce or not.

    private List<Table> tables = new ArrayList<>();

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

    public static class Table {
        private boolean features = false;
        private String tableName = null;

        public Table(String tableName) {
            this.tableName = tableName;
        }

        public boolean isFeatures() {
            return features;
        }

        public void setFeatures(boolean features) {
            this.features = features;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
    }

    public List<Table> getTables() {
        return tables;
    }
}
