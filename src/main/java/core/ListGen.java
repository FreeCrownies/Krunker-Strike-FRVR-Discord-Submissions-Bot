package core;

import core.utils.StringUtil;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;

public class ListGen<T> {

    public static final int SLOT_TYPE_NONE = 0;
    public static final int SLOT_TYPE_BULLET = 1;
    public static final int SLOT_TYPE_NUMBERED = 2;
    public static final int SLOT_TYPE_ARROW = 3;

    public String getList(Collection<T> objs, Locale locale, Function<T, String> getNames) {
        return getList(objs, locale, SLOT_TYPE_NONE, getNames);
    }

    public String getList(Collection<T> objs, String valueIfEmpty, Function<T, String> getNames) {
        return getList(objs, valueIfEmpty, SLOT_TYPE_NONE, getNames);
    }

    public String getList(Collection<T> objs, Function<T, String> getNames) {
        return getList(objs, SLOT_TYPE_NONE, getNames);
    }

    public String getList(Collection<T> objs, Locale locale, int slotType, Function<T, String> getNames) {
        String valueIfEmpty;
        valueIfEmpty = TextManager.getString(locale, TextManager.GENERAL, "not_set");
        return getList(objs, valueIfEmpty, slotType, getNames);
    }

    public String getList(Collection<T> objs, int slotType, Function<T, String> getNames) {
        return getList(objs, "", slotType, getNames);
    }

    public String getList(Collection<T> objs, int slotType, Function<T, String> getNames, String prefix) {
        return getList(objs, "", slotType, getNames, prefix);
    }

    public String getList(Collection<T> objs, String valueIfEmpty, int slotType, Function<T, String> getNames) {
        return getList(objs, valueIfEmpty, slotType, getNames, "");
    }

    public String getList(Collection<T> objs, String valueIfEmpty, int slotType, Function<T, String> getNames, String prefix) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (T obj : objs) {
            try {
                String value = getNames.apply(obj);
                sb.append(prefix);
                switch (slotType) {
                    case SLOT_TYPE_BULLET -> sb.append("• ");
                    case SLOT_TYPE_NUMBERED -> sb.append(i).append(". ");
                    case SLOT_TYPE_ARROW -> sb.append("➤ ");
                }
                sb.append(value);
                sb.append("\n");
            } catch (Throwable e) {
                MainLogger.get().error("Exception", e);
            }
            i++;
        }
        if (sb.toString().isEmpty()) return valueIfEmpty;
        return StringUtil.shortenStringLine(sb.toString(), 1024);
    }
}
