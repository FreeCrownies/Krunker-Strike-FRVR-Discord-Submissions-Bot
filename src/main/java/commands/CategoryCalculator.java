package commands;

public class CategoryCalculator {

    public static Category getCategoryByCommand(Class<? extends Command> c) {
        String packageName = c.getPackage().getName();
        // if (packageName.endsWith("gimmickscategory")) return Category.GIMMICKS;
        if (packageName.endsWith("configurationcategory")) return Category.CONFIGURATION;
        if (packageName.endsWith("utilitycategory")) return Category.UTILITY;
        if (packageName.endsWith("informationcategory")) return Category.INFORMATION;
        // if (packageName.endsWith("interactionscategory")) return Category.INTERACTIONS;
        if (packageName.endsWith("spacesettingscategory")) return Category.SPACE_SETTINGS;
        if (packageName.endsWith("spacecategory")) return Category.SPACE;
         if (packageName.endsWith("casinocategory")) return Category.CASINO;
        if (packageName.endsWith("moderationcategory")) return Category.MODERATION;
        return null;
    }

}
