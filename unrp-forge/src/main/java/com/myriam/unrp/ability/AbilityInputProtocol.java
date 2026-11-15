package com.myriam.unrp.ability;

/** Stable move indices: press/release ranges cannot overlap when a power has many stances. */
public final class AbilityInputProtocol {
    public static final int CAPACITY = 32, PRESS = 200, RELEASE = PRESS + CAPACITY;
    private AbilityInputProtocol() {}
    public static int press(int index) { check(index); return PRESS + index; }
    public static int release(int index) { check(index); return RELEASE + index; }
    public static boolean isMove(int action) { return action >= PRESS && action < RELEASE + CAPACITY; }
    public static boolean isPress(int action) { return action >= PRESS && action < RELEASE; }
    public static int index(int action) {
        if (!isMove(action)) throw new IllegalArgumentException("Not a move input: " + action);
        return action - (isPress(action) ? PRESS : RELEASE);
    }
    private static void check(int index) { if (index < 0 || index >= CAPACITY) throw new IllegalArgumentException("Move index: " + index); }
}
