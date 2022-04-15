package edu.bsu.cs222.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class PokedexProcessorTest extends TestResourceConverter {
    private final TestURLProcessor testURLProcessor = new TestURLProcessor();
    private final PokedexProcessor pokedexProcessor = new PokedexProcessor(testURLProcessor);

    @Test
    public void testPokemonExistsInNationalPokedex_TrueCase() {
        Assertions.assertTrue(pokedexProcessor.pokemonExistsInNationalPokedex("mr. mime"));
    }

    @Test
    public void testPokemonExistsInNationalPokedex_FalseCase() {
        Assertions.assertFalse(pokedexProcessor.pokemonExistsInNationalPokedex("dark magician"));
    }

    @Test
    public void testConvertTypesToString() {
        Type flying = Type.withName("flying").andDamageRelations(null);
        Type ghost = Type.withName("ghost").andDamageRelations(null);
        List<Type> testTypes = new ArrayList<>();
        testTypes.add(flying);
        testTypes.add(ghost);
        String expected = "flying ghost ";
        Assertions.assertEquals(expected, pokedexProcessor.convertTypesToString(testTypes));
    }
}