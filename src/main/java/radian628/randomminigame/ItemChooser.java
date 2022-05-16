package radian628.randomminigame;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemChooser {
    List<Double> weights;
    double weightSum;
    List<List<ItemStack>> choices;


    public ItemChooser() {
        weights = new ArrayList<Double>();
        choices = new ArrayList<List<ItemStack>>();
        // {
        //     List<ItemStack> items = new ArrayList<ItemStack>();
        //     items.add(new ItemStack(Material.STONE, 64));
        //     addChoice(items, 1.0);
        // }
        // {
        //     List<ItemStack> items = new ArrayList<ItemStack>();
        //     items.add(new ItemStack(Material.IRON_SWORD, 1));
        //     addChoice(items, 1.0);
        // }
    }

    public List<ItemStack> getRandomItems() {
        double rand = Math.random() * weightSum;
        int index = 0;
        while (rand > weights.get(index)) {
            rand -= weights.get(index);
            index++;
        }
        return choices.get(index);
    }

    public void addChoice(List<ItemStack> choice, double weight) {
        weights.add(weight);
        weightSum += weight;
        choices.add(choice);
    }
}
