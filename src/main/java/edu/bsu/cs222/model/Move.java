package edu.bsu.cs222.model;

import java.util.List;

public class Move {
    private final String name;
    private final String type;
    private final int PP;
    private final int power;
    private final int accuracy;
    private final List<String> learnMethods;

    public Move(String name, String type, int PP, int power, int accuracy, List<String> learnMethods) {
        this.name = name;
        this.type = type;
        this.PP = PP;
        this.power = power;
        this.accuracy = accuracy;
        this.learnMethods = learnMethods;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        for(String learnMethod: learnMethods) {
            output.append(name);
            output.append(" ");
            output.append(type);
            output.append(" ");
            output.append(PP);
            output.append(" ");
            output.append(power);
            output.append(" ");
            output.append(accuracy);
            output.append(" ");
            output.append(learnMethod);
            output.append("\n");
        }

        return output.toString();
    }
}
