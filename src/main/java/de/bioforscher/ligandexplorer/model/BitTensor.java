package de.bioforscher.ligandexplorer.model;

import de.bioforscher.jstructure.mathematics.SetOperations;
import de.bioforscher.jstructure.model.feature.ComputationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BitTensor {
    //TODO potentially use BitSet implementation

    private final int[] dimensions;
    private final int numberOfDimensions;
    /**
     * use internal list of string representation to store active 'bits'
     */
    private final List<String> entries;

    public BitTensor(int... dimensions) {
        this.dimensions = dimensions;
        this.numberOfDimensions = dimensions.length;
        this.entries = new ArrayList<>();
    }

    public boolean toogleBit(int... indices) {
        String key = toString(indices);
        if(entries.contains(key)) {
            return entries.remove(key);
        } else {
            return entries.add(key);
        }
    }

    public boolean setBit(int... indices) {
        String key = toString(indices);
        if(entries.contains(key)) {
            return false;
        } else {
            entries.add(key);
            return true;
        }
    }

    public boolean removeBit(int... indices) {
        String key = toString(indices);
        return entries.remove(key);
    }

    private static String toString(int... indicies) {
        return IntStream.of(indicies)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private static int[] toArray(String string) {
        return Pattern.compile(",").
                splitAsStream(string)
                .mapToInt(Integer::valueOf)
                .toArray();
    }

    public static double computeDistance(BitTensor bitTensor1, BitTensor bitTensor2) {
        return computeJaccardIndex(bitTensor1, bitTensor2);
    }

    private static double computeJaccardIndex(BitTensor bitTensor1, BitTensor bitTensor2) {
        checkForCompatibleDimensions(bitTensor1, bitTensor2);

        int size1 = bitTensor1.entries.size();
        int size2 = bitTensor2.entries.size();
        double unionSize = SetOperations.createUnionSet(bitTensor1.entries, bitTensor2.entries).size();

        return unionSize / (size1 + size2 + unionSize);
    }

    private static void checkForCompatibleDimensions(BitTensor bitTensor1, BitTensor bitTensor2) {
        if(Arrays.equals(bitTensor1.dimensions, bitTensor2.dimensions)) {
            return;
        }

        throw new ComputationException("dimensions of tensors not compatible: " +
                Arrays.toString(bitTensor1.dimensions) + " vs " + Arrays.toString(bitTensor2.dimensions));
    }
}
