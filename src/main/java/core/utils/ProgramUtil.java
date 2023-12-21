package core.utils;

import java.util.UUID;

public class ProgramUtil {

    public static long generateId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

}
