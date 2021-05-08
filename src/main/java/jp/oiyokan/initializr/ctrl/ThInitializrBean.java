package jp.oiyokan.initializr.ctrl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;

@Component
// @Scope(value = WebApplicationContext.SCOPE_SESSION)
public class ThInitializrBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String msgSuccess = null;
    private String msgError = null;

    private final OiyoSettings settings = new OiyoSettings();

    ///////////////////////////////////////////////
    // 画面項目

    boolean processView = false;
    boolean isReadWriteAccess = true;
    boolean convertCamel = false; // EntitySetなどの名称を Camel case にするかどうか。通常は false で良い
    boolean isFilterTreatNullAsBlank = false; // Support Salesforce or not.

    private List<String> checkboxes = new ArrayList<>();
    private List<EntitySet> entitySets = new ArrayList<>();

    public ThInitializrBean() {

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

        final OiyoSettingsDatabase database = getFirstDatabase();
        if (database.getName() == null) {
            database.setName("mydbsetting1");
        }
        if (database.getType() == null) {
            database.setType("PostgreSQL"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        }
        if (database.getDescription() == null) {
            database.setDescription("Tutorial db sample.");
        }
        if (database.getJdbcDriver() == null) {
            database.setJdbcDriver("org.postgresql.Driver"); // JDBC Driver class name.
        }
        if (database.getJdbcUrl() == null) {
            database.setJdbcUrl("jdbc:postgresql://localhost:5432/dvdrental"); // JDBC URL.
        }
        database.setJdbcUser(""); // JDBC User.
        database.setJdbcPassPlain(""); // JDBC Password.
    }

    ///////////////////////////////////////////////
    // Method

    public OiyoSettings getSettings() {
        return settings;
    }

    public OiyoSettingsDatabase getFirstDatabase() {
        if (settings.getDatabase() == null) {
            settings.setDatabase(new ArrayList<>());
        }
        if (settings.getDatabase().size() == 0) {
            settings.getDatabase().add(new OiyoSettingsDatabase());
        }

        return settings.getDatabase().get(0);
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

    public List<EntitySet> getEntitySets() {
        return entitySets;
    }

    ///////////////////////////////////////////////
    // 画面のチェックボッスで利用する EntitySet

    public static class EntitySet {
        private boolean selected = false;
        private String name = null;

        public EntitySet(String name, boolean selected) {
            this.name = name;
            this.selected = selected;
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
    }
}
