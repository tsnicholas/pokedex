package edu.bsu.cs222.model;

import java.util.*;

public class Pokemon {
    public static Builder withTypeList(List<Type> typeList) {
        return new Builder(typeList);
    }

    public static final class Builder {
        private final List<Type> typeList;
        private List<Move> moveList;
        private Map<String, Integer> statsMap;
        private List<Ability> abilities;
        private String imageURL;

        public Builder (List<Type> typeList) {
            this.typeList = typeList;
        }

        public Builder andStatsMap(Map<String, Integer> statsMap) {
            this.statsMap = statsMap;
            return this;
        }

        public Builder andMoveList(List<Move> moveList) {
            this.moveList = moveList;
            return this;
        }

        public Builder andAbilities(List<Ability> abilities) {
            this.abilities = abilities;
            return this;
        }

        public Pokemon andImageURL(String imageURL) {
            this.imageURL = imageURL;
            return new Pokemon(this);
        }
    }

    private final List<Type> typeList;
    private final List<Move> moveList;
    private final Map<String, Integer> statsMap;
    private final List<Ability> abilities;
    private final String imageURL;
    private List<String> weaknesses = new ArrayList<>();
    private List<String> resistances = new ArrayList<>();
    private List<String> immunities = new ArrayList<>();

    public Pokemon(Builder builder) {
        typeList = builder.typeList;
        moveList = builder.moveList;
        abilities = builder.abilities;
        statsMap = builder.statsMap;
        imageURL = builder.imageURL;
        setDamageRelations();
    }

    private void setDamageRelations() {
        if (typeList.size() == 1) {
            immunities = typeList.get(0).getImmuneTo();
            weaknesses = typeList.get(0).getWeakTo();
            resistances = typeList.get(0).getResistantTo();
            return;
        }
        obtainBaseDamageRelations();
        weaknesses.removeIf(resistances::remove);
        getRidOfDuplicates();
        removeSharedTypesWithImmunities();
    }

    private void obtainBaseDamageRelations() {
        for (Type type : typeList) {
            immunities.addAll(type.getImmuneTo());
            weaknesses.addAll(type.getWeakTo());
            resistances.addAll(type.getResistantTo());
        }
    }

    private void getRidOfDuplicates() {
        immunities = eliminateDuplicates(immunities);
        weaknesses = eliminateDuplicates(weaknesses);
        resistances = eliminateDuplicates(resistances);
    }

    private List<String> eliminateDuplicates(List<String> stringList) {
        Set<String> stringSet = new HashSet<>(stringList);
        return new ArrayList<>(stringSet);
    }

    private void removeSharedTypesWithImmunities() {
        for (String immunity : immunities) {
            weaknesses.remove(immunity);
            resistances.remove(immunity);
        }
    }

    public List<Type> getTypes() {
        return typeList;
    }

    public Map<String, Integer> getStats() {
        return statsMap;
    }

    public List<Move> getMoves() {
        return moveList;
    }

    public List<Ability> getAbilities() {
        return abilities;
    }

    public String getImageURL() {
        return imageURL;
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public List<String> getResistances() {
        return resistances;
    }

    public List<String> getImmunities() {
        return immunities;
    }
}
