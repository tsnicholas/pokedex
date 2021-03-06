package edu.bsu.cs222.view;

import edu.bsu.cs222.model.Pokemon;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class DamageRelationsDisplay extends DisplayCreator implements MenuDisplay {
    private final static String IMMUNITIES = "Immunities: ";
    private final static String RESISTANCES = "Resistances: ";
    private final static String WEAKNESSES = "Weaknesses: ";

    private final VBox damageRelationRows = new VBox(SMALL_SPACING);

    public DamageRelationsDisplay() {
        damageRelationRows.setPadding(DEFAULT_INSETS);
    }

    public Parent getInitialDisplay() {
        damageRelationRows.getChildren().addAll(
                createText(IMMUNITIES, HEADER_FONT),
                createText(RESISTANCES, HEADER_FONT),
                createText(WEAKNESSES, HEADER_FONT)
        );
        return damageRelationRows;
    }

    public Parent display(Pokemon pokemon) {
        damageRelationRows.getChildren().remove(0, damageRelationRows.getChildren().size());
        damageRelationRows.getChildren().addAll(
                createDamageRelationRow(IMMUNITIES, pokemon.getImmunities()),
                createDamageRelationRow(RESISTANCES, pokemon.getResistances()),
                createDamageRelationRow(WEAKNESSES, pokemon.getWeaknesses())
        );
        return damageRelationRows;
    }

    private Parent createDamageRelationRow(String damageRelation, List<String> damageRelationList) {
        HBox damageRelationRow = new HBox(SMALL_SPACING);
        damageRelationRow.getChildren().addAll(
                createText(damageRelation, HEADER_FONT),
                createDamageRelationImages(damageRelationList)
        );
        return damageRelationRow;
    }

    private Parent createDamageRelationImages(List<String> damageRelationList) {
        HBox typeImages = new HBox(SMALL_SPACING);
        for(String type: damageRelationList) {
            typeImages.getChildren().add(retrieveTypeImage(type + ".png"));
        }
        return typeImages;
    }

    public String getDisplayName() {
        return "Damage Relations";
    }
}
