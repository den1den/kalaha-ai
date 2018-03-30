package belt;

import marblegame.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static belt.Piece.PieceType.SELLER;

public class Search {
    Field field;
    Resource targetResource;

    public Search(Field field, Resource targetResource) {
        this.field = field;
        this.targetResource = targetResource;
    }

    public static void main(String[] args) {
        Field field = Field.emptyField(10, 20);
    }

    public void search() {
        int x = 0, y = 0;
        field.place(x, y, new Piece(SELLER));
        search(new Field(field), x, y, targetResource);
    }

    public void search(Field field, int outputX, int outputY, Resource outputResource) {
        if (outputResource.getRecipe().length > 4) {
            throw new UnsupportedOperationException();
        }
        List<int[]> locations = getLocations(outputX, outputY);
        Resource[] recipeTypes = outputResource.getRecipeTypes();
        int[] recipeCounts = outputResource.getRecipeCounts();
        getAllPlacements(field, locations, recipeTypes, recipeCounts);

        int locationsIndex = 0;

        // First try the first resource everywhere
        for (int i = 0; i < recipeTypes.length; i++) {
            Resource firstResource = recipeTypes[i];

        }

        List<int[]> allLocations =
            getAllLocations(outputX, outputY, recipeTypes, recipeCounts);

        /**
         * 1220
         *
         *
         *
         *


        // try only direct paths
        // try all four directions
        for (Piece.Orientation orientation : Piece.Orientation.values()) {
            Field copy = new Field(field);
            int sourceX = orientation.getSourceX(outputX);
            int sourceY = orientation.getSourceY(outputY);
            if (field.canPlace(sourceX, sourceY)) {
                Piece sourcePiece = new Piece(outputResource.getSourcePieceType(),
                    orientation, outputResource);
            }
        }
         */
    }

    private List<Field> getAllPlacements(Field field,
                                         int li, List<int[]> locations,
                                         int ri, Resource[] recipeTypes, int[] recipeCounts) {
        if (li == locations.size() && ri == recipeTypes.length) {
            Field result = new Field(field);
            return Collections.singletonList(result);
        }
        List<Field> result = new ArrayList<>();
        for (int i = ri; i < recipeTypes.length; i++) {
            // Switch every type to the first place
            Resource nextResource = recipeTypes[i];
            Util.swap(recipeTypes, i, ri);
            recipeTypes[i] = recipeTypes[ri];
            recipeTypes[ri] = nextResource;
            int nextResourceN = recipeCounts[i];
            recipeCounts[i] = recipeCounts[ri];
            recipeCounts[ri] = nextResourceN;

            // Place the first place
            if (li + nextResourceN >= locations.size())
                throw new UnsupportedOperationException("multiplicity does not fit,"
                    + " putting " + Arrays.asList(recipeCounts) + " in " + locations);
            for (int l = 0; l < nextResourceN; l++) {
                int[] nextLocation = locations.get(li + l);
                Piece p = new Piece(
                    nextResource.getPieceType(),
                    Piece.Orientation.values()[nextLocation[2]],
                    nextResource
                );
                field.place(nextLocation[0], nextLocation[1], p);
            }

            // Do recursive call

            // switch back
            recipeTypes[ri] = recipeTypes[i]; //TODO check if this is right
            recipeTypes[i] = nextResource;

        }
        throw new UnsupportedOperationException();
    }

    private List<Field> getAllPlacements(Field field, List<int[]> locations, Resource[] recipeTypes, int[] recipeCounts) {
        return getAllPlacements(field, 0, locations, 0, recipeTypes, recipeCounts);
    }

    private List<int[]> getLocations(int outputX, int outputY) {
        ArrayList<int[]> locations = new ArrayList<>();
        for (Piece.Orientation orientation : Piece.Orientation.values()) {
            locations.add(new int[]{
                orientation.getSourceX(outputX),
                orientation.getSourceY(outputY),
                orientation.ordinal()
            });
        }
        return locations;
    }

    private List<int[]> getAllLocations(int outputX, int outputY, Resource[] types, int[] counts) {
        return getAllLocations(outputX, outputY, 0, 0, types, counts);
    }

    private List<int[]> getAllLocations(int outputX, int outputY,
                                        int iLocation, int iResource,
                                        Resource[] types, int[] counts) {
        if (iLocation == 4) return Collections.emptyList();
        if (iResource == types.length) return Collections.emptyList();
        //TODO: search permutation algorithm
        throw new UnsupportedOperationException();
    }

    private void fillOneStep(int iPlace, int iResource, Resource[] resources, int[] resourceCounts) {
        if (iPlace == 4) return;
        if (iResource == resources.length) return;

    }

}
