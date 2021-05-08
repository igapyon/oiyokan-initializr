package jp.oiyokan.initializr.ctrl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
// @Scope(value = WebApplicationContext.SCOPE_SESSION)
public class ThInitializrBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String msgSuccess = null;
    private String msgError = null;

    boolean processView = false;
    boolean isReadWriteAccess = true;
    boolean convertCamel = false; // EntitySetなどの名称を Camel case にするかどうか。通常は false で良い
    boolean isFilterTreatNullAsBlank = false; // Support Salesforce or not.

    private List<String> checkboxes = new ArrayList<>();
    private List<EntitySet> entitySets = new ArrayList<>();

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
