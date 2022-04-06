package edu.bsu.cs222.model.parsers;

import edu.bsu.cs222.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

class PokemonParserTest extends TestResourceConverter {
    private final TestURLProcessor testURLProcessor = new TestURLProcessor();
    private final PokemonParser pokemonParser = new PokemonParser(testURLProcessor);
    private final Object charizardDocument = convertFileNameToObject("charizard.json");
    private final Object dittoDocument = convertFileNameToObject("ditto.json");

    @Test
    public void testParseForTypes_pastType_genOneIsNormal() {
        Object clefableDocument = convertFileNameToObject("clefable.json");

        Generation genOne = Generation.withName("generation-i").andID(1).andVersionGroups(null);
        Generation genFive = Generation.withName("generation-v").andID(5).andVersionGroups(null);
        GenerationMap generationMap = GenerationMap.withGenerationList(List.of(genOne, genFive)).createGenerationMap();

        List<Type> actual = pokemonParser.parseForTypes(clefableDocument, Version.withName(null).
                andGeneration(genOne).andGenerationMap(generationMap));

        Assertions.assertEquals("normal", actual.get(0).getName());
    }

    @ParameterizedTest
    @CsvSource({"fire, 0", "flying, 1"})
    public void testParseForTypes_multipleTypes_fireAndFlying(String typeName, int typeIndex) {
        List<Type> actual = pokemonParser.parseForTypes(charizardDocument, Version.withName(null).andGenerationMap(null));
        Assertions.assertEquals(typeName, actual.get(typeIndex).getName());
    }

    @Test
    public void testParseForStats_charizardStats() {
        Map<String, Integer> expected = new HashMap<>();
        expected.put("hp", 78);
        expected.put("attack", 84);
        expected.put("defense", 78);
        expected.put("special-attack", 109);
        expected.put("special-defense", 85);
        expected.put("speed", 100);
        Map<String, Integer> actual = pokemonParser.parseForStats(charizardDocument);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testParseForMoves_transform() {
        List<Move> expected = new ArrayList<>();
        List<String> expectedLearnMethods = new ArrayList<>();
        expectedLearnMethods.add("LV 1");
        LinkedHashMap<String, String> expectedMoveData = new LinkedHashMap<>();
        expectedMoveData.put("Name", "transform");
        expectedMoveData.put("Type", "normal");
        expectedMoveData.put("PP", "10");
        expectedMoveData.put("Power", "--");
        expectedMoveData.put("Accuracy", "--");
        expected.add(new Move(expectedMoveData, expectedLearnMethods));

        List<Move> actual = pokemonParser.parseForMoves(dittoDocument, "yellow");
        Assertions.assertEquals(expected.get(0).getMoveData(), actual.get(0).getMoveData());
    }

    @Test
    public void testAssertPokemonExistsInGame_Yellow_dittoExists() {
        Assertions.assertTrue(pokemonParser.assertPokemonExistsInGame(dittoDocument, "yellow"));
    }
}