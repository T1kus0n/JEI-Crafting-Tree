package com.lhy.jeict.planning;

import java.util.Locale;

/** Controls how alternatives in one input slot are allocated. */
public enum SubstitutionStrategy {
    /** Use only the alternative explicitly selected in the tree. */
    LOCKED,
    /** Consume all matching alternatives from inventory before crafting or reporting a shortage. */
    MIX_AVAILABLE,
    /** Prefer the alternative with the largest available amount. */
    MOST_AVAILABLE,
    /** Prefer alternatives from a configured namespace, then use the selected alternative. */
    PREFERRED_NAMESPACE,
    /** Require the exact component/subtype identity selected by the user. */
    STRICT_COMPONENTS;

    public String translationKey() {
        return "gui.jeict.recipe_tree.strategy_" + name().toLowerCase(Locale.ROOT);
    }
}
