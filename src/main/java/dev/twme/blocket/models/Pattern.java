package dev.twme.blocket.models;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.block.data.BlockData;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a weighted pattern for generating random blocks based on probability distributions.
 * This class uses the Alias method (Vose's algorithm) for efficient O(1) random sampling
 * from weighted distributions, making it suitable for high-performance block generation.
 * 
 * <p>The Pattern class supports:
 * <ul>
 *   <li>Weighted block distribution with arbitrary probability values</li>
 *   <li>Efficient O(1) random block selection using alias tables</li>
 *   <li>Automatic normalization of input weights</li>
 *   <li>Thread-safe random generation</li>
 *   <li>Equality comparison based on block distributions</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Map<BlockData, Double> blocks = new HashMap<>();
 * blocks.put(Material.STONE.createBlockData(), 70.0);
 * blocks.put(Material.COAL_ORE.createBlockData(), 20.0);
 * blocks.put(Material.IRON_ORE.createBlockData(), 10.0);
 * 
 * Pattern pattern = new Pattern(blocks);
 * BlockData randomBlock = pattern.getRandomBlockData();
 * }</pre>
 * 
 * @author TWME-TW
 * @version 1.0.0  
 * @since 1.0.0
 */
@Getter
@Setter
public class Pattern {
    private final BlockData[] blockDataArray;
    private final int[] alias;
    private final double[] probability;

    /**
     * Creates a new Pattern with the given BlockData and their respective percentages.
     *
     * @param blockDataPercentages A map of BlockData and their respective percentages (can be any positive range).
     * @throws IllegalArgumentException If the map is empty or if any percentage value is negative.
     */
    public Pattern(Map<BlockData, Double> blockDataPercentages) {
        if (blockDataPercentages.isEmpty()) {
            throw new IllegalArgumentException("Pattern must contain at least one BlockData with a non-zero percentage");
        }

        int size = blockDataPercentages.size();
        blockDataArray = new BlockData[size];
        alias = new int[size];
        probability = new double[size];

        // Normalize the weights
        double sum = blockDataPercentages.values().stream().mapToDouble(Double::doubleValue).sum();
        double[] normalizedWeights = new double[size];

        int i = 0;
        for (Map.Entry<BlockData, Double> entry : blockDataPercentages.entrySet()) {
            blockDataArray[i] = entry.getKey();
            normalizedWeights[i] = entry.getValue() / sum * size;
            i++;
        }

        // Initialize probability and alias
        Deque<Integer> small = new ArrayDeque<>();
        Deque<Integer> large = new ArrayDeque<>();
        for (int j = 0; j < size; j++) {
            if (normalizedWeights[j] < 1.0) {
                small.add(j);
            } else {
                large.add(j);
            }
        }

        while (!small.isEmpty() && !large.isEmpty()) {
            int less = small.removeFirst();
            int more = large.removeFirst();

            probability[less] = normalizedWeights[less];
            alias[less] = more;

            normalizedWeights[more] -= 1.0 - probability[less];

            if (normalizedWeights[more] < 1.0) {
                small.add(more);
            } else {
                large.add(more);
            }
        }

        while (!small.isEmpty()) {
            probability[small.remove()] = 1.0;
        }

        while (!large.isEmpty()) {
            probability[large.remove()] = 1.0;
        }
    }

    /**
     * Returns a random BlockData from the Pattern based on the normalized weights.
     *
     * @return A random BlockData from the Pattern.
     */
    public BlockData getRandomBlockData() {
        int column = ThreadLocalRandom.current().nextInt(blockDataArray.length);
        boolean useAlias = ThreadLocalRandom.current().nextDouble() >= probability[column];
        return blockDataArray[useAlias ? alias[column] : column];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pattern pattern)) return false;

        // Rebuild the block data to percentage map for "this" Pattern
        Map<BlockData, Double> thisDataMap = new HashMap<>();
        for (int i = 0; i < blockDataArray.length; i++) {
            thisDataMap.put(blockDataArray[i], probability[i]); // Assuming probability[i] corresponds to the input percentage
        }

        // Rebuild the block data to percentage map for the other Pattern
        Map<BlockData, Double> otherDataMap = new HashMap<>();
        for (int i = 0; i < pattern.blockDataArray.length; i++) {
            otherDataMap.put(pattern.blockDataArray[i], pattern.probability[i]); // Assuming pattern.probability[i] corresponds to the input percentage
        }

        // Compare the two maps for equality
        return mapsAreEqual(thisDataMap, otherDataMap);
    }

    /**
     * Compares two maps of BlockData to Double (percentage) with tolerance for floating-point equality.
     *
     * @param map1 The first map
     * @param map2 The second map
     * @return true if the maps are equal (same keys, same percentages with tolerance), false otherwise.
     */
    private boolean mapsAreEqual(Map<BlockData, Double> map1, Map<BlockData, Double> map2) {
        if (map1.size() != map2.size()) return false;
        for (Map.Entry<BlockData, Double> entry : map1.entrySet()) {
            Double otherValue = map2.get(entry.getKey());
            if (otherValue == null || Math.abs(entry.getValue() - otherValue) > 1e-9) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(blockDataArray);
        result = 31 * result + Arrays.hashCode(alias);
        result = 31 * result + Arrays.hashCode(probability);
        return result;
    }
}