package hotel.command;

/**
 * Sealed interface representing a parsed command.
 *
 * <p>Using a sealed type with Java 21 pattern-matching switch means the
 * compiler enforces exhaustive handling — no default case needed, and no
 * risk of silently ignoring a new command type in future.
 */
public sealed interface Command permits AvailabilityCommand, SearchCommand {}
