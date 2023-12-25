package commands;

public class CategoryCalculator {

    public static Category getCategoryByCommand(Class<? extends Command> c) {
        String packageName = c.getPackage().getName();
        if (packageName.endsWith("dm")) return Category.DM;
        return null;
    }

}
