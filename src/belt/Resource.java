package belt;

import marblegame.Util;

import java.util.HashMap;
import java.util.Map;

import static belt.Resource.Material.*;
import static belt.Resource.State.*;

/**
 * Type of resource
 */
public class Resource {
    public final static Resource GOLD_RAW = new Resource(GOLD, RAW, 80);
    public final static Resource GOLD_LIQUID = new Resource(GOLD, LIQUID, 100);
    public final static Resource GOLD_WIRE = new Resource(GOLD, WIRE, 100);
    public final static Resource GOLD_GEAR = new Resource(GOLD, GEAR, 100);

    public final static Resource ALUMINIUM_RAW = new Resource(ALUMINIUM, RAW, 80);
    public final static Resource ALUMINIUM_LIQUID = new Resource(ALUMINIUM, LIQUID, 100);
    public final static Resource ALUMINIUM_WIRE = new Resource(ALUMINIUM, WIRE, 100);
    public final static Resource ALUMINIUM_GEAR = new Resource(ALUMINIUM, GEAR, 100);

    public final static Resource IRON_RAW = new Resource(IRON, RAW, 80);
    public final static Resource IRON_LIQUID = new Resource(IRON, LIQUID, 100);
    public final static Resource IRON_WIRE = new Resource(IRON, WIRE, 100);
    public final static Resource IRON_GEAR = new Resource(IRON, GEAR, 100);

    public final static Resource COPPER_RAW = new Resource(COPPER, RAW, 80);
    public final static Resource COPPER_LIQUID = new Resource(COPPER, LIQUID, 100);
    public final static Resource COPPER_WIRE = new Resource(COPPER, WIRE, 100);
    public final static Resource COPPER_GEAR = new Resource(COPPER, GEAR, 100);

    public final static Resource DIAMOND_RAW = new Resource(DIAMOND, RAW, 80);
    public final static Resource DIAMOND_LIQUID = new Resource(DIAMOND, LIQUID, 100);
    public final static Resource DIAMOND_WIRE = new Resource(DIAMOND, WIRE, 100);
    public final static Resource DIAMOND_GEAR = new Resource(DIAMOND, GEAR, 100);

    public final static Resource CIRCUIT = new Resource("Circuit", 300,
        COPPER_WIRE, COPPER_WIRE, GOLD_GEAR);

    public final static Resource ENGINE = new Resource("Engine", 360,
        IRON_GEAR, IRON_GEAR, GOLD_GEAR);
    public final static Resource BATTERY = new Resource("Battery", 1050,
        CIRCUIT, ALUMINIUM_RAW, ALUMINIUM_LIQUID);
    public final static Resource POWERSUPPLY = new Resource("Power supply", 1920,
        new int[]{1, 3, 3}, CIRCUIT, COPPER_WIRE, IRON_WIRE);
    public final static Resource PROCESSOR = new Resource("Processor", 1320,
        CIRCUIT, CIRCUIT, ALUMINIUM_RAW, ALUMINIUM_RAW);
    public final static Resource COMPUTER = new Resource("Computer", 11000,
        new int[]{1, 6, 1}, CIRCUIT, ALUMINIUM_RAW, POWERSUPPLY);
    public final static Resource HEATING_PLATE = new Resource("HeatingPlate", 360,
        DIAMOND_RAW, COPPER_RAW, COPPER_WIRE);
    private final String name;
    private final Material material;
    private final State state;
    private final Resource[] recipe;
    private final Map<Resource, Integer> recipeMap;
    private final Resource[] recipeTypes;
    private final int[] recipeCounts;
    private final int price;

    private Resource(Material material, State state, int price) {
        this.name = createName(material, state);
        this.material = material;
        this.state = state;
        this.price = price;
        recipe = null;
        recipeMap = null;
        recipeTypes = null;
        recipeCounts = null;
    }

    private Resource(String name, int price, Resource... recipe) {
        this.name = name;
        this.material = Material.COMPOSITE;
        this.state = State.COMPOSITE;
        this.price = price;
        this.recipe = recipe;
        recipeMap = new HashMap<>();
        for (Resource resource : recipe) {
            recipeMap.put(resource, recipeMap.getOrDefault(resource, 0) + 1);
        }
        recipeTypes = new Resource[recipeMap.size()];
        recipeCounts = new int[recipeTypes.length];
        int i = 0;
        for (Map.Entry<Resource, Integer> re : recipeMap.entrySet()) {
            recipeTypes[i] = re.getKey();
            recipeCounts[i++] = re.getValue();
        }
    }

    private Resource(String name, int price, int[] amounts, Resource... recipe) {
        this(name, price, Util.multipleRefElements(recipe, amounts));
    }

    private static String createName(Material material, State state) {
        return material.toString() + " (" + state.toString() + ")";
    }

    public static void main(String[] args) {
        Field field = Field.defaultField();
    }

    public State getState() {
        return state;
    }

    public Material getMaterial() {
        return material;
    }

    public Resource[] getRecipe() {
        return recipe;
    }

    public Map<Resource, Integer> getRecipeMap() {
        return recipeMap;
    }

    public Resource[] getRecipeTypes() {
        return recipeTypes;
    }

    public int[] getRecipeCounts() {
        return recipeCounts;
    }

    @Override
    public String toString() {
        return name;
    }

    public Piece.PieceType getPieceType() {
        throw new UnsupportedOperationException();
    }

    public enum Material {
        GOLD, ALUMINIUM, IRON, COPPER, DIAMOND, COMPOSITE
    }

    public enum State {
        RAW, LIQUID, WIRE, GEAR, PLATE, COMPOSITE
    }

}
