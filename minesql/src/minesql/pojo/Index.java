package minesql.pojo;

import java.util.List;

/**
 * Created by srg
 *
 * @date 2017/11/20
 */
public class Index {

    private List<Integer> no;
    private List<String> value;

    public List<Integer> getNo() {
        return no;
    }

    public void setNo(List<Integer> no) {
        this.no = no;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public Index() {
    }

    public Index(List<Integer> no, List<String> value) {
        this.no = no;
        this.value = value;
    }
}
