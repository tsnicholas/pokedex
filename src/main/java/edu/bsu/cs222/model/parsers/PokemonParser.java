package edu.bsu.cs222.model.parsers;

import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import edu.bsu.cs222.model.*;
import net.minidev.json.JSONArray;

import java.util.*;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;
import static com.jayway.jsonpath.JsonPath.parse;

public class PokemonParser {
    private final URLProcessor urlProcessor;
    private final AbilityParser abilityParser = new AbilityParser();

    public PokemonParser() {
        this.urlProcessor = new ProductionURLProcessor();
    }

    public PokemonParser(URLProcessor urlProcessor) {
        this.urlProcessor = urlProcessor;
    }

    public boolean assertPokemonExistsInGame(Object pokemonJsonDocument, Version version) {
        JSONArray gameIndices = JsonPath.read(pokemonJsonDocument,
                "$.moves[?(@.version_group_details..version_group.name contains \"" +
                        version.getVersionGroup().getVersionGroupName() +
                        "\")]");
        return 0 != gameIndices.size();
    }

    public String parseName(Object pokemonJsonDocument) {
        return JsonPath.read(pokemonJsonDocument, "$.name");
    }

    public List<Type> parseForTypes(Object pokemonJsonDocument, Version version) {
        pokemonJsonDocument = makeTypeJsonPath(pokemonJsonDocument, version);
        List<String> typeNames = JsonPath.read(pokemonJsonDocument, "$.types..name");
        List<String> typeURLs = JsonPath.read(pokemonJsonDocument, "$.types..url");
        return createTypeList(typeNames, typeURLs, version);
    }

    private Object makeTypeJsonPath(Object PokemonJsonDocument, Version version) {
        JSONArray pastTypesDetailsArray = JsonPath.read(PokemonJsonDocument, "$.past_types");
        for (Object pastTypeDetails : pastTypesDetailsArray) {
            String generationName = JsonPath.read(pastTypeDetails, "$.generation.name");
            int generationID = version.getGenerationMap().get(generationName);
            if (version.getGeneration().getGenerationID() <= generationID) {
                return pastTypeDetails;
            }
        }
        return PokemonJsonDocument;
    }

    private List<Type> createTypeList(List<String> typeNames, List<String> typeURLs, Version version) {
        List<Type> typeList = new LinkedList<>();
        DamageRelationsParser damageRelationsParser = new DamageRelationsParser();
        for (int i = 0; i < typeNames.size(); i++) {
            Object typeJsonObject = urlProcessor.convertStringToObject(typeURLs.get(i));
            HashMap<String, List<String>> damageRelations = damageRelationsParser
                    .parseForDamageRelations(typeJsonObject, version);
            typeList.add(Type.withName(typeNames.get(i)).andDamageRelations(damageRelations));
        }
        return typeList;
    }

    public Map<String, Integer> parseForStats(Object pokemonJsonDocument) {
        JSONArray stats = JsonPath.read(pokemonJsonDocument, "$.stats");
        List<String> statNames = JsonPath.read(stats, "$..stat.name");
        List<Integer> baseStats = JsonPath.read(stats, "$..base_stat");
        return createStatMap(statNames, baseStats);
    }

    private Map<String, Integer> createStatMap(List<String> statNames, List<Integer> baseStats) {
        Map<String, Integer> statMap = new LinkedHashMap<>();
        for (int i = 0; i < statNames.size(); i++) {
            statMap.put(statNames.get(i), baseStats.get(i));
        }
        return statMap;
    }

    public List<Move> parseForMoves(Object pokemonJsonDocument, Version version, HashMap<String, Move> moveCache) {
        List<Move> moveList = new LinkedList<>();
        Filter learnMethodFilter = filter(where("version_group.name").is(version.getVersionGroup().getVersionGroupName()));
        JSONArray moveArray = JsonPath.read(pokemonJsonDocument, "$.moves[?(@.version_group_details..version_group.name " +
                "contains \"" + version.getVersionGroup().getVersionGroupName() + "\")]");
        int startIndex = "https://pokeapi.co/api/v2/move/".length();
        for (Object moveObject : moveArray) {
            JSONArray moveVersionDetailsArray = parse(moveObject).read("$.version_group_details[?]", learnMethodFilter);
            List<String> learnMethods = parseLearnMethods(moveVersionDetailsArray);
            String moveUrl = JsonPath.read(moveObject, "$.move.url");
            if (moveCache.containsKey(moveUrl.substring(startIndex))) {
                moveList.add(moveCache.get(moveUrl.substring(startIndex)));
            } else {
                Object moveJsonDocument = urlProcessor.convertStringToObject(moveUrl);
                Move move = createMove(moveJsonDocument, learnMethods, version);
                moveCache.put(moveUrl.substring(startIndex), move);
                moveList.add(move);
            }
        }
        return moveList;
    }

    private List<String> parseLearnMethods(JSONArray moveVersionDetailsArray) {
        List<String> learnMethods = new ArrayList<>();
        for (Object occurrence : moveVersionDetailsArray) {
            learnMethods.add(processLearnMethod(occurrence));
        }
        return learnMethods;
    }

    private String processLearnMethod(Object occurrence) {
        String method = JsonPath.read(occurrence, "$.move_learn_method.name");
        if (method.equals("level-up")) {
            Integer levelLearnedAt = JsonPath.read(occurrence, "$.level_learned_at");
            return "LV " + levelLearnedAt.toString();
        }
        if (method.equals("machine")) {
            return "TM";
        }
        return method.toUpperCase();
    }

    private Move createMove(Object moveJsonDocument, List<String> learnMethods, Version version) {
        MoveParser moveParser = new MoveParser();
        return moveParser.parseForMove(moveJsonDocument, learnMethods, version);
    }

    public String parseForImage(Object pokemonJsonDocument, Version version) {
        String spriteURL;
        if (versionGroupContainsSprite(pokemonJsonDocument, version))
            spriteURL = JsonPath.read(pokemonJsonDocument, "$.sprites.versions." +
                    version.getGeneration().getGenerationName() +
                    "." + version.getVersionGroup().getVersionGroupName() +
                    ".front_default");
        else if (versionContainsSprite(pokemonJsonDocument, version)) {
            spriteURL = JsonPath.read(pokemonJsonDocument, "$.sprites.versions." +
                    version.getGeneration().getGenerationName() +
                    "." + version.getVersionName() + ".front_default");
        } else {
            spriteURL = JsonPath.read(pokemonJsonDocument, "$.sprites.front_default");
        }
        if (spriteURL == null) {
            spriteURL = JsonPath.read(pokemonJsonDocument, "$.sprites.front_default");
        }
        return spriteURL;
    }

    private boolean versionGroupContainsSprite(Object pokemonJsonDocument, Version version) {
        JSONArray spriteArray = JsonPath.read(pokemonJsonDocument, "$.sprites.versions." +
                version.getGeneration().getGenerationName() +
                "[?(@." + version.getVersionGroup().getVersionGroupName() + ")]");
        return spriteArray.size() != 0;
    }

    private boolean versionContainsSprite(Object pokemonJsonDocument, Version version) {
        JSONArray spriteArray = JsonPath.read(pokemonJsonDocument, "$.sprites.versions." +
                version.getGeneration().getGenerationName() +
                "[?(@." + version.getVersionName() + ")]");
        return spriteArray.size() != 0;
    }

    public List<Ability> parseForAbilities(Object pokeJsonDocument, Version version) {
        JSONArray abilitiesArray = JsonPath.read(pokeJsonDocument, "$.abilities");
        List<Ability> abilities = new ArrayList<>();
        for (Object ability : abilitiesArray) {
            Object abilityJsonDocument = urlProcessor.convertStringToObject(JsonPath.read(ability, "$.ability.url"));
            if (abilityParser.assertExistsInVersion(abilityJsonDocument, version)) {
                abilities.add(createAbility(ability, abilityJsonDocument));
            }
        }
        return abilities;
    }

    private Ability createAbility(Object ability, Object abilityJsonDocument) {
        String abilityName = JsonPath.read(ability, "$.ability.name");
        boolean isHidden = JsonPath.read(ability, "$.is_hidden");
        return Ability.withName(abilityName)
                .andEffect(abilityParser.parseEffect(abilityJsonDocument))
                .andIsHidden(isHidden);
    }

    public List<Evolution> parseForEvolutionChain(Object pokemonJsonDocument, Version version) {
        PokemonSpeciesParser pokemonSpeciesParser = new PokemonSpeciesParser();
        EvolutionChainParser evolutionChainParser = new EvolutionChainParser(urlProcessor);
        Object evolutionChainJsonDocument = urlProcessor.convertStringToObject(
                pokemonSpeciesParser.parseForEvolutionChain(pokemonJsonDocument));
        return evolutionChainParser.parseForEvolutions(evolutionChainJsonDocument, version);
    }
}
