import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import java.lang.Object;
import java.lang.String;
import java.util.List;

public class StepImpl {
    @Step("Say <hello> to <world>")
    public String helloWorld(String greeting, int name) {
    }

    @Step("step <a> and a table <table>")
    public void stepWithTable(float a, Table table) {
    }

    @Step("A step with no params")
    public void someStepStep() {
    }

    @Step(" Tell <greeting> to <name>")
    public void helloWorld(String greeting, String name) {
        System.out.println(greeting + ", " + name);
    }

    @Step("† ‡ µ ¢ step with <Û> and <į>")
    public void stepWith(String a, String b) {
    }
}