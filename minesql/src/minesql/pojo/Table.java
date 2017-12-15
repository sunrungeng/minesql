package minesql.pojo;

import java.util.List;

/**
 * Created by srg
 *
 * @date 2017/11/20
 */
public class Table {
    private List<Integer> no;
    private List<String> field;
    private List<String> type;
    private List<String> isNull;
    private List<String> key;
    private List<String> defaultValue;
    private List<String> check;

    public List<Integer> getNo() {
        return no;
    }

    public void setNo(List<Integer> no) {
        this.no = no;
    }

    public List<String> getField() {
        return field;
    }

    public void setField(List<String> field) {
        this.field = field;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public List<String> getIsNull() {
        return isNull;
    }

    public void setIsNull(List<String> isNull) {
        this.isNull = isNull;
    }

    public List<String> getKey() {
        return key;
    }

    public void setKey(List<String> key) {
        this.key = key;
    }

    public List<String> getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(List<String> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<String> getCheck() {
        return check;
    }

    public void setCheck(List<String> check) {
        this.check = check;
    }

    public Table() {
    }
}
